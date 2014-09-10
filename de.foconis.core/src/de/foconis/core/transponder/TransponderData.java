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
package de.foconis.core.transponder;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public abstract class TransponderData implements Comparable<TransponderData> {

	public abstract String getAppId();

	public abstract String getVersion();

	public String getDbPath() {
		return null;
	}

	@Override
	public int compareTo(final TransponderData other) {
		// TODO Auto-generated method stub
		String s1 = getDbPath().replaceAll("(?i)foconis", "");
		String s2 = other.getDbPath().replaceAll("(?i)foconis", "");
		int i1 = s1.length();
		int i2 = s2.length();

		if (i1 == i2) {
			return s1.compareTo(s2);
		}
		return i2 - i1;
	}

	@Override
	public String toString() {
		return "TransponderData [getAppId()=" + getAppId() + ", getVersion()=" + getVersion() + ", getDbPath()=" + getDbPath() + "]";
	}

}
