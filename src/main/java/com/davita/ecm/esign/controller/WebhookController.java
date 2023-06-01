package com.davita.ecm.esign.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.davita.ecm.esign.webhookevent.model.AgreementCreateEvent;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class WebhookController extends BaseEsignController {
	private void approveWebHookRequest(String applicationName, HttpServletRequest request,
			HttpServletResponse response) {
		String clientId = request.getHeader(ADOBE_CLIENT_HEADER);
		response.setHeader(ADOBE_CLIENT_HEADER, clientId);
		log.info("ApplicationName: {}, X-Adobesign-Clientid={}", applicationName, clientId);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@GetMapping("/webhook/{applicationName}")
	public void webhook(@PathVariable String applicationName, HttpServletRequest request,
			HttpServletResponse response) {
		approveWebHookRequest(applicationName, request, response);
	}

	@PostMapping("/webhook/{applicationName}")
	public void webhookExvent(@RequestBody AgreementCreateEvent agreementEvent, @PathVariable String applicationName,
			HttpServletRequest request, HttpServletResponse response) {
		log.info("agreementEvent={}", agreementEvent);
		log.info("agreementId={}, Event={}, Status={}", agreementEvent.getAgreement().getId(),
				agreementEvent.getEvent(), agreementEvent.getAgreement().getStatus());
		approveWebHookRequest(applicationName, request, response);
	}

}
