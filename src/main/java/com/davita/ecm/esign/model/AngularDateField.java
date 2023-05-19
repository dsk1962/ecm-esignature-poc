package com.davita.ecm.esign.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularDateField extends AngularInputField{
	public AngularDateField(){
		super();
		setSubType("date");
	}
    private Date minValue;
    private Date maxValue;

}
