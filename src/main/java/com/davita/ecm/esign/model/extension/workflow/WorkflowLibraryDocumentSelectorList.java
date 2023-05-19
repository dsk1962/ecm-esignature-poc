package com.davita.ecm.esign.model.extension.workflow;

import java.util.List;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkflowLibraryDocumentSelectorList {

	private String workflowLibDoc;
	private String label;
	private String sharingMode;
	private List<String> templateTypes;
	private Date modifiedDate;
}
