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
package de.foconis.core.internal;

import java.util.Collection;
import java.util.List;

import lotus.domino.NotesException;
import lotus.domino.NotesThread;
import lotus.notes.addins.DominoServer;

/**
 * Methods in this class are not intedended to call from an XPage!
 * 
 * @author praml
 */
public class XUtils {

	/**
	 * Checks if this user may run on behalf agents
	 * 
	 * @param userName
	 * @return
	 */
	public static boolean mayRunOnBehalf(final String userName) {
		NotesThread.sinitThread();
		try {
			DominoServer srv = new DominoServer();
			@SuppressWarnings("unchecked")
			Collection<String> userNames = srv.getNamesList(userName);
			@SuppressWarnings("unchecked")
			List<String> allowOnBehalf = srv.lookupServerRecord(DominoServer.ITEM_ALLOW_ON_BEHALF);
			for (String name : userNames) {
				if (allowOnBehalf.contains(name)) {
					return true;
				}
			}
		} catch (NotesException e) {
		} finally {
			NotesThread.stermThread();
		}
		return false;
	}

	/**
	 * Checks if this user may run unrestricted operations
	 * 
	 * @param userName
	 * @return
	 */
	public static boolean mayRunUnrestricted(final String userName) {
		NotesThread.sinitThread();
		try {
			DominoServer srv = new DominoServer();
			@SuppressWarnings("unchecked")
			Collection<String> userNames = srv.getNamesList(userName);
			@SuppressWarnings("unchecked")
			List<String> allowOnBehalf = srv.lookupServerRecord(DominoServer.ITEM_ALLOW_UNRESTRICTED_LOTUSCRIPT);
			for (String name : userNames) {
				if (allowOnBehalf.contains(name)) {
					return true;
				}
			}
		} catch (NotesException e) {
		} finally {
			NotesThread.stermThread();
		}
		return false;
	}

	public static String normalizeDbPath(String databasePath) {
		databasePath = databasePath.replace('\\', '/').toLowerCase();
		if (!databasePath.startsWith("/")) {
			databasePath = "/".concat(databasePath);
		}
		return databasePath;
	}
}
