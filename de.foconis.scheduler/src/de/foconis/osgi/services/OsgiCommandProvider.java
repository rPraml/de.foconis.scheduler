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
package de.foconis.osgi.services;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import com.ibm.commons.util.StringUtil;

import de.foconis.core.http.client.HttpResponse;
import de.foconis.core.http.client.NSFServiceHttpClient;
import de.foconis.core.scheduler.XPageScheduler;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class OsgiCommandProvider implements CommandProvider {
	private static final String tab = "\t";
	private static final String newline = "\r\n";
	private static final Logger log_ = Logger.getLogger(OsgiCommandProvider.class.getName());

	private Map<String, Logger> configuredLoggers = new HashMap<String, Logger>();

	private void addHeader(final String title, final StringBuffer sb) {
		sb.append("---");
		sb.append(title);
		sb.append("---");
		sb.append(newline);
	}

	private void addCommand(final String cmd, final String desc, final StringBuffer sb) {
		sb.append(tab);
		sb.append(cmd);
		sb.append(" - ");
		sb.append(desc);
		sb.append(newline);
	}

	private void addCommand(final String cmd, final String params, final String desc, final StringBuffer sb) {
		sb.append(tab);
		sb.append(cmd);
		sb.append(" ");
		sb.append(params);
		sb.append(" - ");
		sb.append(desc);
		sb.append(newline);
	}

	/**
	 * Returns all available commands
	 */
	@Override
	public String getHelp() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append(newline); // the previous call forgot newline
		addHeader("Foconis logger configuration", sb);
		addCommand("logging list", "(filter)", "Get status of all loaded loggers", sb);
		addCommand("logging set", "className level", "Get status of all loaded loggers", sb);
		addHeader("Foconis scheduler", sb);
		addCommand("scheduler (list)", "Return the scheduler plan", sb);
		addCommand("scheduler stop", "Stops the scheduler", sb);
		addCommand("scheduler start", "(numThreads)", "Starts the scheduler with the amount of threads (default: 5)", sb);
		addHeader("For debug", sb);
		addCommand("urlget url", "(username)", "Perform a fake HTTP request with username (url=/path/to.nsf/xpage.xsp)", sb);
		return sb.toString();
	}

	/**
	 * Returns a readable name for a logger.
	 * 
	 * @param l
	 * @return
	 */
	private String getLoggerName(final Logger l) {
		String s = l.getName();
		if (StringUtil.isEmpty(s))
			return "(root)";
		return s;
	}

	/**
	 * Returns the readable loglevel
	 * 
	 * @param l
	 * @return
	 */
	private String getLogLevel(final Logger l) {
		Level lvl = l.getLevel();
		if (lvl == null) {
			Logger parent = l.getParent();
			if (parent == null)
				return "UNKNWON";

			return getLoggerName(parent) + " > " + getLogLevel(parent);
		} else {
			return lvl.toString();
		}
	}

	/**
	 * focLoggers return all active loggers, filtered by a string
	 * 
	 * @param ci
	 * @throws Exception
	 */
	public void _logging(final CommandInterpreter ci) throws Exception {
		String cmd = ci.nextArgument();
		try {
			if ("list".equals(cmd)) {
				focLog_list(ci);
			} else if ("set".equals(cmd)) {
				focLog_set(ci);
			} else {
			}
		} catch (Exception e) {
			ci.printStackTrace(e);
			ci.println("");
			throw e;
		}
	}

	/**
	 * List all loggers
	 * 
	 * @param ci
	 */
	private void focLog_list(final CommandInterpreter ci) {
		LogManager logManager = LogManager.getLogManager();
		Enumeration<String> loggerNames = logManager.getLoggerNames();
		log_.warning("Test!!!");
		String filter = ci.nextArgument();

		Dictionary<String, String> ret = new Hashtable<String, String>();

		while (loggerNames.hasMoreElements()) {
			Logger l = logManager.getLogger(loggerNames.nextElement());
			String loggerName = getLoggerName(l);
			if (StringUtil.isEmpty(filter) || loggerName.indexOf(filter) >= 0) {
				if (l != null) {
					ret.put(loggerName, getLogLevel(l) + getHandlers(l));
				}
			}
		}

		ci.println("Loggers currently in memory");
		ci.printDictionary(ret, null);
	}

	/**
	 * com.ibm.domino.osgi.core.adaptor.DominoConsoleHandler com.ibm.domino.http.bootstrap.logger.RCPXMLLogHandler
	 * com.ibm.domino.http.bootstrap.logger.RCPXMLTraceHandler
	 * 
	 * @param l
	 * @return
	 */
	private String getHandlers(final Logger l) {
		// TODO Auto-generated method stub
		StringBuilder ret = new StringBuilder();
		for (Handler h : l.getHandlers()) {
			ret.append("\n  => ");
			ret.append(h.getClass().getName());
			ret.append(" = ");
			ret.append(h.getLevel());
		}
		return ret.toString();
	}

	/**
	 * Set a certain logger to a value
	 * 
	 * @param ci
	 */
	private void focLog_set(final CommandInterpreter ci) {
		String loggerName = ci.nextArgument();
		String loggerValue = ci.nextArgument();
		if (StringUtil.isEmpty(loggerName)) {
			ci.println("Usage: focLog set <name-of-logger> <OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST|ALL>");
			return;
		}
		if (loggerName.equals("(root)")) {
			loggerName = "";
		}
		int colon = loggerName.indexOf(":");

		if (colon >= 0) {
			int handlerIdx = Integer.parseInt(loggerName.substring(colon + 1));
			loggerName = loggerName.substring(0, colon);
			Logger l = Logger.getLogger(loggerName);
			Handler h = l.getHandlers()[handlerIdx];
			h.setLevel(Level.parse(loggerValue));
			ci.println("Logger " + l.getName() + ", Handler: " + h.getClass().getName() + " = " + h.getLevel());
		} else {

			Logger l = Logger.getLogger(loggerName);

			if (loggerValue == null) {
				configuredLoggers.remove(loggerName);
				l.setLevel(null);
			} else {
				configuredLoggers.put(l.getName(), l);
				l.setLevel(Level.parse(loggerValue));
			}
			ci.println("Logger " + l.getName() + " = " + getLogLevel(l));
		}
	}

	// protected String getJobState(final Job job) {
	// switch (job.getState()) {
	// case Job.RUNNING:
	// return "RUNNING";
	// case Job.WAITING:
	// return "WAITNING";
	// case Job.SLEEPING:
	// return "SLEEPING";
	// }
	// return "NONE";
	// }

	// protected String getJobId(final Job job) {
	// String s = Integer.toHexString(System.identityHashCode(job)).toUpperCase();
	// while (s.length() < 8) {
	// s = "0".concat(s);
	// }
	// return s.substring(0, 4) + "-" + s.substring(4);
	// }

	// private void printRunningJobs(final CommandInterpreter ci, final Scheduler sched) throws SchedulerException {
	//
	// List<JobExecutionContext> runningJobs = sched.getCurrentlyExecutingJobs();
	// ci.println("Current RUNNING jobs:");
	// if (runningJobs.isEmpty()) {
	// ci.println("- the scheduler is idle -");
	// } else {
	// for (JobExecutionContext jec : runningJobs) {
	// JobDetail jd = jec.getJobDetail();
	// jec.getJobInstance();
	// Date startTime = jec.getFireTime();
	// Long runTime = System.currentTimeMillis() - startTime.getTime();
	// if (runTime > 10000) {
	// sched.interrupt(jd.getKey());
	// }
	//
	// ci.println("JOB: " + jd.getKey());
	// ci.println(" Start at: " + jec.getFireTime() + " running " + runTime + " ms");
	// }
	// }
	// }

	public void _sched(final CommandInterpreter ci) {
		_scheduler(ci);
	}

	/**
	 * Return all running eclipse jobs
	 * 
	 * @param ci
	 */
	public void _scheduler(final CommandInterpreter ci) {
		String cmd = ci.nextArgument();
		if (StringUtil.isEmpty(cmd) || "list".equals(cmd)) {
			ci.println(XPageScheduler.getInstance().getSchedulePlan(null));
		} else if ("stop".equals(cmd)) {
			XPageScheduler.getInstance().stop(System.out);
		} else if ("start".equals(cmd)) {
			String amount = ci.nextArgument();
			int threads = 5;
			if (!StringUtil.isEmpty(amount)) {
				threads = Integer.parseInt(amount);
			}
			XPageScheduler.getInstance().start(threads);
			// Scheduler sched = null; // XPageScheduler.getInstance().getScheduler();
			// try {
			// printRunningJobs(ci, sched);
			// Set<JobKey> jobs = sched.getJobKeys(GroupMatcher.anyJobGroup());
			// ci.println("Current active jobs");
			// ci.println("Job-ID\t\tJob-State\tThread\tJob-Name\tResult");
			// for (JobKey jobKey : jobs) {
			// ci.println(jobKey);
			// List<? extends Trigger> triggers = sched.getTriggersOfJob(jobKey);
			// for (Trigger trigger : triggers) {
			// ci.println(trigger);
			// }
			// }
			//
			// Set<TriggerKey> triggers = sched.getTriggerKeys(GroupMatcher.anyTriggerGroup());
			// ci.println("Current active triggers");
			// for (TriggerKey trigger : triggers) {
			// Trigger trg = sched.getTrigger(trigger);
			// ci.println(trigger);
			// ci.println(trg);
			// JobDetail jd = sched.getJobDetail(trg.getJobKey());
			// ci.println(jd);
			// }
			// } catch (SchedulerException e) {
			// ci.println(e);
			// }
		} else if ("kill".equals(cmd)) {
			// kill(ci);
		}
	}

	/**
	 * this is for debugging the NSFServiceHttpClient
	 * 
	 * @param ci
	 */
	public void _urlget(final CommandInterpreter ci) {
		String url = ci.nextArgument();
		String name = ci.nextArgument();
		ci.println("urlget: " + url + " user:" + name);
		NSFServiceHttpClient cl = new NSFServiceHttpClient(name);
		HttpResponse resp = cl.doRequest("GET", url, null);
		ci.println("Return code: " + resp.getStatus() + " " + resp.getStatusMessage());
		ci.println(resp.getResponseText());
	}

	/**
	 * Kills the given job ID
	 * 
	 * @param ci
	 */
	// private void kill(final CommandInterpreter ci) {
	//
	// String jobId = ci.nextArgument();
	// if (StringUtil.isEmpty(jobId)) {
	// ci.println("Usage: focJob kill <jobID> <waitsecs>");
	// return;
	// }
	// // read how long to wait
	// Integer secs = 0;
	// try {
	// String secsStr = ci.nextArgument();
	// if (!StringUtil.isEmpty(secsStr)) {
	// secs = Integer.parseInt(secsStr);
	// }
	// } catch (NumberFormatException e) {
	// ci.println("Usage: focJob kill <jobID> <waitsecs>");
	// return;
	//
	// }
	//
	// Job[] jobs = null; // XPageAgentManager.getInstance().getJobs();
	// Job jobToKill = null;
	// for (Job job : jobs) {
	// if (getJobId(job).indexOf(jobId) != -1 || job.getName().indexOf(jobId) != -1) {
	// if (jobToKill == null) {
	// jobToKill = job;
	// } else {
	// ci.println("The ID '" + jobId + "' is not unique");
	// return;
	// }
	// }
	// }
	// if (jobToKill == null) {
	// ci.println("The ID '" + jobId + "' was not found");
	// return;
	// }
	//
	// if (jobToKill.getState() != Job.RUNNING) {
	// ci.println("The Job '" + jobToKill.getName() + "' does not run. It will be canceled");
	// jobToKill.cancel();
	// return;
	// }
	//
	// System.err.println("Killing " + jobToKill.getName());
	//
	// try {
	// jobToKill.cancel();
	// if (secs == 0) {
	// // wait 5 seconds to give the job time to cancel
	// for (int i = 50; i > 0; i--) {
	// Thread.sleep(100);
	// if (jobToKill.getState() != Job.RUNNING) {
	// ci.println("The Job '" + jobToKill.getName() + "' quit normally");
	// return;
	// }
	// }
	// ci.println("The Job '" + jobToKill.getName() + "' is still running");
	// } else {
	// Thread t = jobToKill.getThread();
	// if (t != null) {
	// for (int i = secs; i > 0; i--) {
	// Thread.sleep(1000);
	// if (jobToKill.getState() != Job.RUNNING)
	// return;
	// System.err.println("Waiting " + i + " seconds for '" + jobToKill + "' to quit");
	// }
	// t.interrupt();
	// ci.print("Interrupt sent to Thread " + t.getName());
	// // TODO check if job is terminated
	// }
	// }
	// } catch (InterruptedException e) {
	// ci.println(e);
	// }
	// }

}
