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

import java.util.StringTokenizer;

/**
 * You should not inherit from this class in a NSF!
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public abstract class ScheduleBuilder<T extends ScheduleBuilder<?, ?>, S extends Schedule> {

	// 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday)
	protected boolean[] weekdays = { true, true, true, true, true, true, true };

	/**
	 * Enables or disables the agent on a certain weekday
	 * 
	 * @param dayOfWeek
	 *            use Calendar.SUNDAY .. Calendar.SATURDAY
	 * @return
	 */
	public T onWeekday(final int dayOfWeek, final boolean enable) {
		if (dayOfWeek < 0 || dayOfWeek > 7) {
			throw new IllegalArgumentException();
		}
		if (dayOfWeek == 7) {
			for (int i = 0; i < 7; i++) {
				weekdays[i] = enable;
			}
		} else {
			weekdays[dayOfWeek] = enable;
		}
		return builder();
	}

	/**
	 * Parses a String like "23:00" and converts it to seconds
	 * 
	 * @param s
	 * @return
	 */
	public static int parseTime(final String s) {
		int time[] = { 0, 0, 0 };

		StringTokenizer tok = new StringTokenizer(s, ":");
		for (int i = 0; i < 3; i++) {
			if (tok.hasMoreTokens()) {
				time[i] = Integer.parseInt(tok.nextToken());
			}
		}

		return time[0] * 3600 + time[1] * 60 + time[2];
	}

	/**
	 * return the builder casted to T
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private T builder() {
		return (T) this;
	}

	/**
	 * Build the schedule
	 * 
	 * @return
	 */
	public abstract S build();
}
