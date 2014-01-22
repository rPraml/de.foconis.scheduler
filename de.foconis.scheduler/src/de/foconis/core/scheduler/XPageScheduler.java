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
package de.foconis.core.scheduler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import de.foconis.core.internal.JobQueue;
import de.foconis.core.internal.PatrolJob;
import de.foconis.core.internal.PeriodicRunnable;
import de.foconis.core.internal.WorkerJob;

/**
 * @author praml
 * 
 */
public class XPageScheduler {
	private static final Logger log_ = Logger.getLogger(XPageScheduler.class.getName());

	private List<PeriodicRunnable> workers = new ArrayList<PeriodicRunnable>();
	private PatrolJob patrolJob;

	private boolean isStarted = false;
	private volatile static XPageScheduler instance_ = new XPageScheduler();

	private JobQueue jobQueue = new JobQueue();

	/**
	 * Context is a singleton, ensure this by private constructor
	 */
	private XPageScheduler() {
		super();
	}

	/**
	 * retrieve the current instance of XPageScheduler
	 * 
	 * @return
	 */
	public static XPageScheduler getInstance() {
		return instance_;
	}

	/**
	 * Starts the whole Scheduler
	 * 
	 */
	public void start(final int numWorkers) {
		synchronized (this) {
			if (isStarted) {
				throw new IllegalStateException("Scheduler already started");
			}
			isStarted = true;
			patrolJob = new PatrolJob(30000, jobQueue);
			patrolJob.start("NSF Patrol-job");
			workers.add(patrolJob);
			log_.warning(patrolJob.getName() + " started.");

			for (int i = 1; i <= numWorkers; i++) {
				WorkerJob worker = new WorkerJob(500, jobQueue); // TODO - Workers fragen alle 0,5 sec die queue. Das ist unschön
				worker.start("NSF Worker-job " + i);
				workers.add(worker);
				log_.warning(worker.getName() + " started.");
			}
			System.out.println("Scheduler started " + numWorkers + " threads.");
		}
	}

	/**
	 * Rescans all databases
	 * 
	 */
	public void refresh() {
		log_.info("Patrol-job triggered");
		patrolJob.awake();
	}

	/**
	 * Stops the scheduler
	 */
	public void stop(final PrintStream out) {
		synchronized (this) {
			if (!isStarted) {
				out.println("The scheduler is not started");
			} else {
				isStarted = false;
			}

			out.println("Sending to all threads the STOP signal");

			Iterator<PeriodicRunnable> it = workers.iterator();
			while (it.hasNext()) {
				PeriodicRunnable job = it.next();
				out.println("Stopping " + job.getName());
				if (job.stop(1000, false)) {
					out.println("... Stopped after 1 sec");
					it.remove();
				}
			}

			if (!workers.isEmpty()) {
				out.println("There are stil some threads running, so give them some seconds: " + workers);
				it = workers.iterator();
				while (it.hasNext()) {
					PeriodicRunnable job = it.next();
					if (job.stop(5000, false)) {
						out.println(job.getName() + " Stopped after some extra seconds");
						it.remove();
					}
				}
			}

			if (!workers.isEmpty()) {
				out.println("There are stil some threads running, this will be killed now: " + workers);
				it = workers.iterator();
				while (it.hasNext()) {
					PeriodicRunnable job = it.next();
					if (job.stop(5000, true)) {
						out.println("... Killed after 5 sec");
						it.remove();
					}
				}
			}
			if (!workers.isEmpty()) {
				out.println("Could not kill: " + workers);
			}
		}
	}

	/**
	 * 
	 */
	public void reCalc() {
		jobQueue.reCalc();
	}

	/**
	 * @param object
	 * @return
	 */
	public String getSchedulePlan(final String filter) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append("SchedulePlan:\n");
		for (PeriodicRunnable worker : workers) {
			sb.append(worker.getName());
			sb.append(": ");
			sb.append(worker.getStatus());
			sb.append('\n');
		}
		sb.append(jobQueue);
		return sb.toString();
	}

}
