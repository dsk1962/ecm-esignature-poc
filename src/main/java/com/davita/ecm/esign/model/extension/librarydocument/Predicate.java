package com.davita.ecm.esign.model.extension.librarydocument;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Predicate {
	private String fieldName;
	private String operator;
	private String value;
	private int fieldLocationIndex;

}
