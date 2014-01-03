/**
 * 
 */
package de.foconis.osgi.services;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * This class is specified as com.ibm.xsp.adapter.serviceFactory to trigger a "autostart" on server start
 * 
 * @author praml RCPLoggerConfig
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