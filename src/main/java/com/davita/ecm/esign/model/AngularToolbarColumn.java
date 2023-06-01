package com.davita.ecm.esign.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularToolbarColumn extends AngularWidget {

	public AngularToolbarColumn() {
		super();
		setType("toolbarcolumn");
	}

	private List<AngularButton> buttons;

	public void addButton(AngularButton button) {
		if (buttons == null)
			buttons = new ArrayList<>();
		buttons.add(button);
	}
}
