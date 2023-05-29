package com.davita.ecm.esign.model.extension.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Status {
	 private int code;
	    private String message;
	    private String warnings;
}
