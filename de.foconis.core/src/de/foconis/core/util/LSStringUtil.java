/*
 * © Copyright Foconis AG, 2013
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
package de.foconis.core.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Heinz Fink, FOCONIS AG
 * 
 */
public class LSStringUtil {

	/**
	 * Liefert die Anzahl der angegebenen Zeichen von rechts
	 * 
	 * @param str
	 *            String
	 * @param pos
	 *            die Anzahl der zu liefernden Zeichen
	 * @return String
	 */
	public static String right(final String str, final int pos) {
		if (pos <= 0)
			return "";
		return str.substring(str.length() - Math.min(pos, str.length()));
	}

	/**
	 * Liefert die Zeichen nach dem letzten Vorkommen vom Suchstring
	 * 
	 * @param str
	 *            String
	 * @param needle
	 *            der Suchstring
	 * @return String
	 */
	public static String strRight(final String str, final String needle) {
		int pos = str.indexOf(needle);
		if (pos < 0)
			return "";
		return str.substring(pos + needle.length());
	}

	public static boolean isEmpty(final String s) {
		return (s == null) || (s.length() == 0);
	}

	public static boolean isEmptyTrim(final String s) {
		if ((s == null) || (s.length() == 0))
			return true;
		return s.trim().length() == 0;
	}

	public static boolean isEmpty(final List<String> l) {
		return (l == null) || ((l.size() == 1) && isEmpty(l.get(0)));
	}

	/**
	 * Liefert die Anzahl der angegebenen Zeichen von rechts
	 * 
	 * @param str
	 *            String
	 * @param pos
	 *            die Anzahl der zu liefernden Zeichen
	 * @return String
	 */
	public static String rightBack(final String str, final int pos) {
		if (pos <= 0)
			return "";
		return str.substring(Math.min(pos, str.length()));
	}

	/**
	 * Liefert die Zeichen nach dem letzten Vorkommen vom Suchstring
	 * 
	 * @param str
	 *            String
	 * @param needle
	 *            der Suchstring
	 * @return String
	 */
	public static String strRightBack(final String str, final String needle) {
		int pos = str.lastIndexOf(needle);
		if (pos < 0)
			return "";
		return str.substring(pos + needle.length());
	}

	/**
	 * Liefert die Anzahl der angegebenen Zeichen von links
	 * 
	 * @param str
	 *            String
	 * @param pos
	 *            die Anzahl der zu liefernden Zeichen
	 * @return String
	 */
	public static String left(final String str, final int pos) {
		if (pos <= 0)
			return "";
		return str.substring(0, Math.min(pos, str.length()));
	}

	/**
	 * Liefert alle Zeichen vor dem ersten Vorkommen vom Suchsting
	 * 
	 * @param str
	 * @param needle
	 *            der Suchstring
	 * @return String
	 */
	public static String strLeft(final String str, final String needle) {
		int pos = str.indexOf(needle);
		if (pos < 0)
			return "";
		return str.substring(0, pos);
	}

	/**
	 * Liefert die Anzahl der angegebenen Zeichen von links
	 * 
	 * @param str
	 *            String
	 * @param pos
	 *            die Anzahl der zu liefernden Zeichen
	 * @return String
	 */
	public static String leftBack(final String str, final int pos) {
		if (pos <= 0)
			return "";
		return str.substring(0, str.length() - Math.min(pos, str.length()));
	}

	/**
	 * Liefert alle Zeichen vor dem ersten Vorkommen vom Suchsting
	 * 
	 * @param str
	 * @param needle
	 *            der Suchstring
	 * @return String
	 */
	public static String strLeftBack(final String str, final String needle) {
		int pos = str.lastIndexOf(needle);
		if (pos < 0)
			return "";
		return str.substring(0, pos);
	}

	/**
	 * Führt eine Ersetzung analog zur LS-Funktion replace durch
	 * 
	 * @param source
	 *            Der String, in dem teile ersetzt werden sollen
	 * @param search
	 *            Liste mit Suchbgriffen
	 * @param repl
	 *            Liste mit Ersatzbegriffen
	 * @return String mit Ersetzugen
	 */
	public static String replace(final String source, final List<String> search, final List<String> repl) {
		String result = source;
		for (int i = 0; i < search.size(); ++i) {
			if (i < repl.size()) {
				result = result.replace(search.get(i), repl.get(i));
			} else {
				result = result.replace(search.get(i), repl.get(repl.size() - 1));
			}
		}
		return result;
	}

	/**
	 * Führt eine Ersetzung analog zur LS-Funktion replace durch
	 * 
	 * @param source
	 *            Der String, in dem teile ersetzt werden sollen
	 * @param search
	 *            Liste mit Suchbgriffen
	 * @param repl
	 *            String mit Ersatzbegriff
	 * @return String mit Ersetzugen
	 */
	public static String replace(final String source, final List<String> search, final String repl) {
		String result = source;
		for (String srch : search) {
			result = result.replace(srch, repl);
		}
		return result;
	}

	/**
	 * Führt eine Ersetzung analog zur LS-Funktion replace durch
	 * 
	 * @param source
	 *            Liste mit Strings, in der Teile ersetzt werden sollen
	 * @param search
	 *            Liste mit Suchbgriffen
	 * @param repl
	 *            Liste mit Ersatzbegriffen
	 */
	public static void replace(final List<String> source, final List<String> search, final List<String> repl) {
		for (int i = 0; i < source.size(); ++i) {
			source.set(i, replace(source.get(i), search, repl));
		}
	}

	/**
	 * Konvertiert einen Vergleichsstring gemäß LotusScript-Syntax in ein regexp-Pattern. Der Vergleich erfolgt dann mit pattern.
	 * 
	 * @param lsLike
	 *            Zu konvertierendes Ls-Pattern
	 * @return Kompiliertes Pattern.
	 */
	public static Pattern lsLike2Pattern(final String lsLike) {
		String s;
		s = lsLike.replace("*", ".*");
		s = s.replace('?', '.');
		s = s.replace("#", "\\d");
		s = s.replace("[!", "[^");
		return Pattern.compile(s);
	}

	/**
	 * Führt einen Like-vergleich analog zum LotusScript-Befehl 'like' durch: if str like pat then -> if ( FocString.like(str,pat) )
	 * 
	 * @param str
	 *            Zu prüfender String
	 * @param pat
	 *            Vergleichspattern, darf * ? # [...] etc. enthalten
	 * @return true, wenn str like pat
	 */
	public static boolean like(final String str, final String pat) {
		return lsLike2Pattern(pat).matcher(str).matches();
	}

	public static String surround(String str, final String prefix, final String suffix, final boolean enable) {
		if (enable) {
			if (!str.startsWith(prefix))
				str = prefix + str;
			if (!str.endsWith(suffix))
				str = str + suffix;
		} else {
			if (str.startsWith(prefix))
				str = str.substring(prefix.length());
			if (str.endsWith(suffix))
				str = strLeftBack(str, suffix);
		}
		return str;
	}

	public static String surround(String str, final char prefix, final char suffix, final boolean enable) {
		if (enable) {
			if (str.charAt(0) != prefix)
				str = prefix + str;
			if (str.charAt(str.length() - 1) != suffix)
				str = str + suffix;
		} else {
			if (str.charAt(0) == prefix)
				str = str.substring(1);
			if (str.charAt(str.length() - 1) == suffix)
				str = str.substring(0, str.length() - 2);
		}
		return str;
	}

	public static String xmlEncode(final String text) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if (!((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9'))) {
				result.append("&#" + (int) currentChar + ";");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

}
