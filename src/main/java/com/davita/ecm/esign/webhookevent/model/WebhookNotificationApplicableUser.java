package com.davita.ecm.esign.webhookevent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebhookNotificationApplicableUser {
	private String id;
	private String email;
	private String role;
	private String payloadApplicable;
}

