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
package de.foconis.core.util;

import java.util.HashMap;

/**
 * Case insensitive Hashmap to maintain Notes-Fieldnames
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class CaseInsensitiveHashMap<T> extends HashMap<String, T> {
	private static final long serialVersionUID = 1L;

	/**
	 * puts the element in the map. Key is converted to lowercase
	 * 
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public T put(final String key, final T value) {
		if (key == null)
			return super.put(null, value);
		return super.put(key.toLowerCase(), value);
	}

	/**
	 * get the element from the list. If the key is not of type String, return null
	 * 
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	@Override
	public T get(final Object key) {
		if (key == null) {
			return super.get(null);
		} else if (key instanceof String) {
			return super.get(((String) key).toLowerCase());
		} else {
			return null;
		}
	}

	/**
	 * check if the the element is the list. If the key is not of type String, return false
	 * 
	 * @see java.util.HashMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(final Object key) {
		if (key == null) {
			return super.containsKey(null);
		} else if (key instanceof String) {
			return super.containsKey(((String) key).toLowerCase());
		} else {
			return false;
		}
	}

	/**
	 * remove the key from the map
	 * 
	 * @see java.util.HashMap#containsKey(java.lang.Object)
	 */
	@Override
	public T remove(final Object key) {
		if (key == null) {
			return super.remove(null);
		}
		if (key instanceof String) {
			return super.remove(((String) key).toLowerCase());
		} else {
			return null;
		}
	}

}
