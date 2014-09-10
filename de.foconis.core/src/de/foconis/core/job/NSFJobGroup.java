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
package de.foconis.core.job;

import java.util.List;

import de.foconis.core.schedule.Schedule;

/**
 * For each scheduleDefinition a own class must be created in NSF!
 * 
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public interface NSFJobGroup {

	/**
	 * Returns the group-type to prevent multiple running jobs in same database Only ONE job per group-type will run in NSF
	 * 
	 * @return the group-type
	 */
	public String getGroupId();

	/**
	 * Returns the username where this group should run. If <code>null</code> returned, the servername is used
	 * 
	 * @return
	 */
	public String runOnBehalf();

	/**
	 * Return how long this group may run
	 * 
	 * @return
	 */
	public int getTimeout();

	/**
	 * return the schedule-plan when this jobGroup should run
	 * 
	 * @return
	 */
	public Schedule getSchedule();

	/**
	 * return a list of job groups to run
	 * 
	 * @return
	 */
	public List<NSFJobFactory> getJobFactores();

}
