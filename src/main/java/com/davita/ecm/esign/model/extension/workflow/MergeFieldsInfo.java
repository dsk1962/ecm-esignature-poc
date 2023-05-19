package com.davita.ecm.esign.model.extension.workflow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MergeFieldsInfo {
	private String fieldName;
	private String displayName;
	private String defaultValue;
	private boolean editable;
	private boolean visible;
	private boolean required;
}
