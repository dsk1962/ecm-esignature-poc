package com.davita.ecm.esign.model.extension.librarydocument;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConditionalAction {
	private String anyOrAll;
	private String action;
	private List<Predicate> predicates;
}
