package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularInputField extends AngularWidget {
	public AngularInputField() {
		super();
		setType("inputfield");
	}

	private String id;
	private boolean required;
	private String placeholder;
	private String tooltip;
	private Boolean disabled;
	private Boolean readonly;
	private Object value;
	/**
	 * The field 'initValue' is used only on UI side 
	 */
	private Object initValue;
	Object[] validators;
}
