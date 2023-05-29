package com.davita.ecm.esign.model.extension.search;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgreementAssetsResults {
	 private Status status;
	    private int totalHits;
	    private SearchPageInfo searchPageInfo;
	    private ArrayList<AgreementAssetsResultList> agreementAssetsResultList;
}
