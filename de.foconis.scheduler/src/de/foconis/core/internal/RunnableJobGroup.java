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

import java.util.Calendar;
import java.util.GregorianCalendar;

import de.foconis.core.scheduler.JobStatus;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class RunnableJobGroup implements Comparable<RunnableJobGroup> {
	private JobStatus jobStatus = JobStatus.SCHEDULED;
	private JobGroup delegate;
	private final Calendar onSchedule = new GregorianCalendar();
	private Calendar runningSince = null;

	/**
	 * @param sched
	 */
	public RunnableJobGroup(final JobGroup jobGroup) {
		this.delegate = jobGroup;
	}

	/**
	 * @return
	 */
	public JobStatus getStatus() {
		return jobStatus;
	}

	/**
	 * @return
	 */
	public Calendar getOnScheule() {
		return onSchedule;
	}

	/**
	 * @return
	 */
	public Calendar getRunningSince() {
		return runningSince;
	}

	/**
	 * 
	 */
	public void setStatus(final JobStatus newStatus) {
		// TODO Auto-generated method stub
		switch (newStatus) {
		case UNASSIGNED:
		case SCHEDULED:
			throw new IllegalArgumentException("Status cannot be set back to " + newStatus);
		case BLOCKED:
			if (jobStatus != JobStatus.SCHEDULED) {
				throw new IllegalArgumentException("Status cannot be set from " + jobStatus + " to " + newStatus);
			}
			jobStatus = newStatus;
			break;
		case RUNNING:
			if (jobStatus != JobStatus.SCHEDULED && jobStatus != JobStatus.BLOCKED) {
				throw new IllegalArgumentException("Status cannot be set from " + jobStatus + " to " + newStatus);
			}
			runningSince = new GregorianCalendar();
			jobStatus = newStatus;
			break;
		case COMPLETE:
			if (jobStatus != JobStatus.RUNNING) {
				throw new IllegalArgumentException("Status cannot be set from " + jobStatus + " to " + newStatus);
			}
			jobStatus = newStatus;
			break;

		}
	}

	/**
	 * Delegate methods
	 * 
	 * @return
	 */
	public String getKey() {
		return delegate.getKey();
	}

	/**
	 * @return
	 */
	public String getGroupId() {
		return delegate.getGroupId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final RunnableJobGroup o) {
		return onSchedule.compareTo(o.onSchedule);
	}

	/**
	 * @return
	 */
	public String getRunOnBehalf() {
		return delegate.getRunOnBehalf();
	}

	/**
	 * @return
	 */
	public String getSignedBy() {
		return delegate.getSignedBy();
	}

	/**
	 * @return
	 */
	public String getDatabasePath() {
		return delegate.getDatabasePath();
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return delegate.getClassName();
	}
}
