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
import java.util.GregorianCalendar;

/**
 * @author praml
 * 
 */
public class PeriodicSchedule extends AbstractSchedule {
	private int interval;
	private int[] timeWindow = new int[2];

	/**
	 * @param interval
	 * @param timeWindow
	 * @param weekdays
	 */
	public PeriodicSchedule(final int interval, final int[] timeWindow, final boolean[] weekdays) {
		super(weekdays);
		this.interval = interval;
		this.timeWindow[0] = timeWindow[0];
		this.timeWindow[1] = timeWindow[1];
	}

	/**
	 * checks if we are in the correct time window
	 */
	@Override
	protected boolean allowedAt(final Calendar cal) {
		if (super.allowedAt(cal)) {
			int timestamp = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND);

			if (timeWindow[0] <= timestamp && timestamp <= timeWindow[1]) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Computes the next run time. (simple adds interval to lastRun)
	 */
	@Override
	public Calendar computeNextRunTime(final Calendar lastRun) {
		if (lastRun == null) {
			// if we never ran, we run NOW
			return new GregorianCalendar(); // we do not use Calendar.getInstance(), because we do not want to get a JapaneseCalendar or
											// sth. like this
		} else {
			lastRun.add(Calendar.SECOND, interval);
			return lastRun;
		}
	}

	@Override
	public String toString() {
		return String.format("Days: %s every %d seconds", getRunDays(), interval);
	}
}
