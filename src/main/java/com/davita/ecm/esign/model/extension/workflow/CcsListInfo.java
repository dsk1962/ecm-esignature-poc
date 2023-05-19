package com.davita.ecm.esign.model.extension.workflow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CcsListInfo {
	private String name;
	private String label;
	private boolean required;
	private boolean editable;
	private boolean visible;
	private int minListCount;
	private int maxListCount;
}
