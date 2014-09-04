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

import de.foconis.core.internal.schedule.PeriodicSchedule;

/**
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class PeriodicScheduleBuilder extends ScheduleBuilder<PeriodicScheduleBuilder, PeriodicSchedule> {

	// the start and stop window
	protected int[] timeWindow = { 0, 86400 };

	// the repeat interval in seconds
	private int interval;

	private PeriodicScheduleBuilder(final int interval) {
		this.interval = interval;
	}

	/**
	 * creates a new periodic schedule that will run every <code>interval</code> seconds
	 * 
	 * @param interval
	 *            specify seconds
	 * @return
	 */
	public static PeriodicScheduleBuilder newInterval(final int interval) {
		return new PeriodicScheduleBuilder(interval);
	}

	/**
	 * creates a new periodic schedule that will run every <code>interval</code> seconds
	 * 
	 * @param interval
	 *            specify a string, e.g "00:15" to run every 15 minutes
	 * @return
	 */
	public static PeriodicScheduleBuilder newInterval(final String interval) {
		return new PeriodicScheduleBuilder(ScheduleBuilder.parseTime(interval));
	}

	/**
	 * Specify a time window when this agent may run
	 * 
	 * @param start
	 * @param stop
	 * @return
	 */
	public PeriodicScheduleBuilder timeWindow(final int start, final int stop) {
		if (start > stop) {
			throw new IllegalArgumentException("Start time cannot be after stop time");
		}
		if (start < 0 || stop > 86400) {
			throw new IllegalArgumentException("Start and stop time must be between 0..86400");
		}
		timeWindow[0] = start;
		timeWindow[1] = stop;
		return this;
	}

	public PeriodicScheduleBuilder timeWindow(final String start, final String stop) {
		return timeWindow(parseTime(start), parseTime(stop));
	}

	/**
	 * Factory method
	 */
	@Override
	public PeriodicSchedule build() {
		return new PeriodicSchedule(interval, timeWindow, weekdays);
	}

}
