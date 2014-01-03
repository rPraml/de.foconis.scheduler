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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;

/**
 * This class is the response of a request that was done with the NSFServiceHttpClient
 * 
 * @author Roland Praml, Foconis AG
 * 
 */
public interface HttpResponse {
	/**
	 * @return a HTTP Status-code like 200 for OK
	 */
	public int getStatus();

	/**
	 * Readable message
	 * 
	 * @return a HTTP Status-message like OK
	 */
	public String getStatusMessage();

	/**
	 * @return all cookies that were set during this request
	 */
	public List<Cookie> getCookies();

	/**
	 * @return all headers that were set during this request
	 */
	public Map<String, String> getHeaders();

	/**
	 * @return the contentLength
	 */
	int getContentLength();

	/**
	 * @return the locale
	 */
	public Locale getLocale();

	/**
	 * This is the (HTML)-Output generated by the request
	 * 
	 * @return the response-text
	 */
	public String getResponseText();

	/**
	 * @return the content type
	 */
	public String getContentType();

	/**
	 * @return the character encoding
	 */
	public String getCharacterEncoding();

}
