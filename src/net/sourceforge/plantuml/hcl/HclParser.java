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
package net.sourceforge.plantuml.hcl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class HclParser {

	private final List<HclTerm> terms = new ArrayList<>();

	public HclParser(Iterable<Character> source) {
		parse(source.iterator());
	}

	public JsonObject parseMe() {
		final Map<String, JsonObject> map = new LinkedHashMap<>();
		final Iterator<HclTerm> it = terms.iterator();
		while (it.hasNext()) {
			map.putAll(getModuleOrSomething(it));
		}

		if (map.size() == 1)
			return map.values().iterator().next();

		final JsonObject result = new JsonObject();
		for (Entry<String, JsonObject> ent : map.entrySet())
			result.add(ent.getKey(), ent.getValue());

		return result;
	}

	private Map<String, JsonObject> getModuleOrSomething(Iterator<HclTerm> it) {
		final StringBuilder name = new StringBuilder();
		while (true) {
			final HclTerm current = it.next();
			if (current.is(SymbolType.STRING_QUOTED))
				name.append("\"").append(current.getData()).append("\" ");
			else if (current.is(SymbolType.STRING_SIMPLE))
				name.append(current.getData()).append(" ");
			else if (current.is(SymbolType.CURLY_BRACKET_OPEN)) {
				return Collections.singletonMap(name.toString().trim(), getBracketData(it));
			} else
				throw new IllegalStateException(current.toString());
		}
	}

	private JsonValue getFunctionData(String functionName, Iterator<HclTerm> it) {
		final JsonArray args = new JsonArray();
		if (!it.next().is(SymbolType.PARENTHESIS_OPEN))
			throw new IllegalStateException();

		while (true) {
			final Object value = getValue(it);
			if (value instanceof HclTerm && ((HclTerm) value).is(SymbolType.PARENTHESIS_CLOSE)) {
				if (args.isEmpty())
					return Json.value(functionName + "()");
				final JsonObject result = new JsonObject();
				result.add(functionName + "()", args);
				return result;
			}
			if (value instanceof HclTerm && ((HclTerm) value).is(SymbolType.COMMA))
				continue;

			if (value instanceof String) {
				args.add((String) value);
				continue;
			}


			if (!(value instanceof JsonValue)) throw new AssertionError();

			JsonValue jvalue = (JsonValue) value;

			if (jvalue.isArray())
				args.add(jvalue.asArray());
			else if (jvalue.isObject())
				args.add(jvalue.asObject());
			else if (jvalue.isString())
				args.add(jvalue.asString());
			else
				throw new IllegalStateException();

		}
	}

	private JsonObject getBracketData(Iterator<HclTerm> it) {
		final JsonObject result = new JsonObject();
		while (true) {
			final HclTerm current = it.next();
			if (current.is(SymbolType.CURLY_BRACKET_CLOSE))
				return result;
			if (current.is(SymbolType.STRING_SIMPLE) || current.is(SymbolType.STRING_QUOTED)) {
				final String fieldName = current.getData();
				final HclTerm next = it.next();
				if (!next.is(SymbolType.EQUALS, SymbolType.TWO_POINTS))
					throw new IllegalStateException(current.toString());
				final Object value = getValue(it);
				if (value instanceof String) {
					result.add(fieldName, (String) value);
					continue;
				}

				if (!(value instanceof JsonValue)) throw new AssertionError();

				JsonValue jvalue = (JsonValue) value;

				if (jvalue.isArray())
					result.add(fieldName,jvalue.asArray());
				else if (jvalue.isObject())
					result.add(fieldName,jvalue.asObject());
				else if (jvalue.isString())
					result.add(fieldName,jvalue.asString());
				else
					throw new IllegalStateException();

			} else
				throw new IllegalStateException(current.toString());
		}
	}

	private Object getValue(Iterator<HclTerm> it) {
		final HclTerm current = it.next();
		switch (current.getType()) {
			case COMMA:
				if (current.is(SymbolType.COMMA, SymbolType.PARENTHESIS_CLOSE)) {
					return current;
				}
			case STRING_QUOTED:
			case STRING_SIMPLE:
				return current.getData();
			case SQUARE_BRACKET_OPEN:
				return getArray(it);
			case CURLY_BRACKET_OPEN:
				return getBracketData(it);
			case FUNCTION_NAME:
				return getFunctionData(current.getData(), it);
			default:
				throw new IllegalStateException(current.toString());
		}
	}

	private Object getArray(Iterator<HclTerm> it) {
		final JsonArray result = new JsonArray();
		while (true) {
			final HclTerm current = it.next();
			if (current.is(SymbolType.CURLY_BRACKET_OPEN))
				result.add(getBracketData(it));
			if (current.is(SymbolType.SQUARE_BRACKET_CLOSE))
				return result;
			if (current.is(SymbolType.COMMA))
				continue;
			if (current.is(SymbolType.STRING_QUOTED))
				result.add(current.getData());
		}
	}

	@Override
	public String toString() {
		return terms.toString();
	}

	private void parse(Iterator<Character> it) {
		final StringBuilder pendingString = new StringBuilder();
		while (it.hasNext()) {
			final char c = it.next();
			final SymbolType type = getType(c);
			if (type == SymbolType.PARENTHESIS_OPEN) {
				if (pendingString.length() == 0)
					throw new IllegalArgumentException();
				terms.add(new HclTerm(SymbolType.FUNCTION_NAME, pendingString.toString()));
				pendingString.setLength(0);
			} else if (type != null && pendingString.length() > 0) {
				terms.add(new HclTerm(SymbolType.STRING_SIMPLE, pendingString.toString()));
				pendingString.setLength(0);
			}

			if (type == SymbolType.SPACE)
				continue;

			if (type != null) {
				terms.add(new HclTerm(type));
				continue;
			}
			if (c == '\"') {
				final String s = eatUntilDoubleQuote(it);
				terms.add(new HclTerm(SymbolType.STRING_QUOTED, s));
				continue;
			}
			pendingString.append(c);
		}
	}

	private String eatUntilDoubleQuote(Iterator<Character> it) {
		final StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			final char c = it.next();
			if (c == '\\') {
				sb.append(it.next());
				continue;
			}
			if (c == '\"')
				return sb.toString();
			sb.append(c);
		}
		return sb.toString();
	}

	private SymbolType getType(final char c) {
		switch (c) {
			case ' ':
				return SymbolType.SPACE;
			case '{':
				return SymbolType.CURLY_BRACKET_OPEN;
			case '}':
				return SymbolType.CURLY_BRACKET_CLOSE;
			case '[':
				return SymbolType.SQUARE_BRACKET_OPEN;
			case ']':
				return SymbolType.SQUARE_BRACKET_CLOSE;
			case '(':
				return SymbolType.PARENTHESIS_OPEN;
			case ')':
				return SymbolType.PARENTHESIS_CLOSE;
			case '=':
				return SymbolType.EQUALS;
			case ',':
				return SymbolType.COMMA;
			case ':':
				return SymbolType.TWO_POINTS;
			default:
				return null;
		}
	}

}
