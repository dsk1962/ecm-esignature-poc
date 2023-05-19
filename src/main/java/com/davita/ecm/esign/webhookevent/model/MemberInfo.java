package com.davita.ecm.esign.webhookevent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberInfo {
	private String id;
	private String email;
	private String company;
	private String name;
	private String privateMessage;
	private String status;
}

