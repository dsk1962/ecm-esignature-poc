package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularHiddenInputField extends AngularInputField {
	public AngularHiddenInputField() {
		super();
		setSubType("hiddenfield");
	}
}
