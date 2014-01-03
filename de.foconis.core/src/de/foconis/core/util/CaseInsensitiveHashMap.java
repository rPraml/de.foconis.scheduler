/**
 * 
 */
package de.foconis.core.util;

import java.util.HashMap;

/**
 * Case insensitive Hashmap to maintain Notes-Fieldnames
 * 
 * @author praml
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
