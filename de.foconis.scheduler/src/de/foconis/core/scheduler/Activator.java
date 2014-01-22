/**
 * 
 */
package de.foconis.core.scheduler;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.openntf.domino.xsp.adapter.OsgiCommandProvider;
//import org.openntf.xpt.agents.master.XPageAgentManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.ibm.domino.xsp.module.nsf.NotesContext;

public class Activator extends Plugin {
	private static final Logger log_ = Logger.getLogger(Activator.class.getName());
	private static Activator instance;
	private ServiceRegistration consoleCommandService;

	public Activator() {
		instance = this;
		System.out.println(Activator.class.getPackage().getName() + " loaded. Use 'tell http xsp help' to see available commands");
	}

	/**
	 * Registers the AmgrCommandProvider to handle commands
	 * 
	 * @param bundleContext
	 */
	private void registerCommandProvider() {

		CommandProvider cp = new OsgiCommandProvider();
		Dictionary<String, Object> cpDictionary = new Hashtable<String, Object>(7);
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> bundleDictionary = getBundle().getHeaders();
		cpDictionary.put("service.vendor", bundleDictionary.get("Bundle-Vendor"));
		cpDictionary.put("service.ranking", new Integer(Integer.MIN_VALUE));
		cpDictionary.put("service.pid", getBundle().getBundleId() + "." + cp.getClass().getName());

		consoleCommandService = getBundle().getBundleContext().registerService(CommandProvider.class.getName(), cp, cpDictionary);
	}

	/*
	 * (non-Javadoc) LCDEnvironment
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		if (NotesContext.isClient()) {
			// do nothing on the client
			return;
		}
		super.start(bundleContext);
		try {
			registerCommandProvider();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		if (consoleCommandService != null) {
			consoleCommandService.unregister();
			consoleCommandService = null;
		}
		super.stop(bundleContext);
	}

	public static Activator getInstance() {
		return instance;
	}

}
