package com.davita.ecm.esign.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.davita.ecm.esign.model.extension.librarydocument.Field;
import com.davita.ecm.esign.model.extension.librarydocument.FieldList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.agreements.ParticipantSetInfo.RoleEnum;

@Component
public class Utilities {
	
	@Autowired
	ObjectMapper objectMapper;

	public String toHtmlJsonString( Object object) {
		return objectMapper.convertValue(object, JsonNode.class).toPrettyString().replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;");
	}

	public String getStringValue(JsonNode json, String key) {
		return json.has(key) ? (json.get(key).isNull() ? null : json.get(key).asText()) : null;
	}

	public RoleEnum getTemplatePrticipantRole(FieldList fields, String participantName) {
		RoleEnum result = RoleEnum.FORM_FILLER;
		if (fields != null && !CollectionUtils.isEmpty(fields.getFields()) && participantName != null)
			for (Field field : fields.getFields()) {
				if ((participantName.equalsIgnoreCase(field.getAssignee())
						|| "EVERYONE".equalsIgnoreCase(field.getAssignee()))
						&& "SIGNATURE".equalsIgnoreCase(field.getInputType()))
					return RoleEnum.SIGNER;
			}
		return result;
	}

}
