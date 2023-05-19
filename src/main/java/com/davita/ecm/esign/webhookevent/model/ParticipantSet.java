package com.davita.ecm.esign.webhookevent.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ParticipantSet {
	private ArrayList<MemberInfo> memberInfos;
	private String order;
	private String role;
	private String status;
	private String id;
	private String name;
	private String privateMessage;
}

