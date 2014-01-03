/**
 * 
 */
package de.foconis.core.services;

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.xsp.context.FacesContextEx;

/**
 * The ServiceLocator instantiates the class that is declared in a file located in "META-INF/services" For example you have the interface
 * "de.foconis.AppCtx" you will create the file "META-INF/services/de.foconis.AppCtx" with one single line pointing to the class that
 * implements this interface. ATTENTION: Services are cached application wide (for different users!)
 * 
 * @author praml
 * 
 */
public class ServiceLocator {

	/**
	 * Finds all services for the given class. Applicationcontext should be specified if you have it (performance) The service is cached
	 * application wide!
	 * 
	 * @param service
	 *            the serviceclass to find
	 * @param ctx
	 *            the context. Should be specified if possible. may be null;
	 * @return List of classinstances that implements serviceClass
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> findServices(final Class<T> serviceClass, final Scope scope, FacesContext ctx) {
		if (ctx == null) {
			ctx = (FacesContextEx) FacesContext.getCurrentInstance();
		}

		if (scope.equals(Scope.APPLICATION)) {
			return ((FacesContextEx) ctx).getApplicationEx().findServices(serviceClass.getName());
		}

		Map<String, List<Object>> scopeMap = null;
		List<T> services = null;
		String serviceName = serviceClass.getName();

		if (scope.equals(Scope.SESSION)) {
			scopeMap = ctx.getExternalContext().getSessionMap();
		} else if (scope.equals(Scope.REQUEST)) {
			scopeMap = ctx.getExternalContext().getRequestMap();
		}

		services = (List<T>) ExtensionManager.findApplicationServices(scopeMap, ctx.getContextClassLoader(), serviceName);

		return services;
	}

	/**
	 * Same as {@link #findServices(Class, FacesContext)}, but without context
	 */
	public static <T> List<T> findServices(final Class<T> service, final Scope scope) {
		return findServices(service, scope, null);
	}

	/**
	 * Finds the first service returned by {@link #findService(FacesContext, Class)}
	 * 
	 * @param ctx
	 *            the context. Should be specified if possible
	 * @param service
	 *            the serviceclass to find
	 * @see #findService(FacesContext, Class)
	 * @return classinstance that implements serviceClass
	 */
	public static <T> T findService(final Class<T> service, final Scope scope, final FacesContext ctx) {
		List<T> services = findServices(service, scope, ctx);
		if (services.isEmpty()) {
			return null;
		} else {
			return services.get(0);
		}
	}

	/**
	 * Same as {@link #findService(Class, FacesContext)}, but without context
	 */
	public static <T> T findService(final Class<T> service, Scope scope) {
		if (scope == null) {
			scope = Scope.SESSION; // this is the default scope
		}
		return findService(service, scope, null);
	}

	/**
	 * Same as {@link #findService(Class, FacesContext)}, but without context and scope
	 */
	public static <T> T findService(final Class<T> service) {
		return findService(service, null);
	}

}
