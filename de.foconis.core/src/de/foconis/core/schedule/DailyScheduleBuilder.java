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
package de.foconis.core.schedule;

import de.foconis.core.internal.schedule.DailySchedule;

/**
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class DailyScheduleBuilder extends ScheduleBuilder<DailyScheduleBuilder, DailySchedule> {
	// the repeat interval in seconds
	private int startTime;

	private DailyScheduleBuilder(final int startTime) {
		this.startTime = startTime;
	}

	/**
	 * creates a new periodic schedule that will run every <code>interval</code> seconds
	 * 
	 * @param interval
	 *            specify seconds
	 * @return
	 */
	public static DailyScheduleBuilder newAt(final int interval) {
		return new DailyScheduleBuilder(interval);
	}

	/**
	 * creates a new periodic schedule that will run every <code>interval</code> seconds
	 * 
	 * @param interval
	 *            specify a string, e.g "08:00" to run every 15 minutes
	 * @return
	 */
	public static DailyScheduleBuilder newAt(final String interval) {
		return new DailyScheduleBuilder(ScheduleBuilder.parseTime(interval));
	}

	/**
	 * Factory method
	 */
	@Override
	public DailySchedule build() {
		return new DailySchedule(startTime, weekdays);
	}
}
