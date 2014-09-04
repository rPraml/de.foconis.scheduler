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
package de.foconis.osgi.services;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * This class is specified as com.ibm.xsp.adapter.serviceFactory to trigger a "autostart" on server start.
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class SchedulerServiceFactory implements IServiceFactory {

	public SchedulerServiceFactory() {

	}

	public HttpService[] getServices(final LCDEnvironment paramLCDEnvironment) {
		HttpService[] ret = new HttpService[1];
		ret[0] = new SchedulerService(paramLCDEnvironment);
		return ret;

	}
}