/*
 * © Copyright FOCONIS AG, 2013
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
package de.foconis.core.internal.http;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;

import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

/**
 * as far as I see, this class is totally dumb and does not anything useful than providing a ID: "_foc_amgr_id_"
 * 
 * @author praml
 * 
 */
@SuppressWarnings("deprecation")
public class FakeSessionAdapter implements HttpSessionAdapter {

	@Override
	public Object getAttribute(final String arg0) {
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Enumeration getAttributeNames() {
		return new Vector().elements();
	}

	@Override
	public void setAttribute(final String arg0, final Object arg1) {
	}

	// -- Timestamps
	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public void setMaxInactiveInterval(final int arg0) {
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public void removeAttribute(final String arg0) {
	}

	/**
	 * The ID (must be equal with nullRequest)
	 */
	@Override
	public String getId() {
		return "_foc_amgr_id_";
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	// -- Value handling
	@Override
	public Object getValue(final String arg0) {
		return null;
	}

	@Override
	public String[] getValueNames() {
		return null;
	}

	@Override
	public void putValue(final String arg0, final Object arg1) {

	}

	@Override
	public void removeValue(final String arg0) {
	}

	// -- Other
	@Override
	public void invalidate() {

	}

	@Override
	public boolean isNew() {
		return false;
	}

}
