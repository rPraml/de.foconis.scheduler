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
public class DailySchedule extends AbstractSchedule {
	private int hour;
	private int minute;
	private int second;

	/**
	 * read when this schedule fires.
	 * 
	 * @param startTime
	 * @param weekdays
	 */
	public DailySchedule(int startTime, final boolean[] weekdays) {
		super(weekdays);
		if (startTime < 0 || startTime >= 86400) {
			throw new IllegalArgumentException("Starttime must be between 0 and 86400 seconds!");
		}
		this.second = startTime % 60;
		startTime = startTime / 60;
		this.minute = startTime % 60;
		startTime = startTime / 60;
		this.hour = startTime;

	}

	/**
	 * compute the next run time.
	 */
	@Override
	public Calendar computeNextRunTime(final Calendar lastRun) {
		Calendar nextRun = null;

		if (lastRun == null) {
			// if we never ran, we run TODAY at this time:
			nextRun = new GregorianCalendar();
			nextRun.set(Calendar.HOUR_OF_DAY, hour);
			nextRun.set(Calendar.MINUTE, minute);
			nextRun.set(Calendar.SECOND, second);
		} else {
			nextRun = (Calendar) lastRun.clone();
			nextRun.set(Calendar.HOUR_OF_DAY, hour);
			nextRun.set(Calendar.MINUTE, minute);
			nextRun.set(Calendar.SECOND, second);
			if (nextRun.before(lastRun)) {
				nextRun.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		return lastRun;
	}

	@Override
	public String toString() {
		return String.format("Days: %s at %02d:%02d:%02d", getRunDays(), hour, minute, second);
	}
}
