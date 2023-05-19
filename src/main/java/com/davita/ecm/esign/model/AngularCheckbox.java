package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularCheckbox extends AngularInputField{
	public AngularCheckbox(){
		super();
		setSubType("checkbox");
	}
}
