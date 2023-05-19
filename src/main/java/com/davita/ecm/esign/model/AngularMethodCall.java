package com.davita.ecm.esign.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AngularMethodCall {
	private String member;
	private String method;
	private List<String> params;
	
	public void addParameter(String value)
	{
		if(params == null)
			params = new ArrayList<>(3);
		params.add(value);
	}
}
