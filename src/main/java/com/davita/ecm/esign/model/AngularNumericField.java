package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularNumericField extends AngularInputField{
	public AngularNumericField(){
		super();
		setSubType("numeric");
	}

    private Number minValue;
    private Number maxValue;

}
