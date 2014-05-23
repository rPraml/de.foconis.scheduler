package de.foconis.core.servlet;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lotus.domino.NotesException;
import lotus.domino.Session;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem.NSFResource;
import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.webapp.DesignerFacesServlet;

import de.foconis.core.internal.PatrolJob;
import de.foconis.core.internal.WorkerJob;
import de.foconis.core.job.NSFJob;
import de.foconis.core.job.NSFJobFactory;
import de.foconis.core.job.NSFJobGroup;
import de.foconis.core.services.Scope;
import de.foconis.core.services.ServiceLocator;
//import de.foconis.core.quartz.ScheduleDefinition;
//import de.foconis.core.services.Scope;
//import de.foconis.core.services.ServiceLocator;
import de.foconis.core.transponder.TransponderData;
import de.foconis.core.transponder.TransponderRegistry;

public class SchedulerServlet extends DesignerFacesServlet {
	private static final Logger log_ = Logger.getLogger(SchedulerServlet.class.getName());

	/**
	 * Wir führen unseren Code in der "serviceView" - Methode aus, da wir hier Zugriff auf alle notwendigen Variablen wie FacesContext +
	 * ApplicationContext haben
	 */
	@Override
	protected void serviceView(final FacesContext fc, final FacesController controller) {
		ServletRequest req = (ServletRequest) fc.getExternalContext().getRequest();
		ServletResponse res = (ServletResponse) fc.getExternalContext().getResponse();

		String action = req.getParameter("action");

		if ("invoke".equals(action)) {
			invoke(req, res);
		} else if ("register".equals(action)) {
			registerJobGroups(req, res);
			registerTransponder(req, res);
		}

	}

	/**
	 * Registers the definitions at the XPage Scheduler.
	 * 
	 * @param req
	 * @param res
	 * @throws SchedulerException
	 */
	private void registerJobGroups(final ServletRequest req, final ServletResponse res) {

		// the root of the definition is the META-INF/services file which MUST have a valid signature
		String clazzFile = "META-INF/services/" + TransponderData.class.getName();
		if (!setSigner(clazzFile))
			return;

		NotesContext ctx = NotesContext.getCurrent();
		NSFComponentModule module = ctx.getRunningModule();

		String signer = null;
		try {
			Session signerSession = ctx.getSessionAsSigner();
			signer = signerSession.getEffectiveUserName();
		} catch (NotesException e) {
		}
		// here wo do some security checks (I like signers!)
		if (signer == null) {
			log_.severe("!!! The file " + module.getDatabasePath() + "/" + clazzFile + " is not signed!");
			PatrolJob.getCurrentQueue().unRegisterJobGroup(module.getDatabasePath());

		} else {
			List<NSFJobGroup> defs = ServiceLocator.findServices(NSFJobGroup.class, Scope.NONE);
			// everything must be signed by the SAME user!!! - check it after loading all services
			if (ctx.getSessionAsSigner() == null) {
				log_.severe("!!! One or more classes listed in " + module.getDatabasePath() + "/" + clazzFile + " are not properly signed!");
				PatrolJob.getCurrentQueue().unRegisterJobGroup(module.getDatabasePath());
			} else {
				PatrolJob.getCurrentQueue().registerJobGroup(module.getDatabasePath(), defs, signer);
			}
		}
	}

	protected boolean setSigner(final String clazzFile) {
		NotesContext ctx = NotesContext.getCurrent();
		NSFComponentModule module = ctx.getRunningModule();
		RuntimeFileSystem fs = module.getRuntimeFileSystem();
		NSFResource res = fs.getResource(clazzFile);
		if ((res != null) && ((res instanceof RuntimeFileSystem.NSFFile))) {
			ctx.setSignerSessionRights(clazzFile);
			return true;
		}
		return false;
	}

	/**
	 * Registers the definitions at the XPage Scheduler.
	 * 
	 * @param req
	 * @param res
	 * @throws SchedulerException
	 */
	private void registerTransponder(final ServletRequest req, final ServletResponse res) {

		// the root of the definition is the META-INF/services file which MUST have a valid signature

		String clazzFile = "META-INF/services/" + TransponderData.class.getName();
		if (!setSigner(clazzFile))
			return;

		NotesContext ctx = NotesContext.getCurrent();
		NSFComponentModule module = ctx.getRunningModule();

		String signer = null;
		try {
			Session signerSession = ctx.getSessionAsSigner();
			signer = signerSession.getEffectiveUserName();
		} catch (NotesException e) {
		}
		// here wo do some security checks (I like signers!)
		if (signer == null) {
			log_.severe("!!! The file " + module.getDatabasePath() + "/" + clazzFile + " is not signed!");
			TransponderRegistry.unRegister(module.getDatabasePath());

		} else {
			List<TransponderData> defs = ServiceLocator.findServices(TransponderData.class, Scope.NONE);
			// everything must be signed by the SAME user!!! - check it after loading all services
			if (ctx.getSessionAsSigner() == null) {
				log_.severe("!!! One or more classes listed in " + module.getDatabasePath() + "/" + clazzFile + " are not properly signed!");
				TransponderRegistry.unRegister(module.getDatabasePath());
			} else {
				TransponderRegistry.register(module.getDatabasePath(), defs);

			}
		}
	}

