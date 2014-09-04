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

/**
 * Jobdefinition defines a job class and it's job data T is the type of the jobData type
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class NSFJobFactory {
	private Class<? extends NSFJob> jobClass;
	private Object jobData = null;

	/**
	 * Create a jobfactory without job data
	 * 
	 * @param clazz
	 */
	public NSFJobFactory(final Class<? extends NSFJob> clazz) {
		jobClass = clazz;
	}

	/**
	 * Create a jobfactory with job data
	 * 
	 * @param clazz
	 * @param jobData
	 */
	public <T> NSFJobFactory(final Class<? extends NSFJobEx<T>> clazz, final T jobData) {
		jobClass = clazz;
		this.jobData = jobData;
	}

	/**
	 * @return the jobClass-Name
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public NSFJob createJob() throws IllegalAccessException, InstantiationException {
		NSFJob job = jobClass.newInstance();
		if (job instanceof NSFJobEx && jobData != null) {
			((NSFJobEx<?>) job).initJobData(jobData);
		}
		return job;
	}
}
