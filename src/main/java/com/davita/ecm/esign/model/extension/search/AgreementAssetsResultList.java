package com.davita.ecm.esign.model.extension.search;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgreementAssetsResultList {
	private List<String> role;
    private boolean hidden;
    private String groupId;
    private String externalId;
    private List<String> subTypes;
    private String type;
    private String userId;
    private String parentId;
    private List<ParticipantList> participantList;
    private Date createdDate;
    private Date modifiedDate;
    private String name;
    private String id;
    private String workflowId;
    private String status;
    private Date expirationDate;
}
