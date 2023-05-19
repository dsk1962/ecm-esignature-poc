package com.davita.ecm.esign.model.extension.workflow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipientsListInfo {

	private String name;
	private String label;
	private String defaultValue;
	private int minListCount;
	private int maxListCount;
	private boolean editable;
	private boolean visible;
	private boolean allowfax;
	private boolean allowSender;
	private String authenticationMethod;
	private String role;
}
