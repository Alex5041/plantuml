/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2023, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 *
 * If you like this project or if you find it useful, you can support us at:
 *
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 *
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 *
 */
package net.sourceforge.plantuml.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Tokens {

	private final List<String> keywords = new ArrayList<>();
	private final List<String> added = Arrays.asList("actor",
					"participant",
					"usecase",
					"class",
					"interface",
					"abstract",
					"enum",
					"component",
					"state",
					"object",
					"artifact",
					"folder",
					"rectangle",
					"node",
					"frame",
					"cloud",
					"database",
					"storage",
					"agent",
					"stack",
					"boundary",
					"control",
					"entity",
					"card",
					"file",
					"package",
					"queue",
					"archimate",
					"diamond",
					"detach",
					"@start",
					"@end",
					"also",
					"autonumber",
					"caption",
					"title",
					"newpage",
					"loop",
					"break",
					"critical",
					"note",
					"legend",
					"group",
					"left",
					"right",
					"link",
					"over",
					"activate",
					"deactivate",
					"destroy",
					"create",
					"footbox",
					"hide",
					"show",
					"skinparam",
					"skin",
					"bottom",
					"namespace",
					"page",
					"down",
					"else",
					"endif",
					"partition",
					"footer",
					"header",
					"center",
					"rotate",
					"return",
					"repeat",
					"start",
					"stop",
					"while",
					"endwhile",
					"fork",
					"again",
					"kill",
					"order",
					"mainframe",
					"across",
					"stereotype",
					"split",
					"style",
					"sprite",
					"exit",
					"include",
					"pragma",
					"undef",
					"ifdef",
					"endif",
					"ifndef",
					"else",
					"function",
					"procedure",
					"endfunction",
					"endprocedure",
					"unquoted",
					"return",
					"startsub",
					"endsub",
					"assert",
					"local",
					"!definelong",
					"!enddefinelong",
					"!define",
					"define",
					"alias",
					"shape",
					"label",
					"BackgroundColor",
					"Color",
					"color",
					"Entity",
					"ENTITY",
					"COLOR",
					"LARGE",
					"stereo",
					"AZURE",
					"Azure");

	public static void main(String[] args) {
		System.err.println("keywords=" + new Tokens().keywords.size());
		final Set<String> sorted = new TreeSet<>(new Tokens().keywords);
		for (String s : sorted) {
			System.err.println(s);
		}
	}

	public String compressUnicodeE000(String s) {
		for (int i = 0; i < keywords.size(); i++) {
			final char c = (char) ('\uE000' + i);
			s = s.replace(keywords.get(i), "" + c);
		}
		return s;
	}

	public String compressAscii128(String s) {
		for (int i = 0; i < keywords.size(); i++) {
			final char c = (char) (128 + i);
			s = s.replace(keywords.get(i), "" + c);
		}
		return s;
	}

	public Tokens() {
		for(String element:added){
			add(element);
		}
		// add("endif");
		// add("else");
		// add("return");
	}

	private void add(String string) {
		if (keywords.contains(string)) {
			System.err.println(string);
			throw new IllegalArgumentException(string);
		}
		if (string.length() <= 3) {
			System.err.println(string);
			throw new IllegalArgumentException(string);
		}
		if (!string.matches("[!@]?[A-Za-z]+")) {
			System.err.println(string);
			throw new IllegalArgumentException(string);
		}
		keywords.add(string);
		if (keywords.size() > 127) {
			System.err.println(string);
			throw new IllegalArgumentException();
		}
	}

}
