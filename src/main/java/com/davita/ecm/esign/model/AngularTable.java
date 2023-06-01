package com.davita.ecm.esign.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AngularTable extends AngularWidget {

	public AngularTable() {
		super();
		setType("table");
	}

	private List<AngularWidget> columns;
	@SuppressWarnings("rawtypes")
	private List data;

	public void addColumn(AngularWidget column) {
		if (column == null)
			return;
		if (columns == null)
			columns = new ArrayList<>();
		columns.add(column);
	}
}
