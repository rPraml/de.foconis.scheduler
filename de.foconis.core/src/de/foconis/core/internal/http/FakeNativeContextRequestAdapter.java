/*
 * © Copyright FOCONIS AG, 2014
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

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import com.ibm.designer.runtime.domino.bootstrap.adapter.DominoHttpXspNativeContext;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.xsp.XSPNative;

/**
 * Extension for the FakeRequestAdapter. It really fakes the request, so that everything is executed in the correct user-context
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class FakeNativeContextRequestAdapter extends FakeRequestAdapter implements DominoHttpXspNativeContext {

	private long userListHandle = 0;
	private long userDbHandle = 0;
	private long serverDbHandle = 0;

	private Session serverSession = null;
	private Session xpageSession_ = null;
	private Database userDb = null;
	private Database serverDb = null;

	/**
	 * Initialize a new request
	 * 
	 * @param useHttps
	 *            true or false if we shound "fake" HTTPs
	 * @param method
	 *            GET or POST
	 * @param pathInfo
	 *            the relative path. e.g /path/to/db.nsf/xpage.xsp?query_string
	 * @param useXpageSession
	 * @param userName
	 *            the userName for which the request is done
	 * @param serverSession
	 *            the serverSession
	 */
	public FakeNativeContextRequestAdapter(final boolean useHttps, final String method, final String pathInfo, final String userName,
			final Session serverSession) {
		super(useHttps, method, pathInfo, userName);
		this.serverSession = serverSession;
	}

	/**
	 * Returns the effective username
	 * 
	 * @return
	 */
	@Override
	protected String getEffectiveUserName() {
		String s = super.getEffectiveUserName();
		if (s == null) {
			try {
				s = serverSession.getEffectiveUserName();
			} catch (NotesException e) {
				e.printStackTrace();
				return null;
			}
		}
		return s;
	}

	/**
	 * Returns the XPage session for the current user (if no one set, the server session is returned)
	 * 
	 * @return
	 * @throws NotesException
	 * @throws NException
	 */
	private Session getXPageSession() throws NotesException, NException {

		if (this.xpageSession_ == null) {
			this.xpageSession_ = XSPNative.createXPageSession(getEffectiveUserName(), getUserListHandle(), getEnforceAccess(), false);
			System.out.println("Created XPAge Session. Effecitve user: " + xpageSession_.getEffectiveUserName());
		}
		return xpageSession_;
	}

	// --------------------- DominoHttpXspNativeContext - related

	/**
	 * returns the handle to the server-db (opened in the context of the server)
	 */
	@Override
	public long getServerDBHandle() {
		if (serverDbHandle == 0) {
			try {
				serverDb = serverSession.getDatabase("", getDatabasePath());
				serverDbHandle = XSPNative.getDBHandle(serverDb);
			} catch (NotesException e) {
				e.printStackTrace();
			}

		}
		return serverDbHandle;
	}

	/**
	 * 
	 */
	@Override
	public long getUserDBHandle() {
		if (userDbHandle == 0) {
			try {
				userDb = getXPageSession().getDatabase("", getDatabasePath());
				userDbHandle = XSPNative.getDBHandle(userDb);
			} catch (NotesException e) {
				e.printStackTrace();
			} catch (NException e) {
				e.printStackTrace();
			}
		}
		return userDbHandle;
	}

	/**
	 * Returns a userList
	 */
	@Override
	public long getUserListHandle() {
		if (userListHandle == 0) {
			try {
				userListHandle = NotesUtil.createUserNameList(getRemoteUser());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return userListHandle;
	}

	/**
	 * Checks if the current user is permitted to access this server.
	 * 
	 * If you return FALSE - serveraccess is not checked - you can fake ANY name then
	 * 
	 * @return
	 */
	@Override
	public boolean getEnforceAccess() {
		return true;
	}

	/**
	 * as far as noone knows exactly what this stands for we return false here
	 * 
	 * @return
	 */
	@Override
	public boolean getPreviewServer() {
		return false;
	}

	/**
	 * Do not know what that is...
	 */
	@Override
	public Object getLsxbeSession(final String paramString) throws NotesException {
		return null;
	}

	/**
	 * we do not return server variables
	 */
	@Override
	public String getServerVariable(final String paramString) {
		return null;
	}

	/**
	 * This is called at last and recycles all objects created here
	 */
	@Override
	public void recycle() {
		try {
			if (serverDb != null) {
				serverDb.recycle();
			}
			if (userDb != null) {
				userDb.recycle();
			}
			if (xpageSession_ != null) {
				xpageSession_.recycle();
			}
			userListHandle = 0;
			userDbHandle = 0;
			serverDbHandle = 0;
		} catch (NotesException e) {
			e.printStackTrace();
		}
	}
}
