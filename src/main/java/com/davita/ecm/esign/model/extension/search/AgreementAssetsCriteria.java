package com.davita.ecm.esign.model.extension.search;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgreementAssetsCriteria {
	private CreatedDate createdDate;
	private ExpirationDate expirationDate;
	private List<String> externalId;
	private List<String> groupId;
	private List<String> id;
	private ModifiedDate modifiedDate;
	private int pageSize;
	private List<String> parentId;
	private List<String> participantEmail;
	private List<String> queryableFields;
	private List<String> role;
	private String sortByField;
	private String sortOrder;
	private int startIndex;
	private List<String> status;
	private List<String> subTypes;
	private List<String> type;
	private List<String> userId;
	private String visibility;
}
