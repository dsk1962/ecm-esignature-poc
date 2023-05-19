package com.davita.ecm.esign.webhookevent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SupportingDocument {
	private String displayLabel;
	private String fieldName;
	private String id;
	private String mimeType;
	private String numPages;
}

