package com.davita.ecm.esign.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.davita.ecm.esign.api.extension.LibraryDocumentsApiExtension;
import com.davita.ecm.esign.api.extension.WorkflowsApiExtension;
import com.davita.ecm.esign.model.AngularButton;
import com.davita.ecm.esign.model.AngularCheckbox;
import com.davita.ecm.esign.model.AngularCombobox;
import com.davita.ecm.esign.model.AngularContainer;
import com.davita.ecm.esign.model.AngularDateField;
import com.davita.ecm.esign.model.AngularHiddenInputField;
import com.davita.ecm.esign.model.AngularInputField;
import com.davita.ecm.esign.model.AngularMethodCall;
import com.davita.ecm.esign.model.AngularNumericField;
import com.davita.ecm.esign.model.AngularTextField;
import com.davita.ecm.esign.model.Option;
import com.davita.ecm.esign.model.extension.librarydocument.Field;
import com.davita.ecm.esign.model.extension.librarydocument.FieldList;
import com.davita.ecm.esign.model.extension.workflow.MergeFieldsInfo;
import com.davita.ecm.esign.model.extension.workflow.RecipientsListInfo;
import com.davita.ecm.esign.model.extension.workflow.Workflow;
import com.davita.ecm.esign.webhookevent.model.AgreementCreateEvent;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.client.api.AgreementsApi;
import io.swagger.client.api.LibraryDocumentsApi;
import io.swagger.client.api.WorkflowsApi;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.agreements.AgreementCreationInfo;
import io.swagger.client.model.agreements.AgreementCreationInfo.SignatureTypeEnum;
import io.swagger.client.model.agreements.AgreementCreationInfo.StateEnum;
import io.swagger.client.model.agreements.AgreementCreationResponse;
import io.swagger.client.model.agreements.FileInfo;
import io.swagger.client.model.agreements.MergefieldInfo;
import io.swagger.client.model.agreements.ParticipantSetInfo;
import io.swagger.client.model.agreements.ParticipantSetMemberInfo;
import io.swagger.client.model.agreements.PostSignOption;
import io.swagger.client.model.libraryDocuments.LibraryDocument;
import io.swagger.client.model.libraryDocuments.LibraryDocument.StatusEnum;
import io.swagger.client.model.workflows.UserWorkflow;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class EsignController extends BaseEsignController {

	@Autowired
	ObjectMapper mapper;

	public static final String ADOBE_CLIENT_HEADER = "X-ADOBESIGN-CLIENTID";
	public static final String FIELD_PREFIX = "$FIELD$";
	public static final String DOC_PREFIX = "$DOC$";
	public static final String PARTICIPANT_PREFIX = "$PCNT$";

	public static final String FORM_DEFINITION = "formDefinition";
	public static final String FORM_REQUEST = "formRequest";

	@PostConstruct
	public void postConstruct() {
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

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

	@GetMapping(path = "/libraries", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<LibraryDocument> getLibraries() throws IOException, ApiException {
		LibraryDocumentsApi libApi = new LibraryDocumentsApi(getApiClient());
		return libApi.getLibraryDocuments(accessToken.getToken(), null, null, false, null, null)
				.getLibraryDocumentList();
	}

	// "email:dmitry.kuvshinov@davita.com"
	@GetMapping(path = "/workflows", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserWorkflow> getWorkFlows() throws IOException, ApiException {
		WorkflowsApi workflowsApi = new WorkflowsApi(getApiClient());
		return workflowsApi.getWorkflows(accessToken.getToken(), null, false, false, null).getUserWorkflowList();
	}

	@GetMapping(path = "/workflow", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Workflow getWorkFlow(@RequestParam String workflowId) throws IOException, ApiException {
		WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
		return workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);
	}

	@GetMapping(path = "/options/{listName}", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode getOptions(@PathVariable String listName) throws IOException, ApiException {
		List<Option> result = new ArrayList<>();
		switch (listName) {
		case "templates":
			LibraryDocumentsApi libApi = new LibraryDocumentsApi(getApiClient());
			List<LibraryDocument> tlist = libApi
					.getLibraryDocuments(accessToken.getToken(), null, null, false, null, null)
					.getLibraryDocumentList();
			if (!CollectionUtils.isEmpty(tlist))
				tlist.forEach(t -> result.add(new Option(t.getName(), t.getId())));
			break;
		case "workflows":
			WorkflowsApi workflowsApi = new WorkflowsApi(getApiClient());
			// compliance group id "CBJCHBCAABAAMbwkAqvGZ_cFHHAzJU6h3-mthNC-m4Bv"
			List<UserWorkflow> wlist = workflowsApi.getWorkflows(accessToken.getToken(), null, false, false, null)
					.getUserWorkflowList();
			if (!CollectionUtils.isEmpty(wlist))
				wlist.forEach(t -> result.add(new Option(t.getDisplayName(), t.getId())));
			break;
		default:
			break;
		}
		return createSuccessJson(objectMapper.valueToTree(result));
	}

	@GetMapping(path = "/staticForm", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode getStaticForm(@RequestParam String formPath) {
		try {
			ObjectNode response = createSuccessJson(null);
			response.set(FORM_DEFINITION,objectMapper.readTree(ResourceUtils.getFile("classpath:static/" + formPath)));
			return response;
		} catch (FileNotFoundException e) {
			return createErrorJson("Form: " + formPath + " doesn't exist");
		} catch (Exception e) {
			return createErrorJson("Failed to read formForm: " + formPath + ". Error: " + e.getMessage());
		}
	}

	@GetMapping(path = "/form/workflowForm", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode workflowForm(@RequestParam String workflowId) throws ApiException, IOException {
		AngularContainer root = new AngularContainer();
		root.setClassNames("fixed-width-centered-container");
		AngularContainer body = new AngularContainer();
		body.setClassNames("full-width-centered-container");
		root.addChild(body);

		WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
		Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);
		AngularContainer container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames("full-width-centered-container");
		container.setHtml("<h2>" + workflow.getDisplayName() + "</h2>");
		body.addChild(container);
		// populate form fields
		List<MergeFieldsInfo> list = workflow.getMergeFieldsInfo();
		if (!CollectionUtils.isEmpty(list)) {
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Parameters</h3>");
			body.addChild(container);
			list.forEach(field -> {
				AngularTextField aField = new AngularTextField();
				aField.setLabel(field.getDisplayName());
				aField.setName(FIELD_PREFIX + field.getFieldName());
				aField.setInitValue(field.getDefaultValue());
				aField.setRequired(field.isRequired());
				if (!field.isEditable())
					aField.setReadonly(true);
				body.addChild(aField);
			});
		}

		LibraryDocumentsApi libApi = new LibraryDocumentsApi(getApiClient());
		List<LibraryDocument> libraryDocuments = libApi
				.getLibraryDocuments(accessToken.getToken(), null, null, false, null, null).getLibraryDocumentList()
				.stream().filter(libDoc -> libDoc.getStatus().equals(StatusEnum.ACTIVE)).collect(Collectors.toList());
		List<Option> allDocuments = new ArrayList<>();
		if (!CollectionUtils.isEmpty(list))
			libraryDocuments.forEach(doc -> allDocuments.add(new Option(doc.getName(), doc.getId())));

		List<com.davita.ecm.esign.model.extension.workflow.FileInfo> flist = workflow.getFileInfos();
		if (!CollectionUtils.isEmpty(list)) {
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Documents</h3>");
			body.addChild(container);
			flist.forEach(file -> {
				List<Option> options = new ArrayList<>();
				AngularCombobox aField = new AngularCombobox();
				aField.setRequired(file.isRequired());
				if (!aField.isRequired())
					options.add(new Option("", ""));
				aField.setLabel(file.getLabel());
				aField.setName(DOC_PREFIX + file.getName());
				if (!CollectionUtils.isEmpty(file.getWorkflowLibraryDocumentSelectorList())) {
					file.getWorkflowLibraryDocumentSelectorList()
							.forEach(doc -> options.add(new Option(doc.getLabel(), doc.getWorkflowLibDoc())));
					aField.setOptions(options);
				} else
					options.addAll(allDocuments);
				aField.setOptions(options);
				aField.setInitValue(aField.getOptions().get(0).getValue());
				body.addChild(aField);
			});
		}

		List<RecipientsListInfo> rlist = workflow.getRecipientsListInfo();
		if (!CollectionUtils.isEmpty(rlist)) {
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Participnat Emails</h3>");
			body.addChild(container);
			rlist.forEach(recipient -> {
				AngularTextField aField = new AngularTextField();
				aField.setLabel(recipient.getLabel());
				aField.setName(PARTICIPANT_PREFIX + recipient.getName());
				aField.setInitValue(recipient.getDefaultValue());
				aField.setPattern("^[\\w\\-\\.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$");
				aField.setPatternError("Invalid email");
				body.addChild(aField);
			});
		}

		AngularContainer toolbar = new AngularContainer();
		toolbar.setLayout("horizontal");
		toolbar.setClassNames("full-width-centered-container dynamic-toolbar");
		root.addChild(toolbar);
		AngularButton createButton = new AngularButton();
		createButton.setLabel("Create");
		createButton.setLabel("Create");
		AngularMethodCall call = new AngularMethodCall();
		call.setMember("applicationServiceService");
		call.setMethod("postForm");
		call.addParameter("createNewAgreement");
		createButton.setOnclick(call);
		AngularButton cancelButton = new AngularButton();
		cancelButton.setLabel("Cancel");
		call = new AngularMethodCall();
		call.setMember("applicationServiceService");
		call.setMethod("getStaticForm");
		call.addParameter("forms/fromWorkflow.json");
		cancelButton.setOnclick(call);
		toolbar.addChild(createButton);
		toolbar.addChild(cancelButton);
		AngularHiddenInputField worflowHidden = new AngularHiddenInputField();
		worflowHidden.setName("@@workflowId");
		worflowHidden.setInitValue(workflowId);
		root.addChild(worflowHidden);
		ObjectNode response = createSuccessJson(null);
		response.set(FORM_DEFINITION, objectMapper.valueToTree(root));
		return response;
	}

	@GetMapping(path = "/form/templateForm", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode templateForm(@RequestParam String templateId) throws ApiException, IOException {
		log.info("templateId: {}", templateId);
		AngularContainer root = new AngularContainer();
		root.setClassNames("fixed-width-centered-container");
		AngularContainer body = new AngularContainer();
		body.setClassNames("full-width-centered-container");
		root.addChild(body);

		AngularContainer container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames("full-width-centered-container");
		container.setHtml("<h2>Template Data</h2>");
		body.addChild(container);
		// populate form fields
		HashSet<String> participants = new HashSet<>();
		LibraryDocumentsApiExtension libApi = new LibraryDocumentsApiExtension(getApiClient());
		FieldList list = libApi.getFormFields(accessToken.getToken(), templateId, null, null, null);
		if (!CollectionUtils.isEmpty(list.getFields())) {
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Template Parameters</h3>");
			body.addChild(container);
			list.getFields().forEach(field -> {
				AngularInputField aField = null;
				if (StringUtils.hasText(field.getAssignee()))
					participants.add(field.getAssignee());
				if (StringUtils.hasText(field.getInputType())) {
					switch (field.getInputType()) {
					case Field.TEXT_FIELD:
						String validation = field.getValidation();
						if (validation == null)
							validation = Field.VALIDATION_NONE;
						switch (validation) {
						case Field.VALIDATION_NUMBER:
							AngularNumericField numField = new AngularNumericField();
							aField = numField;
							break;
						case Field.VALIDATION_DATE:
							AngularDateField dateField = new AngularDateField();
							aField = dateField;
							break;
						case Field.VALIDATION_CUSTOM:
							AngularTextField textField = new AngularTextField();
							aField = textField;
							textField.setPattern(field.getValidationData());
							if (StringUtils.hasText(field.getValidationErrMsg()))
								textField.setPatternError(field.getValidationErrMsg());
							break;
						case Field.VALIDATION_EMAIL:
							AngularTextField emailField = new AngularTextField();
							aField = emailField;
							emailField.setPattern("^[\\w\\-\\.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$");
							break;
						default:
							AngularTextField txtField = new AngularTextField();
							aField = txtField;
						}
						break;
					case Field.CHECKBOX:
						aField = new AngularCheckbox();
						break;
					case Field.DROP_DOWN:
						aField = new AngularCombobox();
						if (!CollectionUtils.isEmpty(field.getVisibleOptions())) {
							List<Option> options = new ArrayList<>(20);
							field.getVisibleOptions().forEach(option -> options.add(new Option(option, option)));
							((AngularCombobox) aField).setOptions(options);
						}
						break;
					case Field.LISTBOX:
						aField = new AngularCheckbox();
						break;
					case Field.MULTILINE:
						aField = new AngularCheckbox();
						break;
					default:
						break;
					}
					if (aField != null) {
						aField.setTooltip(field.getTooltip());
						aField.setLabel(field.getDisplayLabel());
						if (!StringUtils.hasText(aField.getLabel()))
							aField.setLabel(field.getName());
						aField.setName(FIELD_PREFIX + field.getName());
						aField.setInitValue(field.getDefaultValue());
						aField.setRequired(field.isRequired());
						if (StringUtils.hasText(field.getTooltip()))
							aField.setPlaceholder(field.getTooltip());
						if (field.isReadOnly())
							aField.setReadonly(true);
						body.addChild(aField);
					}
				}
			});
		}

		if (!participants.isEmpty()) {
			ArrayList<String> pList = new ArrayList<>(participants);
			Collections.sort(pList);
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Participnat Emails</h3>");
			body.addChild(container);
			pList.forEach(recipient -> {
				AngularTextField aField = new AngularTextField();
				aField.setLabel(recipient);
				aField.setName(PARTICIPANT_PREFIX + recipient);
				aField.setPattern("^[\\w\\-\\.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$");
				aField.setPatternError("Invalid email");
				body.addChild(aField);
			});

		}
		AngularContainer toolbar = new AngularContainer();
		toolbar.setLayout("horizontal");
		toolbar.setClassNames("full-width-centered-container dynamic-toolbar");
		root.addChild(toolbar);
		AngularButton createButton = new AngularButton();
		createButton.setLabel("Create");
		AngularMethodCall call = new AngularMethodCall();
		call.setMember("applicationServiceService");
		call.setMethod("postForm");
		call.addParameter("createNewAgreement");
		createButton.setOnclick(call);
		AngularButton cancelButton = new AngularButton();
		cancelButton.setLabel("Cancel");
		call = new AngularMethodCall();
		call.setMember("applicationServiceService");
		call.setMethod("getStaticForm");
		call.addParameter("forms/fromTemplate.json");
		cancelButton.setOnclick(call);
		toolbar.addChild(createButton);
		toolbar.addChild(cancelButton);
		ObjectNode response = createSuccessJson(null);
		response.set(FORM_DEFINITION, objectMapper.valueToTree(root));
		return response;
	}

	@GetMapping(path = "/sendAgreement", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String sendAgreement(@RequestParam String docId, @RequestParam String EmailConsent_patientEmailID,
			@RequestParam String EmailConsent_patientName, @RequestParam String participantEmail)
			throws IOException, ApiException {
		AgreementCreationInfo agreementInfo = new AgreementCreationInfo();
		agreementInfo.setName("Sample_Agreement");
		agreementInfo.setSignatureType(SignatureTypeEnum.ESIGN);
		agreementInfo.setState(StateEnum.IN_PROCESS);
		agreementInfo.setMessage(
				"You may give permission to DaVita Healthcare company to communicate with you by email and text message. This form provides information about the risks of the forms of communication, guidelines for email \\/ text communication, and how we use email \\/ text communication. It also will be used to document your consent for communication with you by email and text message");

		FileInfo fileInfo = new FileInfo();
		fileInfo.setLibraryDocumentId(docId);
		agreementInfo.addFileInfosItem(fileInfo);

		ParticipantSetInfo participantSetInfo = new ParticipantSetInfo();
		participantSetInfo.setOrder(1);
		participantSetInfo.setName("Name");
		participantSetInfo.setRole(ParticipantSetInfo.RoleEnum.SIGNER);

		MergefieldInfo info = new MergefieldInfo();
		info.setFieldName("EmailConsent_patientName");
		info.setDefaultValue(EmailConsent_patientName);
		agreementInfo.addMergeFieldInfoItem(info);
		info = new MergefieldInfo();
		info.setFieldName("EmailConsent_patientEmailID");
		info.setDefaultValue(EmailConsent_patientEmailID);
		agreementInfo.addMergeFieldInfoItem(info);

		ParticipantSetMemberInfo participantSetMemberInfo = new ParticipantSetMemberInfo();

		if (!StringUtils.hasText(participantEmail))
			participantEmail = "Dmitry.Kuvshinov@davita.com";
		participantSetMemberInfo.setEmail(participantEmail);
		participantSetInfo.addMemberInfosItem(participantSetMemberInfo);
		agreementInfo.addParticipantSetsInfoItem(participantSetInfo);

		PostSignOption option = new PostSignOption();
		option.setRedirectDelay(15);
		option.setRedirectUrl("https://www.davita.com");

		agreementInfo.setPostSignOption(option);

		AgreementsApi agreementsApi = new AgreementsApi(getApiClient());
		AgreementCreationResponse response = agreementsApi.createAgreement(accessToken.getToken(), agreementInfo, null,
				null);
		return response.getId();
	}

	@PostMapping("/createNewAgreement")
	public JsonNode createNewAgreement(@RequestBody ObjectNode json) {
		log.info("json={}", json);
		ObjectNode response = createSuccessJson(null);
		response.put("errorMessage", "Error Message Test");
		response.put("infoMessage", "Info Message Test");
		return response;
	}

}
