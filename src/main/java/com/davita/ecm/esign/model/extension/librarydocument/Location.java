package com.davita.ecm.esign.model.extension.librarydocument;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Location {
	private int pageNumber;
	private double top;
	private double left;
	private double width;
	private double height;
}
