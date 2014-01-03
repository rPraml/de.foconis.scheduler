/*
 * © Copyright FOCONIS AG, 2013
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
package de.foconis.core.internal.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;

import lotus.domino.NotesException;
import lotus.notes.addins.DominoServer;

import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;

/**
 * This is a requestAdapter for the default session (everything is done in the server's context with more rights)
 * 
 * @author praml
 * 
 */
public class FakeRequestAdapter implements HttpServletRequestAdapter {

	private String pathInfo;
	private boolean useHttps;
	private String userName;
	private Cookie[] cookies;
	private String method;

	private Map<String, String> headers = new HashMap<String, String>();
	private Vector<String> headerNames = new Vector<String>();
	private Hashtable<String, String[]> params = new Hashtable<String, String[]>();

	private Principal userPrincipal_ = null;

	/**
	 * Initialize a new request
	 * 
	 * @param useHttps
	 *            true or false if we shound "fake" HTTPs
	 * @param method
	 *            GET or POST
	 * @param pathInfo
	 *            the relative path. e.g /path/to/db.nsf/xpage.xsp?query_string
	 * @param useXpageSession
	 * @param userPrincipal
	 *            the userPrinicipal for which the request is done
	 * @param xSession
	 */
	public FakeRequestAdapter(final boolean useHttps, final String method, final String pathInfo, final String userName) {
		this.pathInfo = pathInfo;
		this.userName = userName;
		this.useHttps = useHttps;
		this.method = method;
	}

	/**
	 * returns the database path of the requested nsf
	 * 
	 * @return
	 */
	protected String getDatabasePath() {
		int pos = pathInfo.toLowerCase().indexOf(".nsf");
		if (pos < 0) {
			return null;
		} else {
			return pathInfo.substring(1, pos + 4);
		}
	}

	/**
	 * Add a header to the request
	 * 
	 * @param headerName
	 * @param headerValue
	 */
	public void addHeader(final String headerName, final String headerValue) {
		this.headerNames.add(headerName);
		this.headers.put(headerName.toLowerCase(Locale.ENGLISH), headerValue);
	}

