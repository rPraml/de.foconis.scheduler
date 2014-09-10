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

import java.util.logging.Logger;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class DbInfo {
	private static final Logger log_ = Logger.getLogger(DbInfo.class.getName());
	private static final long serialVersionUID = 1L;
	private long designTimeStamp = 0;
	private boolean dirty;
	private boolean seen;
	private String databasePath;

	/**
	 * @param nsf
	 */
	public DbInfo(final String nsf) {
		// TODO Auto-generated constructor stub
		if (nsf.startsWith("/")) {
			databasePath = nsf;
		} else {
			databasePath = "/" + nsf;
		}
	}

	/**
	 * @return the designTimeStamp
	 */
	public long getDesignTimeStamp() {
		return designTimeStamp;
	}

	/**
	 * @param designTimeStamp
	 *            the designTimeStamp to set
	 */
	public void setDesignTimeStamp(final long designTimeStamp) {
		if (this.designTimeStamp != designTimeStamp) {
			dirty = true;
		}
		this.designTimeStamp = designTimeStamp;
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @param dirty
	 *            the dirty to set
	 */
	public void markClean() {
		this.dirty = false;
	}

	/**
	 * @return the seen
	 */
	public boolean isSeen() {
		return seen;
	}

	/**
	 * @param seen
	 *            the seen to set
	 */
	public void setSeen(final boolean seen) {
		this.seen = seen;
	}

	/**
	 * @return the databasePath
	 */
	public String getDatabasePath() {
		return databasePath;
	}
}
