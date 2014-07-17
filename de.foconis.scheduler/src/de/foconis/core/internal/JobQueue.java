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
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import de.foconis.core.schedule.Schedule;
import de.foconis.core.scheduler.JobListEnty;
import de.foconis.core.scheduler.JobStatus;

/**
 * @author praml
 * 
 */
public class JobQueue {
	private static final Logger log_ = Logger.getLogger(JobQueue.class.getName());
	// all schedule entries
	// the key is "scheduleEntry.getKey()"
	private Map<String, List<JobGroup>> registeredJobGroups = new HashMap<String, List<JobGroup>>();

	Map<String, JobGroup> allJobs = new HashMap<String, JobGroup>();

	// the key is "scheduleEntry.getKey()"
	Map<String, RunnableJobGroup> readyForRun = new HashMap<String, RunnableJobGroup>();

	// the key is "scheduleEntry.getKey()"
	Map<String, Calendar> lastRuns = new HashMap<String, Calendar>();
	Map<String, Calendar> nextRuns = new HashMap<String, Calendar>();
	Set<String> blockedGroupIds = new HashSet<String>();

	// private static JobQueue instance = new JobQueue();
	//
	// public static JobQueue getInstance() {
	// return instance;
	// }
	//
	// // JobQueue is a singleton
	// private JobQueue() {
	//
	// }

	/**
	 * Updates the "allJob" map. This map contains all jobs that are currently "on schedule"
	 * 
	 * @param dbJobGroups
	 */
	private void update() {
		synchronized (this) {
			// die scheduleDefinitions sind stateless, darum dürfen wir die Objekte einfach ersetzen
			allJobs.clear();

			for (Entry<String, List<JobGroup>> jobGroupsEntry : registeredJobGroups.entrySet()) {
				for (JobGroup jobGroup : jobGroupsEntry.getValue()) {
					allJobs.put(jobGroup.getKey(), jobGroup);
				}
			}
			// cleanup lastRuns & nextRuns
			Iterator<String> it = lastRuns.keySet().iterator();
			while (it.hasNext()) {
				if (!allJobs.containsKey(it.next())) {
					it.remove();
				}
			}
			it = nextRuns.keySet().iterator();
			while (it.hasNext()) {
				if (!allJobs.containsKey(it.next())) {
					it.remove();
				}
			}

		}
	}

	/**
	 * Recalculates the schedule-plan
	 */
	public void reCalc() {
		synchronized (this) {
			for (Entry<String, JobGroup> jobGroupEntry : allJobs.entrySet()) {
				JobGroup jobGroup = jobGroupEntry.getValue();
				String jobGroupKey = jobGroup.getKey();
				Schedule schedule = jobGroup.getSchedule();

				// here we compoute the nextRun and put this in the nextRuns list (if there is no one before me)
				Calendar lastRun = lastRuns.get(jobGroupKey);
				if (lastRun != null) {
					lastRun = (Calendar) lastRun.clone();
				}
				Calendar nextRun = schedule.getNextRunTime(lastRun);
				nextRuns.put(jobGroup.getKey(), nextRun);
			}

			// now we have computed all "nextRuns"... we fill the readyForRun - list
			Calendar now = new GregorianCalendar();

			for (Entry<String, Calendar> entry : nextRuns.entrySet()) {
				String jobGroupKey = entry.getKey();
				Calendar nextRun = entry.getValue();
				if (!nextRun.after(now)) {
					// job is ready for run
					if (!readyForRun.containsKey(jobGroupKey)) {
						JobGroup jobGroup = allJobs.get(jobGroupKey);
						readyForRun.put(jobGroupKey, new RunnableJobGroup(jobGroup));
					}
				}
			}
		}
	}

