/**
 * 
 */
package de.foconis.osgi.services;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

import de.foconis.core.scheduler.XPageScheduler;

/**
 * @author praml
 * 
 */
public class SchedulerService extends HttpService {

	private static final Logger log_ = Logger.getLogger(SchedulerService.class.getName());
	private static final long serialVersionUID = 1L;

	/**
	 * @param arg0
	 */
	public SchedulerService(final LCDEnvironment arg0) {
		super(arg0);
		// XPageScheduler.getInstance().start(5);
		// System.out.println("Foconis Scheduler service started");
		// log_.info("Foconis Scheduler service started");
	}

	@Override
	public void destroyService() {
		XPageScheduler.getInstance().stop(System.out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.designer.runtime.domino.adapter.HttpService#doService(java.lang.String, java.lang.String,
	 * com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter,
	 * com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter,
	 * com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter)
	 */
	@Override
	public boolean doService(final String arg0, final String arg1, final HttpSessionAdapter arg2, final HttpServletRequestAdapter arg3,
			final HttpServletResponseAdapter arg4) throws ServletException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getPriority() {
		return 50;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.designer.runtime.domino.adapter.HttpService#getModules(java.util.List)
	 */
	@Override
	public void getModules(final List<ComponentModule> paramList) {

	}

}
