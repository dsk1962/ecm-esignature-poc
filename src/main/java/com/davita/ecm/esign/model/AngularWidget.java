package com.davita.ecm.esign.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AngularWidget {
	private String id;
	private String type;
	private String label;
	private String name;
	private String style;
	private String labelStyle;
	private String classNames;
	private String subType;
	private Object config;
}
