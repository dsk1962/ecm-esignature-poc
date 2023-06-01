package com.davita.ecm.esign.model.extension.search;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchRequest {
	private List<String> scope = new ArrayList<>(5);
	private AgreementAssetsCriteria agreementAssetsCriteria;
	private String query;

}
