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
package de.foconis.core.job;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.NotesException;

import com.ibm.xsp.model.domino.DominoUtils;

/**
 * Da diese Klasse aus der NSF heraus definiert wird, düfen wir diese Instanzen nur verwenden, wenn wir im Kontext der NSF sind
 * 
 * @author praml
 * 
 */
public abstract class AbstractNSFJobGroup implements NSFJobGroup {

	private List<NSFJobFactory> jobFactories = new ArrayList<NSFJobFactory>();

	/**
	 * Es kann immer nur ein Agent pro Gruppe laufen
	 * 
	 * @throws NotesException
	 */
	@Override
	public String getGroupId() {
		try {
			return DominoUtils.getCurrentDatabase().getReplicaID() + ":" + getSubGroupId();
		} catch (NotesException e) {
			return null;
		}
	}

	public AbstractNSFJobGroup() {
		super();
		init();
	}

	/**
	 * Setup jour jobs here
	 */
	protected abstract void init();

	/**
	 * add a factory to the job list
	 * 
	 * @param jf
	 */
	protected void addFactory(final NSFJobFactory jf) {
		jobFactories.add(jf);
	}

	/**
	 * add a job without data
	 * 
	 * @param clazz
	 */
	protected void addJob(final Class<? extends NSFJob> clazz) {
		addFactory(new NSFJobFactory(clazz));
	}

	/**
	 * add a NSFJobEx with data
	 * 
	 * @param clazz
	 * @param jobData
	 */
	protected <T> void addJob(final Class<? extends NSFJobEx<T>> clazz, final T jobData) {
		addFactory(new NSFJobFactory(clazz, jobData));
	}

	/**
	 * @return
	 */
	protected String getSubGroupId() {
		return "MAIN-AGENT";
	}

	/**
	 * Das Timeout. Nach dieser Zeit wird der Agent terminiert (aktuell noch nicht implementiert!)
	 */
	@Override
	public int getTimeout() {
		return 0;
	}

	/**
	 * Liste der Jobs
	 */
	@Override
	public List<NSFJobFactory> getJobFactores() {
		return jobFactories;
	}

	/**
	 * Der Name unter dem die Requests ausgeführt werden (Default=Servername)
	 */
	@Override
	public String runOnBehalf() {
		return null; // no special name
	}

	@Override
	public String toString() {
		return "GID:" + getGroupId() + ", Class:" + getClass().getName() + " Schedule:" + getSchedule();
	}

}
