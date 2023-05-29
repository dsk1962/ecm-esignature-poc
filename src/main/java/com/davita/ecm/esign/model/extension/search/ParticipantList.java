package com.davita.ecm.esign.model.extension.search;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantList {
	private List<String> role;
	private String fullName;
	private List<String> participantSetNames;
	private String company;
	private String email;
}
