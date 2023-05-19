package com.davita.ecm.esign.model.extension.librarydocument;

import java.util.List;

import com.davita.ecm.esign.webhookevent.model.Location;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Field {

	public static final String TEXT_FIELD = "TEXT_FIELD";
	public static final String MULTILINE = "MULTILINE";
	public static final String PASSWORD = "PASSWORD";
	public static final String RADIO = "RADIO";
	public static final String CHECKBOX = "CHECKBOX";
	public static final String DROP_DOWN = "DROP_DOWN";
	public static final String LISTBOX = "LISTBOX";

	public static final String VALIDATION_NONE = "NONE";
	public static final String VALIDATION_STRING = "STRING";
	public static final String VALIDATION_NUMBER = "NUMBER";
	public static final String VALIDATION_DATE = "DATE";
	public static final String VALIDATION_DATE_CUSTOM = "DATE_CUSTOM";
	public static final String VALIDATION_TIME = "TIME";
	public static final String VALIDATION_ZIP = "ZIP";
	public static final String VALIDATION_PHONE = "PHONE";
	public static final String VALIDATION_SOCIAL_SEC = "SOCIAL_SEC";
	public static final String VALIDATION_EMAIL = "EMAIL";
	public static final String VALIDATION_PERCENT = "PERCENT";
	public static final String VALIDATION_CURRENCY = "CURRENCY";
	public static final String VALIDATION_CUSTOM = "CUSTOM";
	public static final String VALIDATION_FORMULA= "FORMULA";

	private String backgroundColor;
	private String borderColor;
	private String borderStyle;
	private int borderWidth;
	private String displayLabel;
	private boolean visible;
	private String inputType;
	private String tooltip;
	private String fontColor;
	private String fontName;
	private int fontSize;
	private String alignment;
	private String displayFormat;
	private String displayFormatType;
	private boolean masked;
	private String maskingText;
	private String radioCheckType;
	private ConditionalAction conditionalAction;
	private String contentType;
	private String defaultValue;
	private boolean readOnly;
	private String valueExpression;
	private boolean calculated;
	private boolean urlOverridable;
	private boolean required;
	private int minLength;
	private int maxLength;
	private double minValue;
	private double maxValue;
	private String validationErrMsg;
	private String validation;
	private String validationData;
	private String currency;
	private String origin;
	private String name;
	private List<Location> locations;
	private String assignee;
	private List<String> hiddenOptions;
	private List<String> visibleOptions;
}
