package com.davita.ecm.esign.webhookevent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PostSignOption {
	private String redirectDelay;
	private String redirectUrl;
}

