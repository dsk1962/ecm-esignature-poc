package com.davita.ecm.esign.model.extension.workflow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgreementNameInfo {
	private boolean required;
	private String defaultValue;
	private boolean editable;
	private boolean visible;
}
