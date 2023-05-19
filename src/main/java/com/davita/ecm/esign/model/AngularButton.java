package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularButton extends AngularWidget {
	public AngularButton() {
		super();
		setType("button");
	}

	private String buttonType = "button";
	private AngularMethodCall onclick;
}
