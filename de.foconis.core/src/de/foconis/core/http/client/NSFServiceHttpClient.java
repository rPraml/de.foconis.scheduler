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
package de.foconis.core.http.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

import de.foconis.core.internal.http.FakeNativeContextRequestAdapter;
import de.foconis.core.internal.http.FakeRequestAdapter;
import de.foconis.core.internal.http.FakeResponseAdapter;
import de.foconis.core.internal.http.FakeSessionAdapter;

/**
 * This class performs a "pseudo" HTTP request. I.e. the request is done directly against the NSFService (that handles also HTTP request
 * made against the HTTP-Task).
 * 
 * Of course you can instanttiate the Classes you want to invoke directly from the NSFClassloader. But doing a request over the NSFService
 * has the advantage that we can invoke a servlet which does a lot of work for us:
 * <ul>
 * <li>setting up everything properly, like FacesContext, ClassLoader</li>
 * <li>locks and refreshes the RuntimeVFS if it is neccessary</li>
 * <li>calls certain listeners that are responsible for setting up other plugins (like OpenNTF-Domino API)
 * </ul>
 * 
 * The XPT from www.webgate.biz (from which I got some ideas) does this in a similar way. But they connect via TCP/IP Socket to the
 * HTTP-Task. Here I see problems if the HTTP task is not listening on Port 80 or if some firewall settings block the connection. The next
 * thing is, that you need HTTP credentials saved somewhere which may be a potential security risk.
 * 
 * So that's why I do the whole stuff to perform a fake request
 * 
 * @author Roland Praml, Foconis AG
 * 
 */

public class NSFServiceHttpClient {
	// private HttpService service;
	private String userName;
	private List<Cookie> cookies = null;
	private List<HttpService> services;

	/**
	 * Creates a new HTTP Client on the default serive, userName = server
	 */
	public NSFServiceHttpClient() {
		this(null);
	}

	/**
	 * Creates a new HTTP Client n the default serive for a given userName
	 */
	public NSFServiceHttpClient(final String userName) {
		// this(null, userName);
		LCDEnvironment env = LCDEnvironment.getInstance();
		this.services = env.getServices();
	}

	/**
	 * This method simulates a GET Request on an NSF-HttpService with a username
	 * 
	 * @param service
	 * @param userName
	 * @param path
	 */
	public HttpResponse doRequest(final String method, String path, final Map<String, String[]> params) {
		String contextPath = "";
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		// Thread must be initialized, because FakeRequestAdapter needs this!
		NotesThread.sinitThread();
		FakeRequestAdapter httpRequest;
		try {
			Session session = NotesFactory.createSession();
			HttpSessionAdapter httpSession = new FakeSessionAdapter();

			if (userName == null) {
				// do it in the context of the server
				httpRequest = new FakeRequestAdapter(true, method, path, session.getEffectiveUserName());
			} else {
				httpRequest = new FakeNativeContextRequestAdapter(true, method, path, userName, session);
			}

			// cookies from the last request
			if (cookies != null) {
				httpRequest.setCookies(cookies);
			}

			// add the parameters
			if (params != null) {
				for (Map.Entry<String, String[]> entry : params.entrySet()) {
					String[] value = entry.getValue();
					if (value != null) {
						for (int k = 0; k < value.length; ++k) {
							httpRequest.addParameter(entry.getKey(), value[k]);
						}
					}
				}
			}
			FakeResponseAdapter httpResponse = new FakeResponseAdapter(httpRequest);

			try {
				for (HttpService service : this.services) {
					if (service.doService(contextPath, path, httpSession, httpRequest, httpResponse)) {
						break;
					}
				}
			} catch (Exception e) {
				httpResponse = new FakeResponseAdapter(httpRequest);

				try {
					e.printStackTrace();
					PrintWriter writer = httpResponse.getWriter();
					e.printStackTrace(writer);
					writer.close();
					httpResponse.sendError(500, e.getMessage());
				} catch (IOException ioEx) {
					ioEx.printStackTrace();
				}
				return httpResponse;
			} finally {
				// the request creates internally some notes objects. They must be recycled
				httpRequest.recycle();
				session.recycle();
			}

			cookies = httpResponse.getCookies();
			return httpResponse;
		} catch (NotesException e) {
			e.printStackTrace();
			return null;
		} finally {
			NotesThread.stermThread();
		}
	}
}
