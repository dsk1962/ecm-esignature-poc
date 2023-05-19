package com.davita.ecm.esign.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsignAccessToken {
	private String token;
	private long expirationTime = 0;

	public boolean isExpired() {
		return token == null || (System.currentTimeMillis() + 3000) > expirationTime;
	}
}
