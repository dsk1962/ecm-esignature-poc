package com.davita.ecm.esign.webhookevent.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DocumentsInfo {
	private ArrayList<Document> documents;
	private ArrayList<SupportingDocument> supportingDocuments;
}

