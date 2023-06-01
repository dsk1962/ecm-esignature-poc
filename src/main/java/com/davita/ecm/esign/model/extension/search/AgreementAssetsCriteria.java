package com.davita.ecm.esign.model.extension.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

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
	private Integer pageSize;
	private List<String> parentId;
	private List<String> participantEmail;
	private List<String> queryableFields;
	private List<String> role;
	private String sortByField;
	private String sortOrder;
	private Integer startIndex;
	private List<String> status;
	private List<String> subTypes;
	private List<String> type;
	private List<String> userId;
	private String visibility;
	
	public void addExternalId(String value) {
		if(StringUtils.hasText(value))
		{
			if(externalId == null)
				externalId = new ArrayList<>(3);
		}
		externalId.add(value);
	}
	public void addGroupId(String value) {
		if(StringUtils.hasText(value))
		{
			if(groupId == null)
				groupId = new ArrayList<>(3);
		}
		groupId.add(value);
	}
	public void addId(String value) {
		if(StringUtils.hasText(value))
		{
			if(id == null)
				id = new ArrayList<>(3);
		}
		id.add(value);
	}
	public void addParentId(String value) {
		if(StringUtils.hasText(value))
		{
			if(parentId == null)
				parentId = new ArrayList<>(3);
		}
		parentId.add(value);
	}
	public void addParticipantEmail(String value) {
		if(StringUtils.hasText(value))
		{
			if(participantEmail == null)
				participantEmail = new ArrayList<>(3);
		}
		participantEmail.add(value);
	}
	public void addQueryableField(String value) {
		if(StringUtils.hasText(value))
		{
			if(queryableFields == null)
				queryableFields = new ArrayList<>(3);
		}
		queryableFields.add(value);
	}
	public void addRole(String value) {
		if(StringUtils.hasText(value))
		{
			if(role == null)
				role = new ArrayList<>(3);
		}
		role.add(value);
	}
	public void addStatus(String value) {
		if(StringUtils.hasText(value))
		{
			if(status == null)
				status = new ArrayList<>(3);
		}
		status.add(value);
	}
	public void addSubType(String value) {
		if(StringUtils.hasText(value))
		{
			if(subTypes == null)
				subTypes = new ArrayList<>(3);
		}
		subTypes.add(value);
	}
	public void addType(String value) {
		if(StringUtils.hasText(value))
		{
			if(type == null)
				type = new ArrayList<>(3);
		}
		type.add(value);
	}
	public void addUserId(String value) {
		if(StringUtils.hasText(value))
		{
			if(userId == null)
				userId = new ArrayList<>(3);
		}
		userId.add(value);
	}
}
