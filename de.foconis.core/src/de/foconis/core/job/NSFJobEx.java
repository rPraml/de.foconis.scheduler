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
 * Extended Job class. Supports additional data
 * 
 * @author Roland Praml, FOCONIS AG
 */
public abstract class NSFJobEx<T> extends NSFJob {
	private T jobData;

	/**
	 * @param jobData
	 *            the jobData to set
	 */
	@SuppressWarnings("unchecked")
	public void initJobData(final Object jobData) {
		if (this.jobData != null) {
			throw new IllegalStateException();
		}
		this.jobData = (T) jobData;
	}

	/**
	 * @return the jobData
	 */
	protected T getJobData() {
		return jobData;
	}

}