	/**
	 * This method returns false if the classloader has loaded unsigned code
	 * 
	 * @return
	 */
	// private boolean checkIfSigned(final String signedBy) {
	// NotesContext ctx = NotesContext.getCurrent();
	// NSFComponentModule moduleCurrent = ctx.getModule();
	//
	// // there's a bug - if you getSessionAsSigner first, you cannot getSessionAsSignerFullAdmin()
	// Session signerSession = ctx.getSessionAsSignerFullAdmin();
	//
	// /* 376:376 */ ??? = "WEB-INF/classes/" + paramString.replace('.', '/') + ".class";
	// /* 377:377 */ localNotesContext = NotesContext.getCurrent();
	// /* 378:378 */ localNotesContext.setSignerSessionRights((String)???);
	//
	// if (signerSession == null) {
	// log_.severe("!!! The Database " + moduleCurrent.getDatabasePath() + " is not properly signed !!! Cannot continue ");
	// return false;
	// }
	//
	// try {
	// String effective = signerSession.getEffectiveUserName();
	// if (signedBy.equals(effective)) {
	// return true;
	// }
	// log_.severe("!!! The Database " + moduleCurrent.getDatabasePath() + " is not signed by " + signedBy + " !!! Cannot continue ");
	// } catch (NotesException e) {
	// }
	// return false;
	//
	// }

	/**
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws JobExecutionException
	 */
	protected void invoke(final ServletRequest req, final ServletResponse res) {

		String clazzName = req.getParameter(NSFJobGroup.class.getName());

		NotesContext ctx = NotesContext.getCurrent();
		NSFComponentModule moduleCurrent = ctx.getModule();

		WorkerJob wj = WorkerJob.getInstance();
		try {

			String clazzFile = "WEB-INF/classes/" + clazzName.replace('.', '/') + ".class";
			// ctx.setSignerSessionRights(clazzFile);

			Class<?> clazz = moduleCurrent.getModuleClassLoader().loadClass(clazzName);

			if (NSFJobGroup.class.isAssignableFrom(clazz)) {
				execScheduleDefinition((NSFJobGroup) clazz.newInstance(), wj);
			} else {
				throw new ClassCastException("Class '" + clazzName + "' is not a instance of '" + NSFJobGroup.class.getName() + "'");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log_.log(Level.SEVERE, "Error while executing jobGroup: " + clazzName, e);

		}

		// } catch (JobExecutionException jex) {
		// HttpRequestJob outerJob = HttpRequestJob.getInstance();
		// if (outerJob != null) {
		// outerJob.setJobExecutionException(jex);
		// }
		// throw new ServletException("Error while executing class: " + clazzName, jex);
		// } catch (Exception iex) {
		// throw new ServletException("Could not instantiate class: " + clazzName, iex);
		//

	}

	/**
	 * @param wj
	 * @param signer
	 * @param newInstance
	 * @throws InstantiationException
	 */
	private void execScheduleDefinition(final NSFJobGroup jobGroups, final WorkerJob wj) throws IllegalAccessException,
			InstantiationException {
		// TODO Auto-generated method stub

		if (wj == null) {
			throw new UnsupportedOperationException("Direct invoke not yet supported!");

		}
		NotesContext ctx = NotesContext.getCurrent();

		for (NSFJobFactory jobFactory : jobGroups.getJobFactores()) {
			if (wj != null) {
				if (!wj.isRunning()) {
					return;
				}
			}
			try {
				NSFJob job = jobFactory.createJob();
				if (wj != null) {
					wj.setInnerJob(job);
				}

				job.run();
			} catch (InterruptedException e) {
				// if IE is thrown, quit
				e.printStackTrace();
				return;

			} finally {
				if (wj != null) {
					wj.setInnerJob(null);
				}
			}
		}

	}
}