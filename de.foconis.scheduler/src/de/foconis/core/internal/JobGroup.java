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

import java.util.ArrayList;
import java.util.List;

import de.foconis.core.job.NSFJobGroup;
import de.foconis.core.schedule.Schedule;

/**
 * This represents a jobGroup outside a NSF. We must not use classes from NSF because they cannot be instantiated.
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class JobGroup {
	String databasePath;
	String className;
	private Schedule schedule;
	private String groupId;
	private String runOnBehalf;
	private String signedBy;

	/**
	 * Wraps a NSFJobGroup and returns a "safe" jobGroup to use in scheduler
	 * 
	 * @param signedBy
	 * 
	 * @param defs
	 */
	private JobGroup(final String databasePath, final NSFJobGroup def, final String signedBy) {
		this.databasePath = databasePath;
		this.className = def.getClass().getName();
		this.schedule = def.getSchedule();
		this.groupId = def.getGroupId();
		this.runOnBehalf = def.runOnBehalf();
		this.signedBy = signedBy;
	}

	/**
	 * @param defs
	 * @param signedBy
	 * @return
	 */
	public static List<JobGroup> wrap(final String database, final List<NSFJobGroup> defs, final String signedBy) {
		List<JobGroup> ret = new ArrayList<JobGroup>();
		for (NSFJobGroup def : defs) {
			ret.add(new JobGroup(database, def, signedBy));
		}
		return ret;
	}

	/**
	 * @return
	 */
	public Schedule getSchedule() {
		return schedule;
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	public String getKey() {
		return databasePath + ":" + className;
	}

	/**
	 * @return
	 */
	public String getDatabasePath() {
		return databasePath;
	}

	/**
	 * @return
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @return
	 */
	public String getRunOnBehalf() {
		return runOnBehalf;
	}

	public String getSignedBy() {
		// TODO Auto-generated method stub
		return signedBy;
	}

}
