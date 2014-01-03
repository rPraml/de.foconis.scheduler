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
package de.foconis.core.internal.schedule;

import java.util.Calendar;
import java.util.logging.Logger;

import de.foconis.core.schedule.Schedule;

/**
 * @author praml
 * 
 */
public abstract class AbstractSchedule implements Schedule {
	private static final Logger log_ = Logger.getLogger(AbstractSchedule.class.getName());
	private boolean[] weekdays = new boolean[7];

	private static String[] strDays = new String[] { "S", "M", "T", "W", "T", "F", "S" };
	boolean runsNever = true;
	boolean runsAlways = true;

	/**
	 * @param weekdays
	 */
	public AbstractSchedule(final boolean[] weekdays) {
		for (int i = 0; i < 7; i++) {
			this.weekdays[i] = weekdays[i];
			if (weekdays[i]) {
				runsNever = false;
			} else {
				runsAlways = false;
			}
		}
	}

	/**
	 * compute the next run based on last run (you need not to check against weekdays. this is done in getNextRunTime)
	 * 
	 * @param lastRun
	 * @return
	 */
	protected abstract Calendar computeNextRunTime(final Calendar lastRun);

	/**
	 * returns TRUE if the schedule can run at given date
	 */
	protected boolean allowedAt(final Calendar cal) {
		return weekdays[cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY];
	}

	/**
	 * Compute the nearest next run time when this agent can run
	 */
	public final Calendar getNextRunTime(final Calendar lastRun) {
		if (runsNever) {
			// disabled on all weekdays
			return null;
		}

		Calendar nextRunTime = computeNextRunTime(lastRun);
		int i = 0;
		while (nextRunTime != null && !allowedAt(nextRunTime)) {
			nextRunTime = computeNextRunTime(nextRunTime);
			if (i++ > 10000) {
				// we give up
				log_.severe("Could not find next schedule for " + this);
				return null;
			}
		}
		return nextRunTime;
	}

	/**
	 * returns a string when this agent can run
	 */
	public String getRunDays() {
		if (runsAlways) {
			return "ALLDAYS";
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 7; i++) {
				if (weekdays[i]) {
					sb.append(strDays[i]);
				} else {
					sb.append('_');
				}
			}
			return sb.toString();
		}
	}
}
