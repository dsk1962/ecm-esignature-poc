package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularTextArea extends AngularTextField{
	public AngularTextArea(){
		super();
		setSubType("textarea");
	}
	private String rows;
	private String columns;
}
