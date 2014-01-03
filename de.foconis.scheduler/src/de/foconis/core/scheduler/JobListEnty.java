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

import java.util.Calendar;

import de.foconis.core.internal.RunnableJobGroup;
import de.foconis.core.schedule.Schedule;

/**
 * @author praml
 * 
 */
public class JobListEnty implements Comparable<JobListEnty> {
	private String databasePath;
	private String className;
	private Schedule schedule;
	private Calendar lastRun;
	private Calendar nextRun;
	private JobStatus status = JobStatus.UNASSIGNED;
	private Calendar onSchedule;
	private Calendar runningSince;

	protected String formatCalender(final Calendar cal) {
		if (cal == null)
			return "-";
		return cal.getTime().toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DB: ");
		sb.append(databasePath);

		sb.append(" Class: ");
		sb.append(className);

		sb.append("\n  Rule: ");
		sb.append(schedule);

		sb.append("\n  Last run: ");
		sb.append(formatCalender(lastRun));

		sb.append("\n  Next run: ");
		sb.append(formatCalender(nextRun));

		switch (status) {
		case UNASSIGNED:
			break;
		case SCHEDULED:
			sb.append("\n  Waiting for execution since: ");
			sb.append(formatCalender(onSchedule));
			break;
		case RUNNING:
			sb.append("\n  On schedule since: ");
			sb.append(formatCalender(onSchedule));
			sb.append(" and running since: ");
			sb.append(formatCalender(runningSince));
			break;
		case COMPLETE:
			break;
		default:
			break;

		}
		return sb.toString();
	}

	/**
	 * @param databasePath
	 * @param className
	 * @param schedule
	 * @param calendar
	 * @param calendar2
	 */
	public JobListEnty(final String databasePath, final String className, final Schedule schedule, final Calendar lastRun,
			final Calendar nextRun, final RunnableJobGroup runDef) {
		this.databasePath = databasePath;
		this.className = className;
		this.schedule = schedule;
		this.lastRun = lastRun;
		this.nextRun = nextRun;

		if (runDef != null) {
			this.status = runDef.getStatus();
			this.onSchedule = runDef.getOnScheule();
			this.runningSince = runDef.getRunningSince();
		}
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final JobListEnty other) {
		return nextRun.compareTo(other.nextRun);
	}
}
