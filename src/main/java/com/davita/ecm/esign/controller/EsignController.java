package com.davita.ecm.esign.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.davita.ecm.esign.model.Utilities;
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
import io.swagger.client.model.agreements.AgreementInfo;
import io.swagger.client.model.agreements.ExternalId;
import io.swagger.client.model.agreements.FileInfo;
import io.swagger.client.model.agreements.MergefieldInfo;
import io.swagger.client.model.agreements.ParticipantSetInfo;
import io.swagger.client.model.agreements.ParticipantSetMemberInfo;
import io.swagger.client.model.agreements.PostSignOption;
import io.swagger.client.model.libraryDocuments.LibraryDocument;
import io.swagger.client.model.libraryDocuments.LibraryDocument.StatusEnum;
import io.swagger.client.model.libraryDocuments.LibraryDocumentCreationInfoV6;
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

	private static final String APPLICATION_SERVICE_TS_NAME = "applicationServiceService";
	private static final String FULLWIDTH_CENTERED_CONTAINER_CSS_NAME = "full-width-centered-container";

	private static final String EMAIL_PATTERN = "^[\\w\\-\\.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$";

	public static final String FORM_DEFINITION = "formDefinition";
	public static final String FORM_REQUEST = "formRequest";
	public static final String WORKFLOW_ID_ENTRY = "$$workflowId$$";
	public static final String TEMPLATE_ID_ENTRY = "$$templateId$$";
	public static final String EXTERNAL_GROUP_ID_ENTRY = "$$externalGroupId$$";
	public static final String EXTERNAL_ID_ENTRY = "$$agreement_externalId$$";
	public static final String AGREEMENT_NAME_ENTRY = "$$agreement_name$$";

	public static final String X_USER = "email:dmitry.kuvshinov@davita.com";

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
			ObjectNode response = createSuccessJson();
			response.set(FORM_DEFINITION, objectMapper.readTree(ResourceUtils.getFile("classpath:static/" + formPath)));
			return response;
		} catch (FileNotFoundException e) {
			return createErrorJson("Form: " + formPath + " doesn't exist");
		} catch (Exception e) {
			return createErrorJson("Failed to read formForm: " + formPath + ". Error: " + e.getMessage());
		}
	}

	@GetMapping(path = "/form/workflowFormDocuments", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode workflowFormDocuments(@RequestParam String workflowId) throws ApiException, IOException {
		AngularContainer root = new AngularContainer();
		root.setClassNames("fixed-width-centered-container");
		AngularContainer body = new AngularContainer();
		body.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
		root.addChild(body);
		WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
		Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);
		AngularContainer container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
		container.setHtml("<h2>Workflow: " + workflow.getDisplayName() + "</h2>");
		body.addChild(container);
		LibraryDocumentsApi libApi = new LibraryDocumentsApi(getApiClient());
		List<LibraryDocument> libraryDocuments = libApi
				.getLibraryDocuments(accessToken.getToken(), null, null, false, null, null).getLibraryDocumentList()
				.stream().filter(libDoc -> libDoc.getStatus().equals(StatusEnum.ACTIVE)).collect(Collectors.toList());
		List<Option> allDocuments = new ArrayList<>();
		if (!CollectionUtils.isEmpty(libraryDocuments))
			libraryDocuments.forEach(doc -> allDocuments.add(new Option(doc.getName(), doc.getId())));

		List<com.davita.ecm.esign.model.extension.workflow.FileInfo> flist = workflow.getFileInfos();
		if (!CollectionUtils.isEmpty(libraryDocuments)) {
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
		AngularContainer toolbar = new AngularContainer();
		toolbar.setLayout("horizontal");
		toolbar.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME + " dynamic-toolbar");
		root.addChild(toolbar);
		AngularButton createButton = new AngularButton();
		createButton.setLabel("Next");
		AngularMethodCall call = new AngularMethodCall();
		call.setMember(APPLICATION_SERVICE_TS_NAME);
		call.setMethod("postForm");
		call.addParameter("form/workflowFormFields");
		createButton.setOnclick(call);
		AngularButton cancelButton = new AngularButton();
		cancelButton.setLabel("Cancel");
		call = new AngularMethodCall();
		call.setMember(APPLICATION_SERVICE_TS_NAME);
		call.setMethod("getStaticForm");
		call.addParameter("forms/fromWorkflow.json");
		cancelButton.setOnclick(call);
		toolbar.addChild(createButton);
		toolbar.addChild(cancelButton);
		AngularHiddenInputField worflowHidden = new AngularHiddenInputField();
		worflowHidden.setName(WORKFLOW_ID_ENTRY);
		worflowHidden.setInitValue(workflowId);
		root.addChild(worflowHidden);
		ObjectNode response = createSuccessJson();
		response.set(FORM_DEFINITION, objectMapper.valueToTree(root));
		return response;
	}

	@PostMapping(path = "/form/workflowFormFields", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode workflowFormFields(@RequestBody ObjectNode json) throws ApiException, IOException {
		log.info("json={}", json);
		try {
			AngularContainer root = new AngularContainer();
			root.setClassNames("fixed-width-centered-container");
			AngularContainer body = new AngularContainer();
			body.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
			root.addChild(body);

			String workflowId = json.has(WORKFLOW_ID_ENTRY) ? json.get(WORKFLOW_ID_ENTRY).asText() : null;
			if (!StringUtils.hasText(workflowId))
				return createErrorJson("No workflow id found in input data");
			WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
			Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);

			HashMap<String, String> docs = new HashMap<>(10);
			workflow.getFileInfos().forEach(fInfo -> {
				String docId = Utilities.getStringValue(json, DOC_PREFIX + fInfo.getName());
				if (StringUtils.hasText(docId)) {
					docs.put(DOC_PREFIX + fInfo.getName(), docId);
				}
			});

			AngularContainer container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
			container.setHtml("<h2>Workflow: " + workflow.getDisplayName() + "</h2>");
			body.addChild(container);

			HashSet<String> fieldNames = new HashSet<>();

			// populate form fields
			List<MergeFieldsInfo> list = workflow.getMergeFieldsInfo();
			if (!CollectionUtils.isEmpty(list)) {
				container = new AngularContainer();
				container.setLayout("horizontal");
				container.setClassNames("full-width-container");
				container.setHtml("<h3>Workflow Parameters</h3>");
				body.addChild(container);
				list.forEach(field -> {
					if (!fieldNames.contains(field.getFieldName())) {
						fieldNames.add(field.getFieldName());
						AngularTextField aField = new AngularTextField();
						aField.setLabel(field.getDisplayName());
						aField.setName(FIELD_PREFIX + field.getFieldName());
						aField.setInitValue(field.getDefaultValue());
						aField.setRequired(field.isRequired());
						if (!field.isEditable())
							aField.setReadonly(true);
						body.addChild(aField);
					}
				});
			}
			LibraryDocumentsApiExtension libApi = new LibraryDocumentsApiExtension(getApiClient());
			for (String templateId : docs.values()) {
				LibraryDocumentCreationInfoV6 info = libApi.getLibraryDocumentInfo(accessToken.getToken(), templateId,
						null, null, null);
				FieldList flist = libApi.getFormFields(accessToken.getToken(), templateId, null, null, null);
				if (!CollectionUtils.isEmpty(flist.getFields())) {
					AngularContainer tContainer = new AngularContainer();
					tContainer.setLayout("horizontal");
					tContainer.setClassNames("full-width-container");
					tContainer.setHtml("<h3>" + info.getName() + " Parameters</h3>");
					body.addChild(tContainer);
					flist.getFields().forEach(field -> {
						if (!fieldNames.contains(field.getName())) {
							fieldNames.add(field.getName());
							AngularInputField aField = createAngularField(field);
							if (aField != null)
								body.addChild(aField);
						}
					});
				}
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
					aField.setPattern(EMAIL_PATTERN);
					aField.setPatternError("Invalid email");
					body.addChild(aField);
				});

			}
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Agreement info</h3>");
			body.addChild(container);
			AngularTextField aField = new AngularTextField();
			aField.setLabel("External ID");
			aField.setName(EXTERNAL_ID_ENTRY);
			body.addChild(aField);
			aField = new AngularTextField();
			aField.setLabel("Name");
			aField.setName(AGREEMENT_NAME_ENTRY);
			body.addChild(aField);

			AngularContainer toolbar = new AngularContainer();
			toolbar.setLayout("horizontal");
			toolbar.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME + " dynamic-toolbar");
			root.addChild(toolbar);
			AngularButton createButton = new AngularButton();
			createButton.setLabel("Next");
			AngularMethodCall call = new AngularMethodCall();
			call.setMember(APPLICATION_SERVICE_TS_NAME);
			call.setMethod("postForm");
			call.addParameter("createWorkflowAgreement");
			createButton.setOnclick(call);
			AngularButton cancelButton = new AngularButton();
			cancelButton.setLabel("Cancel");
			call = new AngularMethodCall();
			call.setMember(APPLICATION_SERVICE_TS_NAME);
			call.setMethod("getStaticForm");
			call.addParameter("forms/fromWorkflow.json");
			cancelButton.setOnclick(call);
			toolbar.addChild(createButton);
			toolbar.addChild(cancelButton);
			// add workflowid as hidden variable
			AngularHiddenInputField worflowHidden = new AngularHiddenInputField();
			worflowHidden.setName(WORKFLOW_ID_ENTRY);
			worflowHidden.setInitValue(workflowId);
			root.addChild(worflowHidden);
			// add document ids as hidden variables
			docs.entrySet().forEach(entry -> {
				AngularHiddenInputField docHidden = new AngularHiddenInputField();
				docHidden.setName(entry.getKey());
				docHidden.setInitValue(entry.getValue());
				root.addChild(docHidden);
			});
			ObjectNode response = createSuccessJson();
			response.set(FORM_DEFINITION, objectMapper.valueToTree(root));
			return response;
		} catch (ApiException e) {
			log.error("Create field form failed.", e);
			return createErrorJson(e.getMessage() + ". Response: " + e.getResponseBody());
		} catch (IOException e) {
			log.error("Create field form failed.", e);
			return createErrorJson(e.getMessage());
		}

	}

	@GetMapping(path = "/form/workflowForm", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode workflowForm(@RequestParam String workflowId) throws ApiException, IOException {
		AngularContainer root = new AngularContainer();
		root.setClassNames("fixed-width-centered-container");
		AngularContainer body = new AngularContainer();
		body.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
		root.addChild(body);

		WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
		Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);
		AngularContainer container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
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
		if (!CollectionUtils.isEmpty(libraryDocuments))
			libraryDocuments.forEach(doc -> allDocuments.add(new Option(doc.getName(), doc.getId())));

		List<com.davita.ecm.esign.model.extension.workflow.FileInfo> flist = workflow.getFileInfos();
		if (!CollectionUtils.isEmpty(libraryDocuments)) {
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
				aField.setPattern(EMAIL_PATTERN);
				aField.setPatternError("Invalid email");
				body.addChild(aField);
			});

		}
		container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames("full-width-container");
		container.setHtml("<h3>Agreement info</h3>");
		body.addChild(container);
		AngularTextField aField = new AngularTextField();
		aField.setLabel("External ID");
		aField.setName(EXTERNAL_ID_ENTRY);
		body.addChild(aField);
		aField = new AngularTextField();
		aField.setLabel("Name");
		aField.setName(AGREEMENT_NAME_ENTRY);
		body.addChild(aField);

		AngularContainer toolbar = new AngularContainer();
		toolbar.setLayout("horizontal");
		toolbar.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME + " dynamic-toolbar");
		root.addChild(toolbar);
		AngularButton createButton = new AngularButton();
		createButton.setLabel("Create");
		AngularMethodCall call = new AngularMethodCall();
		call.setMember(APPLICATION_SERVICE_TS_NAME);
		call.setMethod("postForm");
		call.addParameter("createWorkflowAgreement");
		createButton.setOnclick(call);
		AngularButton cancelButton = new AngularButton();
		cancelButton.setLabel("Cancel");
		call = new AngularMethodCall();
		call.setMember(APPLICATION_SERVICE_TS_NAME);
		call.setMethod("getStaticForm");
		call.addParameter("forms/fromWorkflow.json");
		cancelButton.setOnclick(call);
		toolbar.addChild(createButton);
		toolbar.addChild(cancelButton);
		AngularHiddenInputField worflowHidden = new AngularHiddenInputField();
		worflowHidden.setName(WORKFLOW_ID_ENTRY);
		worflowHidden.setInitValue(workflowId);
		root.addChild(worflowHidden);
		ObjectNode response = createSuccessJson();
		response.set(FORM_DEFINITION, objectMapper.valueToTree(root));
		return response;
	}

	private AngularInputField createAngularField(Field field) {
		AngularInputField aField = null;
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
					emailField.setPattern(EMAIL_PATTERN);
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
			}
		}
		return aField;
	}
	@PostMapping(path = "/agreement/search", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode searchAgreements(@RequestBody ObjectNode json) {
		log.info("json={}", json);
//		try {
			//SearchAPI
			ObjectNode result = createSuccessJson();
			return result;
//		} catch (ApiException e) {
//			log.error("Create agreement from workflow failed.", e);
//			return createErrorJson(e.getMessage() + ". Response: " + e.getResponseBody());
//		} catch (IOException e) {
//			log.error("Create agreement from workflow failed.", e);
//			return createErrorJson(e.getMessage());
//		}
	}

	@GetMapping(path = "/form/templateForm", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode templateForm(@RequestParam String templateId) throws ApiException, IOException {
		log.info("templateId: {}", templateId);
		AngularContainer root = new AngularContainer();
		root.setClassNames("fixed-width-centered-container");
		AngularContainer body = new AngularContainer();
		body.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
		root.addChild(body);

		LibraryDocumentsApiExtension libApi = new LibraryDocumentsApiExtension(getApiClient());
		LibraryDocumentCreationInfoV6 info = libApi.getLibraryDocumentInfo(accessToken.getToken(), templateId, null,
				null, null);
		FieldList list = libApi.getFormFields(accessToken.getToken(), templateId, null, null, null);

		AngularContainer container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
		container.setHtml("<h2>Template: " + info.getName() + "</h2>");
		body.addChild(container);
		// populate form fields
		HashSet<String> participants = new HashSet<>();
		if (!CollectionUtils.isEmpty(list.getFields())) {
			container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames("full-width-container");
			container.setHtml("<h3>Template Parameters</h3>");
			body.addChild(container);
			list.getFields().forEach(field -> {
				AngularInputField aField = createAngularField(field);
				if (StringUtils.hasText(field.getAssignee()))
					participants.add(field.getAssignee());
				if (aField != null)
					body.addChild(aField);
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
				AngularContainer pContainer = new AngularContainer();
				pContainer.setLayout("horizontal");
				pContainer.setClassNames("full-width-container");
				AngularTextField aField = new AngularTextField();
				aField.setLabel(recipient);
				aField.setClassNames("dynamic-flex-1");
				aField.setName(PARTICIPANT_PREFIX + recipient);
				aField.setPattern(EMAIL_PATTERN);
				aField.setPatternError("Invalid email");
				pContainer.addChild(aField);
				AngularNumericField nField = new AngularNumericField();
				nField.setLabel("Order");
				nField.setStyle("max-width:200px;margin-left:10px;");
				nField.setLabelStyle("max-width:60px");
				nField.setName(PARTICIPANT_PREFIX + recipient + "-order");
				pContainer.addChild(nField);
				body.addChild(pContainer);
			});

		}
		container = new AngularContainer();
		container.setLayout("horizontal");
		container.setClassNames("full-width-container");
		container.setHtml("<h3>Agreement info</h3>");
		body.addChild(container);
		AngularTextField aField = new AngularTextField();
		aField.setLabel("External ID");
		aField.setName(EXTERNAL_ID_ENTRY);
		body.addChild(aField);
		aField = new AngularTextField();
		aField.setLabel("Name");
		aField.setName(AGREEMENT_NAME_ENTRY);
		body.addChild(aField);

		AngularContainer toolbar = new AngularContainer();
		toolbar.setLayout("horizontal");
		toolbar.setClassNames("full-width-centered-container dynamic-toolbar");
		root.addChild(toolbar);
		AngularButton createButton = new AngularButton();
		createButton.setLabel("Create");
		AngularMethodCall call = new AngularMethodCall();
		call.setMember(APPLICATION_SERVICE_TS_NAME);
		call.setMethod("postForm");
		call.addParameter("createTemplateAgreement");
		createButton.setOnclick(call);
		AngularButton cancelButton = new AngularButton();
		cancelButton.setLabel("Cancel");
		call = new AngularMethodCall();
		call.setMember(APPLICATION_SERVICE_TS_NAME);
		call.setMethod("getStaticForm");
		call.addParameter("forms/fromTemplate.json");
		cancelButton.setOnclick(call);
		toolbar.addChild(createButton);
		toolbar.addChild(cancelButton);
		AngularHiddenInputField worflowHidden = new AngularHiddenInputField();
		worflowHidden.setName(TEMPLATE_ID_ENTRY);
		worflowHidden.setInitValue(templateId);
		root.addChild(worflowHidden);
		ObjectNode response = createSuccessJson();
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

	@PostMapping("/createWorkflowAgreement")
	public JsonNode createWorkflowAgreement(@RequestBody ObjectNode json) {
		log.info("json={}", json.toPrettyString());
		try {
			String workflowId = json.has(WORKFLOW_ID_ENTRY) ? json.get(WORKFLOW_ID_ENTRY).asText() : null;
			if (!StringUtils.hasText(workflowId))
				return createErrorJson("No workflow id found in input data");
			WorkflowsApiExtension workflowsApi = new WorkflowsApiExtension(getApiClient());
			Workflow workflow = workflowsApi.getWorkflow(accessToken.getToken(), workflowId, null, null);

			AgreementCreationInfo agreementInfo = new AgreementCreationInfo();
			agreementInfo.setName(workflow.getAgreementNameInfo().getDefaultValue());
			agreementInfo.setSignatureType(SignatureTypeEnum.ESIGN);
			agreementInfo.setState(StateEnum.IN_PROCESS);
			agreementInfo.setMessage(workflow.getMessageInfo().getDefaultValue());
			agreementInfo.setWorkflowId(workflow.getId());

			workflow.getFileInfos().forEach(fInfo -> {
				String docId = Utilities.getStringValue(json, DOC_PREFIX + fInfo.getName());
				if (StringUtils.hasText(docId)) {
					FileInfo fileInfo = new FileInfo();
					fileInfo.setLibraryDocumentId(docId);
					fileInfo.setLabel(fInfo.getLabel());
					agreementInfo.addFileInfosItem(fileInfo);
				}
			});

			json.fieldNames().forEachRemaining(fName -> {
				if (fName.startsWith(FIELD_PREFIX)) {
					String value = Utilities.getStringValue(json, fName);
					if (StringUtils.hasText(value)) {
						MergefieldInfo info = new MergefieldInfo();
						info.setFieldName(fName.substring(FIELD_PREFIX.length()));
						info.setDefaultValue(value);
						agreementInfo.addMergeFieldInfoItem(info);
					}
				}
			});
			int order = 1;
			for (RecipientsListInfo rInfo : workflow.getRecipientsListInfo()) {
				String email = Utilities.getStringValue(json, PARTICIPANT_PREFIX + rInfo.getName());
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
			String sValue = Utilities.getStringValue(json, EXTERNAL_ID_ENTRY);
			if (StringUtils.hasText(sValue)) {
				ExternalId externalId = new ExternalId();
				externalId.setId(sValue);
				agreementInfo.setExternalId(externalId);
			}
			sValue = Utilities.getStringValue(json, AGREEMENT_NAME_ENTRY);
			if (StringUtils.hasText(sValue)) {
				agreementInfo.setName(sValue);
			}

			PostSignOption option = new PostSignOption();
			option.setRedirectDelay(15);
			option.setRedirectUrl("https://www.davita.com");

			agreementInfo.setPostSignOption(option);
			AgreementsApi agreementsApi = new AgreementsApi(getApiClient());
			AgreementCreationResponse agreementCreationResponse = agreementsApi.createAgreement(accessToken.getToken(),
					agreementInfo, null, null);
			log.info("Agreement created. Agreement Id: {}", agreementCreationResponse.getId());
			AgreementInfo aInfo = agreementsApi.getAgreementInfo(accessToken.getToken(),
					agreementCreationResponse.getId(), null, null, null);
			ObjectNode result = createSuccessJson(objectMapper.convertValue(aInfo, JsonNode.class));
			result.put("infoMessage", objectMapper.writeValueAsString(aInfo));
			return result;
		} catch (ApiException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage() + ". Response: " + e.getResponseBody());
		} catch (IOException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage());
		}
	}

	@PostMapping("/createTemplateAgreement")
	public JsonNode createTemplateAgreement(@RequestBody ObjectNode json) {
		log.info("json={}", json);
		try {
			String templateId = Utilities.getStringValue(json, TEMPLATE_ID_ENTRY);
			if (!StringUtils.hasText(templateId))
				return createErrorJson("No template id found in input data");
			LibraryDocumentsApiExtension libApi = new LibraryDocumentsApiExtension(getApiClient());
			FieldList list = libApi.getFormFields(accessToken.getToken(), templateId, null, null, null);

			AgreementCreationInfo agreementInfo = new AgreementCreationInfo();
			agreementInfo.setSignatureType(SignatureTypeEnum.ESIGN);
			agreementInfo.setState(StateEnum.IN_PROCESS);

			FileInfo fileInfo = new FileInfo();
			fileInfo.setLibraryDocumentId(templateId);
			agreementInfo.addFileInfosItem(fileInfo);

			list.getFields().forEach(mfInfo -> {
				String value = Utilities.getStringValue(json, FIELD_PREFIX + mfInfo.getName());
				if (StringUtils.hasText(value)) {
					MergefieldInfo info = new MergefieldInfo();
					info.setFieldName(mfInfo.getName());
					info.setDefaultValue(value);
					agreementInfo.addMergeFieldInfoItem(info);
				}
			});
			json.fieldNames().forEachRemaining(name -> {
				if (name.startsWith(PARTICIPANT_PREFIX) && !name.endsWith("-order")) {
					String email = Utilities.getStringValue(json, name);
					if (StringUtils.hasText(email)) {
						String sOrder = Utilities.getStringValue(json, name + "-order");
						int order = StringUtils.hasText(sOrder) ? Integer.parseInt(sOrder) : 1;
						ParticipantSetInfo participantSetInfo = new ParticipantSetInfo();
						participantSetInfo.setOrder(order);
						participantSetInfo.setRole(
								Utilities.getTemplatePrticipantRole(list, name.substring(PARTICIPANT_PREFIX.length())));
						participantSetInfo.setName(name.substring(PARTICIPANT_PREFIX.length()));
						ParticipantSetMemberInfo participantSetMemberInfo = new ParticipantSetMemberInfo();
						participantSetMemberInfo.setEmail(email);
						participantSetInfo.addMemberInfosItem(participantSetMemberInfo);
						agreementInfo.addParticipantSetsInfoItem(participantSetInfo);
					}
				}
			});

			String sValue = Utilities.getStringValue(json, EXTERNAL_ID_ENTRY);
			if (StringUtils.hasText(sValue)) {
				ExternalId externalId = new ExternalId();
				externalId.setId(sValue);
				agreementInfo.setExternalId(externalId);
			}
			sValue = Utilities.getStringValue(json, AGREEMENT_NAME_ENTRY);
			if (StringUtils.hasText(sValue))
				agreementInfo.setName(sValue);
			else
				agreementInfo.setName("New Agreement");

			PostSignOption option = new PostSignOption();
			option.setRedirectDelay(15);
			option.setRedirectUrl("https://www.davita.com");

			agreementInfo.setPostSignOption(option);
			AgreementsApi agreementsApi = new AgreementsApi(getApiClient());
			AgreementCreationResponse agreementCreationResponse = agreementsApi.createAgreement(accessToken.getToken(),
					agreementInfo, null, null);
			log.info("Agreement created. Agreement Id: {}", agreementCreationResponse.getId());
			AgreementInfo aInfo = agreementsApi.getAgreementInfo(accessToken.getToken(),
					agreementCreationResponse.getId(), null, null, null);
			ObjectNode result = createSuccessJson(objectMapper.convertValue(aInfo, JsonNode.class));
			result.put("infoMessage", objectMapper.writeValueAsString(aInfo));
			return result;
		} catch (ApiException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage() + ". Response: " + e.getResponseBody());
		} catch (IOException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage());
		}
	}

}
