package com.davita.ecm.esign.model.extension.workflow;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileInfo {

	private String name;
	private String label;
	private boolean required;
	private List<WorkflowLibraryDocumentSelectorList> workflowLibraryDocumentSelectorList;
}
