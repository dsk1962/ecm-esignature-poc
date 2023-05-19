package com.davita.ecm.esign.model.extension.workflow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordInfo {
	private boolean required;
	private String defaultValue;
	private boolean editable;
	private boolean visible;
}
