package com.davita.ecm.esign.model;

import com.fasterxml.jackson.databind.JsonNode;

public class Utilities {
	private Utilities() {
	}

	public static final String getStringValue(JsonNode json, String key) {
		return json.has(key) ? (json.get(key).isNull() ? null : json.get(key).asText()) : null;
	}
}
