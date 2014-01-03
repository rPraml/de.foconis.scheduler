package de.foconis.core.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

public class ServletFactory implements IServletFactory {
	public static final String SERVLET_PATH = "/xsp/de.foconis.scheduler.servlet";

	private static final String SERVLET_WIDGET_CLASS = SchedulerServlet.class.getName();
	private static final String SERVLET_WIDGET_NAME = "Servlet to communicate with NSF jobs";
	public volatile Servlet servlet;
	private ComponentModule module;

	@Override
	public void init(final ComponentModule module) {
		this.module = module;
	}

	public ServletMatch getServletMatch(final String contextPath, final String path) throws ServletException {
		if (path.startsWith(SERVLET_PATH)) {
			int len = SERVLET_PATH.length(); // $NON-NLS-1$
			String servletPath = path.substring(0, len);
			String pathInfo = path.substring(len);
			return new ServletMatch(getServlet(), servletPath, pathInfo);
		}
		return null;
	}

	public Servlet getServlet() throws ServletException {
		if (servlet == null) {
			synchronized (this) {
				if (servlet == null) {
					servlet = module.createServlet(SERVLET_WIDGET_CLASS, SERVLET_WIDGET_NAME, null);
				}
			}
		}
		return servlet;
	}

}