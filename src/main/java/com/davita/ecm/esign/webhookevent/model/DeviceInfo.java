package com.davita.ecm.esign.webhookevent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceInfo {
	private String applicationDescription;
	private String deviceDescription;
	private Location location;
	private String deviceTime;
}
