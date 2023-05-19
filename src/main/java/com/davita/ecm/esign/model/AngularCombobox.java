package com.davita.ecm.esign.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularCombobox extends AngularInputField {
	
	public AngularCombobox(){
		super();
		setSubType("combobox");
	}
	List<Option> options;
	private String optionLabel;
	private String optionValue;
	private String listName;

}
