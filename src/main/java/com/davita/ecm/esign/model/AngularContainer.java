package com.davita.ecm.esign.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularContainer extends AngularWidget {

	public AngularContainer() {
		super();
		setType("container");
	}

	private String layout;
	private String html;
	private List<AngularWidget> children;

	public void addChild(AngularWidget child) {
		if (children == null)
			children = new ArrayList<>();
		children.add(child);
	}
}
