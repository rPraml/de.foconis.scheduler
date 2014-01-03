/**
 * © Copyright Foconis AG, 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package de.foconis.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;
import lotus.notes.NotesThread;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import de.foconis.core.http.client.HttpResponse;
import de.foconis.core.http.client.NSFServiceHttpClient;
import de.foconis.core.scheduler.XPageScheduler;
import de.foconis.core.servlet.ServletFactory;

/**
 * The patrol job scans all NSFs and reads the NSFJobGroups in "META-INF/services"
 * 
 * @author Roland Praml, Foconis AG
 * 
 */
public class PatrolJob extends PeriodicRunnable {
	private static final Logger log_ = Logger.getLogger(PatrolJob.class.getName());
	private Map<String, DbInfo> dbInfos = new HashMap<String, DbInfo>();
	private static ThreadLocal<JobQueue> queueInstance = new ThreadLocal<JobQueue>();
	private JobQueue queue;

	/**
	 * Initialzies a new PatrolJob
	 * 
	 * @param interval
	 *            the interval in ms
	 */
	public PatrolJob(final long interval, final JobQueue queue) {
		super(interval);
		this.queue = queue;
	}

	/**
	 * Return a list of all nsfs in the dbdirectory
	 * 
	 * @return nsf paths in lowercase without leading "/"
	 * @throws NotesException
	 */
	protected List<String> getFofDbs() throws NotesException {
		NotesThread.sinitThread();

		List<String> ret = new ArrayList<String>();
		try {
			Session sess = NotesFactory.createSession();
			DbDirectory dbDir = sess.getDbDirectory(null);
			Database currDb = dbDir.getFirstDatabase(DbDirectory.DATABASE);
			while (currDb != null) {

				if (isFofDb(currDb)) {
					String dbPath = currDb.getFilePath();
					if (dbPath.toLowerCase().endsWith(".nsf")) {
						dbPath = dbPath.replace('\\', '/');
					}
					ret.add(dbPath.toLowerCase());
				}
				Database recycleDb = currDb;
				currDb = dbDir.getNextDatabase();
				// This is not an org.openntf.domino DB - so recycle it!!
				recycleDb.recycle();
			}
		} catch (NotesException e) {
			throw e;
		} finally {
			NotesThread.stermThread();
		}
		return ret;
	}

	/**
	 * Checks if this DB is a FOF DB and if it should be queried
	 * 
	 * @param currDb
	 * @return
	 * @throws NotesAPIException
	 */
	protected boolean isFofDb(final Database currDb) {
		try {
			currDb.open();
			return currDb.getForm("de.foconis.form.xsp.default") != null;
		} catch (NotesException e) {
			// log_.log(Level.WARNING, "Cannot access database " + currDb, e);
			return true;
		}
	}

	/**
	 * scans the nsfs and updates the state stored in dbInfos
	 * 
	 * @param nsfs
	 * @param dbInfos
	 */
	protected void updateDbInfo(final List<String> nsfs) {
		// scan the DB if design was modified is done via NAPI

		// first set all databases in dbInfos to "unseen"
		for (Entry<String, DbInfo> e : dbInfos.entrySet()) {
			e.getValue().setSeen(false);
		}

		NotesContext ctx = new NotesContext(null);
		NotesContext.initThread(ctx);
		try {

			for (String nsf : nsfs) {
				DbInfo info = dbInfos.get(nsf);
				if (info == null) {
					info = new DbInfo(nsf);
					dbInfos.put(nsf, info);
				}
				try {

					long designTimeStamp = NotesSession.getLastNonDataModificationDateByName(nsf);
					info.setDesignTimeStamp(designTimeStamp);
					info.setSeen(true);
				} catch (NotesAPIException e) {
					// we will get here if the file was deleted in the meantime. So ignore it
					e.printStackTrace();
					log_.log(Level.WARNING, "could not get timestamp for: " + nsf, e);
				}
			}
		} finally {
			NotesContext.termThread();
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void runCode() {

		List<String> nsfs;
		try {
			nsfs = getFofDbs();
		} catch (NotesException e) {
			log_.log(Level.WARNING, "Could not scan databases", e);
			return;
		}

		updateDbInfo(nsfs);
		// delete all unseen
		Iterator<Entry<String, DbInfo>> it = dbInfos.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, DbInfo> infoEntry = it.next();
			DbInfo info = infoEntry.getValue();

			try {
				if (!info.isSeen()) {
					log_.fine("unRegistering NSF (deleted?): " + info.getDatabasePath());
					queue.unRegisterDb(info.getDatabasePath());
					it.remove();
				} else if (info.isDirty()) {
					log_.fine("registering NSF: " + info.getDatabasePath());
					queryNSF(info.getDatabasePath());
					info.markClean();
				}
			} catch (Exception e) {
				log_.log(Level.SEVERE, "ERROR while accessing DB: " + info.getDatabasePath(), e);
			}
		}
		XPageScheduler.getInstance().reCalc();
	}

	/**
	 * This method is called by the partol-job
	 * 
	 * @param trigName
	 * @throws SchedulerException
	 */
	protected boolean queryNSF(String databasePath) {
		try {
			queueInstance.set(queue);
			databasePath = XUtils.normalizeDbPath(databasePath);
			String url = databasePath + ServletFactory.SERVLET_PATH;

			// prepare the parameters for the request
			Map<String, String[]> params = new HashMap<String, String[]>();
			params.put("action", new String[] { "register" });
			NSFServiceHttpClient cl = new NSFServiceHttpClient(null); // run in server context
			HttpResponse resp = cl.doRequest("GET", url, params);
			// this will call the servlet (which sets up the agentContext and so on)
			// and calls "registerDefinitions"
			if (resp.getStatus() == HttpServletResponse.SC_OK) {
				return true;
			} else {
				log_.log(Level.SEVERE, "Querying " + url + " returned:\n" + resp.getResponseText());
				return false;
			}
		} finally {
			queueInstance.set(null);
		}
	}

	/**
	 * 
	 * @return
	 */
	public static JobQueue getCurrentQueue() {
		return queueInstance.get();
	}

}
