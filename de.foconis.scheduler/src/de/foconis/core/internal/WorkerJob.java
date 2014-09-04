/*
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.foconis.core.http.client.HttpResponse;
import de.foconis.core.http.client.NSFServiceHttpClient;
import de.foconis.core.job.NSFJob;
import de.foconis.core.job.NSFJobGroup;
import de.foconis.core.scheduler.XPageScheduler;
import de.foconis.core.servlet.ServletFactory;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class WorkerJob extends PeriodicRunnable {
	private static final Logger log_ = Logger.getLogger(WorkerJob.class.getName());

	JobQueue queue;
	private NSFJob innerJob = null;
	private static ThreadLocal<WorkerJob> instance = new ThreadLocal<WorkerJob>();
	private RunnableJobGroup nextJobGroup;

	/**
	 * @param interval
	 */
	public WorkerJob(final long interval, final JobQueue queue) {
		super(interval);
		this.queue = queue;
	}

	/**
	 * pulls the next job out of the queue
	 */
	@Override
	protected void runCode() {
		synchronized (queue) {
			nextJobGroup = queue.getNextJobGroup();
		}
		if (nextJobGroup != null) {
			instance.set(this);
			try {
				try {
					runJobsInNSF(nextJobGroup);
				} finally {
					queue.jobComplete(nextJobGroup);
				}
			} finally {
				instance.set(null);
			}
		}
		// TODO RPr Dies ist unschön!!!
		XPageScheduler.getInstance().reCalc();
	}

	/**
	 * @param jobs
	 *            the jobGroup to run
	 */
	private boolean runJobsInNSF(final RunnableJobGroup jobs) {

		String runOnBehalf = jobs.getRunOnBehalf();
		String signer = jobs.getSignedBy();

		// do some security checks
		if (signer == null) {
			log_.severe(jobs.getDatabasePath() + ":" + jobs.getClassName() + " cannot determine signer!");
			return false;
		}

		if (runOnBehalf == null || "".equals(runOnBehalf) || "@server".equalsIgnoreCase(runOnBehalf)) {
			// if running in server context, this means we have unrestricted rights
			runOnBehalf = null;

			if (!XUtils.mayRunUnrestricted(signer)) {
				log_.severe(jobs.getDatabasePath() + ":" + jobs.getClassName() + ": signed by " //
						+ signer + ". Cannot run unrestricted operations (not listed in 'Run unrestricted operations')");
				return false;
			}

		} else if ("@signer".equalsIgnoreCase(runOnBehalf)) {
			// if no user or @signer is specified. run it as signer (in a XPageSession)
			runOnBehalf = signer;
		} else {

			if (!signer.equals(runOnBehalf) && !XUtils.mayRunOnBehalf(signer)) {
				log_.severe(jobs.getDatabasePath() + ":" + jobs.getClassName() + ": signed by " //
						+ signer + ". Cannot run operation on behalf of " + runOnBehalf  //
						+ " (not listed in 'Sign agents that run on behalf of')");
				return false;
			}
		}

		String url = jobs.getDatabasePath() + ServletFactory.SERVLET_PATH;
		// System.out.println("Execute: " + url + " job:" + jobName + " class:" + jobDefinitionClass);
		// prepare the parameters for the request
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("action", new String[] { "invoke" });
		params.put("signer", new String[] { signer });
		params.put(NSFJobGroup.class.getName(), new String[] { jobs.getClassName() });

		NSFServiceHttpClient cl = new NSFServiceHttpClient(runOnBehalf);
		instance.set(this);
		try {
			HttpResponse resp = cl.doRequest("GET", url, params);
			return resp.getStatus() == 200;
		} finally {
			instance.set(null);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.foconis.core.runnable.AbstractRunnable#stop(long, boolean)
	 */
	@Override
	public boolean stop(final long timeout, final boolean force) {
		synchronized (this) {
			if (innerJob != null) {
				innerJob.stop();
			}
		}
		return super.stop(timeout, force);
	}

	/**
	 * Notified by the servlet which job is running
	 * 
	 * @param job
	 */
	public void setInnerJob(final NSFJob job) {
		synchronized (this) {
			this.innerJob = job;
		}
	}

	@Override
	public String getStatus() {
		synchronized (this) {
			if (nextJobGroup == null) {
				return super.getStatus();
			} else {
				return super.getStatus() + ": " + nextJobGroup.getKey();
			}
		}
	}

	public static WorkerJob getInstance() {
		return instance.get();
	}

}
