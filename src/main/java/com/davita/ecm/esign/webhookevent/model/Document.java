package com.davita.ecm.esign.webhookevent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Document {
	private String id;
	private String label;
	private String numPages;
	private String mimeType;
	private String name;
}
