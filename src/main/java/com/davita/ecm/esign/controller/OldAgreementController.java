package com.davita.ecm.esign.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.davita.ecm.esign.api.extension.WorkflowsApiExtension;
import com.davita.ecm.esign.model.extension.workflow.RecipientsListInfo;
import com.davita.ecm.esign.model.extension.workflow.Workflow;

import io.swagger.client.api.AgreementsApi;
import io.swagger.client.api.LibraryDocumentsApi;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.agreements.AgreementCreationInfo;
import io.swagger.client.model.agreements.AgreementCreationInfo.SignatureTypeEnum;
import io.swagger.client.model.agreements.AgreementCreationInfo.StateEnum;
import io.swagger.client.model.agreements.AgreementCreationResponse;
import io.swagger.client.model.agreements.AgreementInfo;
import io.swagger.client.model.agreements.FileInfo;
import io.swagger.client.model.agreements.MergefieldInfo;
import io.swagger.client.model.agreements.ParticipantSetInfo;
import io.swagger.client.model.agreements.ParticipantSetMemberInfo;
import io.swagger.client.model.agreements.PostSignOption;
import io.swagger.client.model.agreements.SigningUrlResponse;
import io.swagger.client.model.agreements.SigningUrlSetInfo;
import io.swagger.client.model.libraryDocuments.LibraryDocument;
import io.swagger.client.model.libraryDocuments.LibraryDocument.StatusEnum;
import io.swagger.client.model.workflows.UserWorkflow;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class OldAgreementController extends BaseEsignController {

	@GetMapping(path = "/showAgreementForm", consumes = MediaType.ALL_VALUE)
	public String searchFailedAuditRecords(@RequestParam(required = false) String docId, Model model,
			HttpServletResponse response) throws ApiException, IOException {
		response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=0, no-cache, no-store");
		response.setHeader(HttpHeaders.PRAGMA, "no-cache");
		response.setIntHeader(HttpHeaders.EXPIRES, 0);
		return "sendAgreement";
	}

	@GetMapping(path = "/showWorkflowAgreementForm", consumes = MediaType.ALL_VALUE)
	public String showWorkflowAgreementForm(@RequestParam(required = false) String workflowId, Model model,
			HttpServletResponse response) throws ApiException, IOException {

		response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=0, no-cache, no-store");
		response.setHeader(HttpHeaders.PRAGMA, "no-cache");
		response.setIntHeader(HttpHeaders.EXPIRES, 0);
		WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
		List<UserWorkflow> workflowList = workflowsApi.getWorkflows(accessToken.getToken(), null, false, false, null)
				.getUserWorkflowList();
		model.addAttribute("workflowList", workflowList);
		if (!StringUtils.hasText(workflowId))
			workflowId = workflowList.get(0).getId();
		Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);
		model.addAttribute("workflow", workflow);

		LibraryDocumentsApi libApi = new LibraryDocumentsApi(getApiClient());
		List<LibraryDocument> libraryDocuments = libApi
				.getLibraryDocuments(accessToken.getToken(), null, null, false, null, null).getLibraryDocumentList()
				.stream().filter(libDoc -> libDoc.getStatus().equals(StatusEnum.ACTIVE)).collect(Collectors.toList());
		model.addAttribute("libDocList", libraryDocuments);
		return "sendWorkflow";
	}

	@PostMapping(path = "/createWorkflowAgreement", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createWorkflowAgreement(@RequestParam String workflowId, HttpServletRequest request, Model model,
			HttpServletResponse response) throws ApiException, IOException {

		try {
			response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=0, no-cache, no-store");
			response.setHeader(HttpHeaders.PRAGMA, "no-cache");
			response.setIntHeader(HttpHeaders.EXPIRES, 0);
			WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
			Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);
			model.addAttribute("workflow", workflow);

			AgreementCreationInfo agreementInfo = new AgreementCreationInfo();
			agreementInfo.setName(workflow.getAgreementNameInfo().getDefaultValue());
			agreementInfo.setSignatureType(SignatureTypeEnum.ESIGN);
			agreementInfo.setState(StateEnum.IN_PROCESS);
			agreementInfo.setMessage(workflow.getMessageInfo().getDefaultValue());
			agreementInfo.setWorkflowId(workflow.getId());

			workflow.getFileInfos().forEach(fInfo -> {
				String docId = request.getParameter("_document_" + fInfo.getName());
				if (StringUtils.hasText(docId)) {
					FileInfo fileInfo = new FileInfo();
					fileInfo.setLibraryDocumentId(docId);
					fileInfo.setLabel(fInfo.getLabel());
					agreementInfo.addFileInfosItem(fileInfo);
				}
			});

			workflow.getMergeFieldsInfo().forEach(mfInfo -> {
				String value = request.getParameter("_field_" + mfInfo.getFieldName());
				if (StringUtils.hasText(value)) {
					MergefieldInfo info = new MergefieldInfo();
					info.setFieldName(mfInfo.getFieldName());
					info.setDefaultValue(value);
					agreementInfo.addMergeFieldInfoItem(info);
				}
			});
			int order = 1;
			for (RecipientsListInfo rInfo : workflow.getRecipientsListInfo()) {
				String email = request.getParameter("_recipient_" + rInfo.getName());
				if (StringUtils.hasText(email)) {
					ParticipantSetInfo participantSetInfo = new ParticipantSetInfo();
					participantSetInfo.setOrder(order++);
					// do not set name. Thos may cause "Recipient set is not enabled" error
					// participantSetInfo.setName(rInfo.getName());
					participantSetInfo.setLabel(rInfo.getLabel());
					participantSetInfo.setRole(ParticipantSetInfo.RoleEnum.fromValue(rInfo.getRole()));
					ParticipantSetMemberInfo participantSetMemberInfo = new ParticipantSetMemberInfo();
					participantSetMemberInfo.setEmail(email);
					participantSetInfo.addMemberInfosItem(participantSetMemberInfo);
					agreementInfo.addParticipantSetsInfoItem(participantSetInfo);
				}
			}

			PostSignOption option = new PostSignOption();
			option.setRedirectDelay(15);
			option.setRedirectUrl("https://www.davita.com");

			agreementInfo.setPostSignOption(option);
			AgreementsApi agreementsApi = new AgreementsApi(getApiClient());
			AgreementCreationResponse agreementCreationResponse = agreementsApi.createAgreement(accessToken.getToken(),
					agreementInfo, "email:dmitry.kuvshinov@davita.com", null);
			log.info("Agreement created. Agreement Id: {}", agreementCreationResponse.getId());
			AgreementInfo aInfo = agreementsApi.getAgreementInfo(accessToken.getToken(),
					agreementCreationResponse.getId(), "email:dmitry.kuvshinov@davita.com", null, null);
			model.addAttribute("agreementInfo", aInfo);
			model.addAttribute("success", true);
		} catch (Exception e) {
			model.addAttribute("message",
					e instanceof ApiException ex ? e.getMessage() + ex.getResponseBody() : e.getMessage());
			model.addAttribute("success", false);
		}
		return "agreementInfo";
	}

	@GetMapping(path = "/agreementInfo", consumes = MediaType.ALL_VALUE)
	public String agreementInfo(@RequestParam String agreementId, Model model, HttpServletResponse response)
			throws ApiException, IOException {
		try {
			response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=0, no-cache, no-store");
			response.setHeader(HttpHeaders.PRAGMA, "no-cache");
			response.setIntHeader(HttpHeaders.EXPIRES, 0);
			AgreementsApi agreementsApi = new AgreementsApi(getApiClient());
			AgreementInfo agreementInfo = agreementsApi.getAgreementInfo(accessToken.getToken(), agreementId, "email:dmitry.kuvshinov@davita.com",
					null, null);
			model.addAttribute("agreementInfo", agreementInfo);
			List<SigningUrlSetInfo> signInfoList = Collections.emptyList();
			if (agreementInfo.getStatus()
					.equals(io.swagger.client.model.agreements.AgreementInfo.StatusEnum.OUT_FOR_SIGNATURE)) {
				SigningUrlResponse signingUrlResponse = agreementsApi.getSigningUrl(accessToken.getToken(),
						agreementInfo.getId(), "email:dmitry.kuvshinov@davita.com", null);
				signInfoList = signingUrlResponse.getSigningUrlSetInfos();
			}
			model.addAttribute("signInfoList", signInfoList);
			model.addAttribute("success", true);
		} catch (Exception e) {
			model.addAttribute("message",
					e instanceof ApiException ex ? e.getMessage() + ex.getResponseBody() : e.getMessage());
			model.addAttribute("success", false);
		}
		return "agreementInfo";
	}
}
