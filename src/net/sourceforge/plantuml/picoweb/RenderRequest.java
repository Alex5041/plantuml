package net.sourceforge.plantuml.picoweb;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * POJO of the json sent to "POST /render"
 */
public class RenderRequest {

	private final String[] options;

	private final String source;

	public RenderRequest(String[] options, String source) {
		this.options = options;
		this.source = source;
	}

	public String[] getOptions() {
		return options;
	}

	public String getSource() {
		return source;
	}

	public static RenderRequest fromJson(String json) {
		final JsonObject parsed = Json.parse(json).asObject();
		final String[] options;
		JsonValue option = parsed.get("options");
		if (option != null) {
			final JsonArray jsonArray = option.asArray();
			options = new String[jsonArray.size()];
			for (int i = 0; i < jsonArray.size(); i++) {
				options[i] = jsonArray.get(i).asString();
			}
		} else {
			options = new String[0];
		}

		return new RenderRequest(options, parsed.get("source").asString());
	}
}
