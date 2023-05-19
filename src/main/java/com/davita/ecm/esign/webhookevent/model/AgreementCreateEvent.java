package com.davita.ecm.esign.webhookevent.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AgreementCreateEvent {
	private String webhookId;
	private String webhookName;
	private String webhookNotificationId;
	private WebhookUrlInfo webhookUrlInfo;
	private String webhookScope;
	private ArrayList<WebhookNotificationApplicableUser> webhookNotificationApplicableUsers;
	private String event;
	private String subEvent;
	private String eventDate;
	private String eventResourceType;
	private String eventResourceParentType;
	private String eventResourceParentId;
	private String participantUserId;
	private String participantUserEmail;
	private String actingUserId;
	private String actingUserEmail;
	private String actingUserIpAddress;
	private String initiatingUserId;
	private String initiatingUserEmail;
	private Agreement agreement;
}
