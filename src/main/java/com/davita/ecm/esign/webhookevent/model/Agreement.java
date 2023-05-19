package com.davita.ecm.esign.webhookevent.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Agreement {
	private String id;
	private String name;
	private String signatureType;
	private String status;
	private ArrayList<Cc> ccs;
	private DeviceInfo deviceInfo;
	private String documentVisibilityEnabled;
	private String createdDate;
	private String expirationTime;
	private ExternalId externalId;
	private PostSignOption postSignOption;
	private String firstReminderDelay;
	private String locale;
	private String message;
	private String reminderFrequency;
	private String senderEmail;
	private VaultingInfo vaultingInfo;
	private String workflowId;
	private ParticipantSetsInfo participantSetsInfo;
	private DocumentsInfo documentsInfo;
}
