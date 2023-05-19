package com.davita.ecm.esign.model;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularTextField extends AngularInputField {
	public AngularTextField() {
		super();
		setSubType("text");
	}

	private int maxLength;
	private int minLength;
	private String mask;
	private String pattern;
	private String patternError;

	public void setMask(String mask) {
		if (StringUtils.hasText(mask)) {
			this.mask = mask;
			setSubType("mask");
		} else
			setSubType("text");
	}
}