	/**
	 * returns the next possible job group
	 * 
	 * @return
	 */
	public RunnableJobGroup getNextJobGroup() {
		if (readyForRun.isEmpty()) {
			return null;
		}
		synchronized (this) {
			// first, try to find a blocked job that is now unblocked
			List<RunnableJobGroup> blockedJobs = new ArrayList<RunnableJobGroup>();
			List<RunnableJobGroup> scheuledJobs = new ArrayList<RunnableJobGroup>();

			for (Entry<String, RunnableJobGroup> entry : readyForRun.entrySet()) {
				RunnableJobGroup runningJob = entry.getValue();
				if (runningJob.getStatus() == JobStatus.BLOCKED) {
					// this job can be pulled
					String groupId = runningJob.getGroupId();
					if (!blockedGroupIds.contains(groupId)) {
						blockedJobs.add(runningJob);
					}
				} else if (runningJob.getStatus() == JobStatus.SCHEDULED) {
					String groupId = runningJob.getGroupId();
					if (!blockedGroupIds.contains(groupId)) {
						scheuledJobs.add(runningJob);
					}
				}
			}

			RunnableJobGroup candidate = null;
			if (!blockedJobs.isEmpty()) {
				candidate = Collections.min(blockedJobs);
			} else if (!scheuledJobs.isEmpty()) {
				candidate = Collections.min(scheuledJobs);
			}

			if (candidate != null) {
				System.out.println("We have " + scheuledJobs + " on schedule and " + blockedJobs + " blocked");
				blockedGroupIds.add(candidate.getGroupId());
				candidate.setStatus(JobStatus.RUNNING);
				return candidate;
			}
		}
		return null;
	}

	/**
	 * removes a job from readyForRun list
	 * 
	 * @param complete
	 */
	public void jobComplete(final RunnableJobGroup complete) {
		synchronized (allJobs) {
			complete.setStatus(JobStatus.COMPLETE);
			readyForRun.remove(complete.getKey());
			blockedGroupIds.remove(complete.getGroupId());
			lastRuns.put(complete.getKey(), new GregorianCalendar());
		}
		reCalc();
	}

	/**
	 * This returns a sorted list of all jobs (useful for debug output)
	 * 
	 * @return
	 */
	public List<JobListEnty> getJobList() {
		List<JobListEnty> ret = new ArrayList<JobListEnty>();
		synchronized (allJobs) {
			for (Entry<String, JobGroup> jobGroupEntry : allJobs.entrySet()) {
				JobGroup jobGroup = jobGroupEntry.getValue();
				String key = jobGroup.getKey();
				JobListEnty entry = new JobListEnty(jobGroup.getDatabasePath(), jobGroup.getClassName(), jobGroup.getSchedule(),
						lastRuns.get(key), nextRuns.get(key), readyForRun.get(key));
				ret.add(entry);
			}
		}
		Collections.sort(ret);
		return ret;
	}

	/**
	 * Outputs the whole schedule plan
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (JobListEnty jle : getJobList()) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(jle.toString());
		}
		return sb.toString();
	}

	/**
	 * This is called by the servlet and registers certain definitions
	 * 
	 * @param databasePath
	 * @param defs
	 * @throws SchedulerException
	 */
	public void registerJobGroup(String databasePath, final List<de.foconis.core.job.NSFJobGroup> defs, final String signer) {
		databasePath = XUtils.normalizeDbPath(databasePath);

		synchronized (this) {
			// we must wrap the scheduleDefinition without using any class instantiated from the NSF
			// (because the NSF-context is not running while building the schedule)
			registeredJobGroups.put(databasePath, JobGroup.wrap(databasePath, defs, signer));
			log_.warning("Database  " + databasePath + " registered with " + defs.size() + " job groups, Signer: " + signer);
			update();
		}
	}

	/**
	 * Remove all triggers and jobs for this DB
	 * 
	 * @param trigName
	 * @throws SchedulerException
	 */
	public void unRegisterJobGroup(String databasePath) {
		databasePath = XUtils.normalizeDbPath(databasePath);
		synchronized (this) {
			List<JobGroup> jobs = registeredJobGroups.remove(databasePath);

			if (jobs != null) {
				log_.warning("Database " + databasePath + " unregistered with " + jobs.size() + " job groups");
			}
			update();
		}
	}
}
