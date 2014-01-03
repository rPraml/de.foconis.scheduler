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
package de.foconis.core.services;

/**
 * Typesafe enumeration for scopes
 * 
 * @author praml
 * 
 */
public class Scope implements Comparable<Scope> {
	public static final Scope APPLICATION = new Scope(1);
	public static final Scope SESSION = new Scope(2);
	public static final Scope REQUEST = new Scope(3);
	public static final Scope NONE = new Scope(4);

	int value;

	Scope(final int v) {
		value = v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Scope)) {
			return false;
		}
		Scope other = (Scope) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Scope scope) {
		int i = this.value;
		int j = scope.value;
		return i == j ? 0 : i < j ? -1 : 1;
	}

}
