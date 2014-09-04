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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;

import de.foconis.core.http.client.HttpResponse;

/**
 * This is the response that is returned by the service request.
 * 
 * @author Roland Praml, FOCONIS AG
 * 
 */
public class FakeResponseAdapter implements HttpServletResponseAdapter, HttpResponse {

	private ServletOutputStream servletOutputStream;
	private int statusCode = 200;
	private String statusMessage = null;
	private List<Cookie> cookies = new ArrayList<Cookie>();
	private Map<String, String> headers = new HashMap<String, String>();
	private HttpServletRequestAdapter request;
	private String contentType = "text/html";
	private int contentLength = -1;
	private String characterEncoding = "UTF-8";
	private Locale locale;
	private int bufferSize;

	public FakeResponseAdapter() {
		super();
		setBufferSize(1024);
	}

	/**
	 * @param outputStream
	 */
	public FakeResponseAdapter(final HttpServletRequestAdapter request) {
		this();
		this.request = request;
	}

	/**
	 * add a Cookie to the response
	 */
	public void addCookie(final Cookie cookie) {
		cookies.add(cookie);
	}

	/**
	 * Formatter for header date values
	 * 
	 * @return
	 */
	private DateFormat getFormatter() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat;
	}

	/**
	 * add a date header
	 */
	public void addDateHeader(final String hdrName, final long value) {
		headers.put(hdrName, getFormatter().format(new Date(value)));
	}

	public void setDateHeader(final String hdrName, final long value) {
		headers.put(hdrName, getFormatter().format(new Date(value)));
	}

	/**
	 * add a normal header
	 */
	public void addHeader(final String hdrName, final String value) {
		headers.put(hdrName, value);
	}

	public void setHeader(final String hdrName, final String value) {
		headers.put(hdrName, value);
	}

	/**
	 * add a integer header
	 */
	public void addIntHeader(final String hdrName, final int value) {
		headers.put(hdrName, Integer.toString(value));
	}

	public void setIntHeader(final String hdrName, final int value) {
		headers.put(hdrName, Integer.toString(value));
	}

	/**
	 * checks if a header exists
	 */
	public boolean containsHeader(final String hdrName) {
		return headers.containsKey(hdrName);
	}

	/**
	 * Delegate to encodeRedirectURL
	 */
	public String encodeRedirectUrl(final String url) {
		return encodeRedirectURL(url);
	}

	/**
	 * Delegate to encodeURL
	 */
	public String encodeUrl(final String url) {
		return encodeURL(url);
	}

	/**
	 * redirect URLs are encoded, if the servername is in the URL
	 */
	public String encodeRedirectURL(final String url) {
		if ((this.request == null) || (url.indexOf(this.request.getServerName()) == -1)) {
			return url;
		} else {
			return encodeURL(url);
		}
	}

	/**
	 * encodes a URL and adds the sessionID if isRequestedSessionIdFromCookie is false
	 */
	public String encodeURL(final String url) {
		if ((this.request == null) || (this.request.isRequestedSessionIdFromCookie())) {
			return url;
		}
		String sessId = this.request.getRequestedSessionId();
		int i = url.indexOf('#');
		if ((sessId == null) || (i == 0)) {
			return url;
		} else if (url.indexOf('?') == -1) {
			if (i < 0) {
				return url + "?SessionID=" + sessId;
			} else {
				return url.substring(0, i) + "?SessionID=" + sessId + url.substring(i);
			}
		} else if (i < 0) {
			return url + "&SessionID=" + sessId;
		} else {
			return url.substring(0, i) + "&SessionID=" + sessId + url.substring(i);
		}
	}

	/**
	 * flushes the servletOutput-buffer
	 */
	public void flushBuffer() throws IOException {
		servletOutputStream.flush();
	}

	/**
	 * returns the buffersize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * sets and initializes the buffer
	 */
	public void setBufferSize(final int bufSize) {
		this.bufferSize = bufSize;
		servletOutputStream = new FakeServletOutputStream(new ByteArrayOutputStream(bufSize));
	}

	// Encoding / charset
	/**
	 * returns the character encoding (default UTF-8)
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * returns the content type (default text/html)
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * set the character encoding (e.g UTF-8)
	 */
	public void setCharacterEncoding(final String encoding) {
		this.characterEncoding = encoding;
		if (this.contentType != null) {
			setContentType(this.contentType + "; charset=" + this.characterEncoding);
		}
	}

	/**
	 * set the content-type (and maybe the charset: "text/html; charset=UTF-8")
	 */
	public void setContentType(final String contentType) {
		if (contentType == null) {
			return;
		}
		int i = contentType.indexOf(';');
		if (i < 0) {
			this.contentType = contentType;
		} else {
			this.contentType = contentType.substring(0, i);
			i = contentType.indexOf("charset=", i);
			if (i >= 0) {
				int j = contentType.indexOf(';', i);
				this.characterEncoding = contentType.substring(i + 8, j >= 0 ? j : contentType.length());
			}
		}
		setHeader("Content-Type", this.contentType + "; charset=" + this.characterEncoding);
	}

	/**
	 * returns the locale if set
	 */
	public Locale getLocale() {
		return this.locale;
	}

	/**
	 * sets the locale for the response
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * returns the outputstream that becomes the responseText
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		return this.servletOutputStream;
	}

	/**
	 * get a printwriter that respects encoding
	 */
	public PrintWriter getWriter() throws IOException {
		try {
			OutputStreamWriter osWriter;
			osWriter = new OutputStreamWriter(getOutputStream(), getCharacterEncoding());
			return new PrintWriter(osWriter);
		} catch (UnsupportedEncodingException e) {
			return new PrintWriter(getOutputStream());

		}
	}

	/**
	 * we do not really buffer, so we commit everything (that means also, reset and resetBuffer are unsupported)
	 */
	public boolean isCommitted() {
		return true;
	}

	public void reset() {
		// throw new UnsupportedOperationException();
	}

	public void resetBuffer() {
		throw new UnsupportedOperationException();
	}

	/**
	 * redirect is not supported
	 */
	public void sendRedirect(final String url) throws IOException {
		// setStatus(302);
		// setHeader("Location", toAbsolute(url));
		throw new UnsupportedOperationException("Redirects are not supported");
	}

	// content length
	public void setContentLength(final int contentLength) {
		this.contentLength = contentLength;
		setHeader("Content-Length", Integer.toString(this.contentLength));
	}

	/**
	 * set a error statuscode
	 */
	public void sendError(final int errNr, final String errMsg) throws IOException {
		setStatus(errNr, errMsg);
	}

	public void sendError(final int errNr) throws IOException {
		setStatus(errNr);
	}

	/**
	 * set a HTTP-Statuscode
	 */

	public void setStatus(final int statusCode, final String statusMessage) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}

	public void setStatus(final int statusCode) {
		setStatus(statusCode, null);
	}

	// -- HTTPResonse Interface
	public int getStatus() {
		return statusCode;
	}

	public String getStatusMessage() {
		if (this.statusMessage != null) {
			return this.statusMessage;
		}
		switch (this.statusCode) {
		// 100er Codes
		case HttpServletResponse.SC_CONTINUE:
			return "Continue";
		case HttpServletResponse.SC_SWITCHING_PROTOCOLS:
			return "Switching Protocols";
		case 102:
			return "Processing";

			// 200er Codes
		case HttpServletResponse.SC_OK:
			return "OK";
		case HttpServletResponse.SC_CREATED:
			return "Created";
		case HttpServletResponse.SC_ACCEPTED:
			return "Accepted";
		case HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION:
			return "Non-Authoritative Information";
		case HttpServletResponse.SC_NO_CONTENT:
			return "No Content";
		case HttpServletResponse.SC_RESET_CONTENT:
			return "Reset Content";
		case HttpServletResponse.SC_PARTIAL_CONTENT:
			return "Partial Content";
		case 207:
			return "Multi-Status";
		case 208:
			return "Already Reported";
		case 226:
			return "IM Used";

			// 300er Codes
		case HttpServletResponse.SC_MULTIPLE_CHOICES:
			return "Multiple Choices";
		case HttpServletResponse.SC_MOVED_PERMANENTLY:
			return "Moved Permanently";
		case HttpServletResponse.SC_FOUND:
			return "Found";
		case HttpServletResponse.SC_SEE_OTHER:
			return "See Other";
		case HttpServletResponse.SC_NOT_MODIFIED:
			return "Not Modified";
		case HttpServletResponse.SC_USE_PROXY:
			return "Use Proxy";
		case 306:
			return "(reserved)";
		case HttpServletResponse.SC_TEMPORARY_REDIRECT:
			return "Temporary Redirect";
		case 308:
			return "Permanent Redirect";

			// 400er Coder
		case HttpServletResponse.SC_BAD_REQUEST:
			return "Bad Request";
		case HttpServletResponse.SC_UNAUTHORIZED:
			return "Unauthorized";
		case HttpServletResponse.SC_PAYMENT_REQUIRED:
			return "Payment Required";
		case HttpServletResponse.SC_FORBIDDEN:
			return "Forbidden";
		case HttpServletResponse.SC_NOT_FOUND:
			return "Not Found";
		case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
			return "Method Not Allowed";
		case HttpServletResponse.SC_NOT_ACCEPTABLE:
			return "Not Acceptable";
		case HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
			return "Proxy Authentication Required";
		case HttpServletResponse.SC_REQUEST_TIMEOUT:
			return "Request Time-out";
		case HttpServletResponse.SC_CONFLICT:
			return "Conflict";
		case HttpServletResponse.SC_GONE:
			return "Gone";
		case HttpServletResponse.SC_LENGTH_REQUIRED:
			return "Length Required";
		case HttpServletResponse.SC_PRECONDITION_FAILED:
			return "Precondition Failed";
		case HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE:
			return "Request Entity Too Large";
		case HttpServletResponse.SC_REQUEST_URI_TOO_LONG:
			return "Request-URL Too Long";
		case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE:
			return "Unsupported Media Type";
		case HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE:
			return "Requested range not satisfiable";
		case HttpServletResponse.SC_EXPECTATION_FAILED:
			return "Expectation Failed";
		case 418:
			return "I’m a teapot";
		case 420:
			return "Policy Not Fulfilled";
		case 421:
			return "There are too many connections from your internet address";
		case 422:
			return "Unprocessable Entity";
		case 423:
			return "Locked";
		case 424:
			return "Failed Dependency";
		case 425:
			return "Unordered Collection";
		case 426:
			return "Upgrade Required";
		case 428:
			return "Precondition Required";
		case 429:
			return "Too Many Requests";
		case 431:
			return "Request Header Fields Too Large";
		case 451:
			return "Unavailable For Legal Reasons";

			// 500er Codes
		case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
			return " 	Internal Server Error";
		case HttpServletResponse.SC_NOT_IMPLEMENTED:
			return "Not Implemented";
		case HttpServletResponse.SC_BAD_GATEWAY:
			return "Bad Gateway";
		case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
			return "Service Unavailable";
		case HttpServletResponse.SC_GATEWAY_TIMEOUT:
			return "Gateway Time-out";
		case HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED:
			return "HTTP Version not supported";
		case 506:
			return "Variant Also Negotiates";
		case 507:
			return "Insufficient Storage";
		case 509:
			return "Bandwidth Limit Exceeded";
		case 510:
			return "Not Extended";

		}
		return "Unknwon statuscode: " + statusCode;
	}

	/**
	 * returns the cookies that are set while this request was performed
	 */
	@Override
	public List<Cookie> getCookies() {
		return cookies;
	}

	/**
	 * returns the headers that are set while this request was performed
	 */
	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public int getContentLength() {
		return this.contentLength;
	}

	@Override
	public String getResponseText() {
		return servletOutputStream.toString();
	}

}
