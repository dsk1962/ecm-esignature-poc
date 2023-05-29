package com.davita.ecm.esign.model.extension.search;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchRequest {
	private List<String> scope;
	private AgreementAssetsCriteria agreementAssetsCriteria;
	private String query;
}