	/**
	 * add a Map with headers
	 * 
	 * @param headers
	 */
	public void addHeaders(final Map<String, String> headers) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			addHeader(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * add a parameter to the get/post data
	 * 
	 * @param name
	 * @param value
	 */
	public void addParameter(final String name, final String value) {
		String[] paramAr = params.get(name);
		String[] toAdd;
		if (paramAr != null) {
			toAdd = new String[paramAr.length + 1];
			for (int j = 0; j < paramAr.length; j++) {
				toAdd[j] = paramAr[j];
			}
			toAdd[paramAr.length] = value;
		} else {
			toAdd = new String[] { value };
		}
		params.put(name, toAdd);
	}

	/**
	 * set a list of cookies
	 * 
	 * @param cookies
	 */
	public void setCookies(final List<Cookie> cookies) {
		this.cookies = cookies.toArray(this.cookies);
	}

	/**
	 * the servlet path. As fas as I know, this is always ""
	 */
	@Override
	public String getServletPath() {
		return "";
	}

	/**
	 * the context path. As fas as I know, this is always ""
	 */
	public String getContextPath() {
		return "";
	}

	/**
	 * This is normally the PATH_INFO variable of the server. Must be set in constructor (whole URL with query string)
	 */
	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	/**
	 * Returns the absolute path to the invoked page
	 */
	@Override
	public String getPathTranslated() {
		int i = pathInfo.indexOf('?');
		if (i < 0) {
			return pathInfo;
		} else {
			return pathInfo.substring(0, i - 1);
		}
	}

	public Principal getUserPrincipal2() {
		if (this.userPrincipal_ == null) {
			userPrincipal_ = new Principal() {
				@Override
				public String getName() {
					return userName;
				}

			};

		}
		return this.userPrincipal_;
	}

	/**
	 * Returns the effective username
	 * 
	 * @return
	 */
	protected String getEffectiveUserName() {
		return userName;
	}

	/**
	 * this returns the userPrincipal for which this request was invoked
	 */
	@Override
	public Principal getUserPrincipal() {
		try {
			if (this.userPrincipal_ == null) {
				DominoServer srv = new DominoServer();
				@SuppressWarnings("unchecked")
				Collection<String> names = (Collection<String>) srv.getNamesList(getEffectiveUserName());
				userPrincipal_ = new com.ibm.domino.xsp.bridge.http.servlet.DominoAuthPrincipal(names.toArray(new String[0]));

			}
			return this.userPrincipal_;
		} catch (NotesException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * this returns the userName for which this request was invoked
	 */
	@Override
	public String getRemoteUser() {
		return getUserPrincipal().getName();
	}

	/**
	 * returns the complete uri that was called (uri was not really called, because we are a fake call)
	 */
	@Override
	public String getRequestURI() {
		StringBuffer sb = new StringBuffer();
		if (this.getServletPath() != null) {
			sb.append(this.getServletPath());
		}
		if (this.getContextPath() != null) {
			sb.append(this.getContextPath());
		}
		if (this.pathInfo != null) {
			sb.append(this.pathInfo);
		}
		return sb.toString();
	}

	/**
	 * returns the complete url that was called (uri was not really called, because we are a fake call)
	 */
	@Override
	public StringBuffer getRequestURL() {
		StringBuffer sb = new StringBuffer();
		sb.append(getScheme());
		sb.append("://");
		sb.append(getServerName());
		sb.append(":");
		sb.append(getServerPort());
		sb.append(getRequestURI());
		return sb;
	}

	/**
	 * We do not handle roles
	 */
	@Override
	public boolean isUserInRole(final String paramString) {
		// TODO FOCONIS !!!
		return false;
	}

	/**
	 * this has to return the same session-ID as FakeSessionAdapter
	 */
	@Override
	public String getRequestedSessionId() {
		return "_foc_amgr_id_";
	}

	/**
	 * returns the __xspconvid header
	 * 
	 * @return
	 */
	@Override
	public String getConversationId() {
		return getHeader("__xspconvid");
	}

	// -- where comes the request from
	/**
	 * due the fact that the session ID is constant, we say that it comes from a cookie (and we do not need to add a sessionID to all links)
	 */
	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

	/**
	 * do we fake a HTTPs request? (Note HTTPs handshake is done by the server)
	 */
	@Override
	public boolean isSecure() {
		return useHttps;
	}

	/**
	 * returns how we were authenticated (We say "Basic")
	 */
	@Override
	public String getAuthType() {
		return "Basic";
	}

	/**
	 * returns the cookies you set in constructor
	 */
	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	// -- headers

	/**
	 * returns all headers
	 */
	@Override
	public Enumeration<String> getHeaderNames() {
		return headerNames.elements();
	}

	/**
	 * return all headers of a given name (our implementation supports only one header per name!)
	 */
	@Override
	public Enumeration<String> getHeaders(final String headerName) {
		Vector<String> ret = new Vector<String>(2);
		String header = getHeader(headerName);
		if (header != null)
			ret.add(header);
		return ret.elements();
	}

	/**
	 * read and parse a date Header
	 */
	@SuppressWarnings("deprecation")
	@Override
	public long getDateHeader(final String headerName) {
		String header = getHeader(headerName);
		if (header == null)
			return -1;
		try {
			return Date.parse(header);
		} catch (IllegalArgumentException localIllegalArgumentException) {
		}
		return -1;
	}

	/**
	 * returns the header value
	 */
	@Override
	public String getHeader(final String headerName) {
		if (headerName == null)
			return null;
		return headers.get(headerName.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * read and parse a int Header
	 */
	@Override
	public int getIntHeader(final String headerName) {
		String header = getHeader(headerName);
		if (header == null)
			return -1;
		try {
			return Integer.parseInt(header);
		} catch (NumberFormatException e) {
		}
		return -1;
	}

	// -- HTTP related
	/**
	 * returns GET or POST
	 */
	@Override
	public String getMethod() {
		return method;
	}

	/**
	 * returns the Protocol. This is always HTTP/1.1
	 */
	@Override
	public String getProtocol() {
		return "HTTP/1.1";
	}

	/**
	 * returns the query string (the part that comes after the ? in the url)
	 */
	@Override
	public String getQueryString() {
		int i = pathInfo.indexOf('?');
		if (i < 0)
			return "";
		return pathInfo.substring(i + 1);
	}

	/**
	 * returns the real path (Think it is ok if we return the parameter
	 */
	@Override
	public String getRealPath(final String path) {
		return path;
	}

	// -- content related
	@Override
	public int getContentLength() {
		return -1;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return "UTF-8";
	}

	@Override
	public String getScheme() {
		if (isSecure())
			return "https";
		return "http";
	}

	// -- TCP/IP

	@Override
	public Object getRequestDispatcher(final String paramString) {
		return null;
	}

	// -- Locale

	@Override
	public Locale getLocale() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getLocales() {
		return new Vector().elements();
	}

	// -- Parameter handling

	/**
	 * returns a GET/POST Parameter
	 * 
	 */
	@Override
	public String getParameter(final String name) {
		String[] paramAr = (String[]) this.params.get(name);
		if (paramAr == null) {
			return null;
		} else if (paramAr.length == 1) {
			return paramAr[0];
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < paramAr.length; i++) {
				if (i > 0)
					sb.append(",");
				sb.append(paramAr[i]);
			}
			return sb.toString();
		}
	}

	/**
	 * Return all parameters
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		return this.params;
	}

	/**
	 * return the parameter names that are available
	 */
	@Override
	public Enumeration<String> getParameterNames() {
		return this.params.keys();
	}

	/**
	 * return the parameter values as array
	 */
	@Override
	public String[] getParameterValues(final String name) {
		return this.params.get(name);
	}

	/**
	 * sets a userprincipal. The principal is unchangeable here!
	 */
	@Override
	public void setUserPrincipal(final Principal paramPrincipal) {
		throw new UnsupportedOperationException();
	}

	/**
	 * sets the character encoding. The encoding is unchangeable here!
	 */
	public void setCharacterEncoding(final String encoding) throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	// --- remote: the "client"
	/**
	 * the remote address that did the request
	 */
	@Override
	public String getRemoteAddr() {
		return "127.0.0.1";
	}

	/**
	 * the remote host that did the request
	 */
	@Override
	public String getRemoteHost() {
		return "localhost";
	}

	/**
	 * the remote port that did the request
	 */
	@Override
	public int getRemotePort() {
		return 0;
	}

	// --- local: the "server"
	@Override
	public String getLocalAddr() {
		return "127.0.0.1";
	}

	@Override
	public String getLocalName() {
		return getServerName();
	}

	@Override
	public int getLocalPort() {
		return getServerPort();
	}

	// --- the server confic
	@Override
	public String getServerName() {
		return "localhost";
	}

	@Override
	public int getServerPort() {
		if (isSecure())
			return 443;
		return 80;
	}

	/**
	 * returns the reader for the post data. This is not available on fake request
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * returns the reader for the post data. This is not available on fake request
	 */

	@Override
	public ServletInputStream getInputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void recycle() {
		// nothing to recycle here, no domino objects created
	}
}
