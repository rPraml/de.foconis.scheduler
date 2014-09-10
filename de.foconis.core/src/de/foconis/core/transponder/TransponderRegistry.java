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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public enum TransponderRegistry {
	;
	static Map<String, List<TransponderData>> dbMap = new HashMap<String, List<TransponderData>>();
	static Map<String, List<TransponderData>> idMap = new HashMap<String, List<TransponderData>>();

	public static void unRegister(final String databasePath) {
		synchronized (dbMap) {
			List<TransponderData> data = dbMap.remove(databasePath);
			if (data != null) {
				for (TransponderData transData : data) {
					notifyUnregister(databasePath, transData);
				}
			}
			updateIdMap();
		}
	}

	public static void register(final String databasePath, final List<TransponderData> defs) {
		synchronized (dbMap) {
			List<TransponderData> dataOrig = dbMap.get(databasePath);
			List<TransponderData> dataNew = new ArrayList<TransponderData>();

			for (TransponderData transData : defs) {
				dataNew.add(new TransponderDataImpl(transData, databasePath));
			}

			if (dataOrig == null) {
				dataOrig = new ArrayList<TransponderData>();
				dbMap.put(databasePath, dataOrig);
			}
			// remove old ones
			Iterator<TransponderData> it = dataOrig.iterator();
			while (it.hasNext()) {
				TransponderData transData = it.next();
				if (!dataNew.contains(transData)) {
					notifyUnregister(databasePath, transData);
					it.remove();
				}
			}
			// add new ones to dataOrig
			it = dataNew.iterator();
			while (it.hasNext()) {
				TransponderData transData = it.next();
				if (!dataOrig.contains(transData)) {
					notifyRegister(databasePath, transData);
					dataOrig.add(transData);
				}
			}

			updateIdMap();
		}
	}

	private static void updateIdMap() {
		// TODO Auto-generated method stub
		idMap.clear();
		for (List<TransponderData> transDataList : dbMap.values()) {
			for (TransponderData transData : transDataList) {
				List<TransponderData> lst = idMap.get(transData.getAppId());
				if (lst == null) {
					lst = new ArrayList<TransponderData>();
					idMap.put(transData.getAppId(), lst);
				}
				lst.add(transData);
			}
		}
		for (List<TransponderData> transDataList : idMap.values()) {
			Collections.sort(transDataList);
		}
	}

	private static void notifyUnregister(final String databasePath, final TransponderData transData) {
		System.out.println("Unregistering " + transData + " in " + databasePath);
	}

	private static void notifyRegister(final String databasePath, final TransponderData transData) {
		System.out.println("Registering " + transData + " in " + databasePath);
	}

	public static void update() {
		updateIdMap();
		// System.out.println("ID-Map");
		// System.out.println(idMap);
	}
}
