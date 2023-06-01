package com.davita.ecm.esign.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.davita.ecm.esign.api.extension.LibraryDocumentsApiExtension;
import com.davita.ecm.esign.api.extension.SearchApi;
import com.davita.ecm.esign.api.extension.WorkflowsApiExtension;
import com.davita.ecm.esign.model.AngularButton;
import com.davita.ecm.esign.model.AngularContainer;
import com.davita.ecm.esign.model.AngularMethodCall;
import com.davita.ecm.esign.model.AngularTable;
import com.davita.ecm.esign.model.AngularToolbarColumn;
import com.davita.ecm.esign.model.AngularWidget;
import com.davita.ecm.esign.model.extension.librarydocument.FieldList;
import com.davita.ecm.esign.model.extension.search.AgreementAssetsCriteria;
import com.davita.ecm.esign.model.extension.search.SearchRequest;
import com.davita.ecm.esign.model.extension.search.SearchResponse;
import com.davita.ecm.esign.model.extension.workflow.RecipientsListInfo;
import com.davita.ecm.esign.model.extension.workflow.Workflow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.client.api.AgreementsApi;
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
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AgreementController  extends BaseEsignController {
	@PostMapping("/createTemplateAgreement")
	public JsonNode createTemplateAgreement(@RequestBody ObjectNode json) {
		log.info("json={}", json);
		try {
			String templateId = utilities.getStringValue(json, TEMPLATE_ID_ENTRY);
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
				String value = utilities.getStringValue(json, FIELD_PREFIX + mfInfo.getName());
				if (StringUtils.hasText(value)) {
					MergefieldInfo info = new MergefieldInfo();
					info.setFieldName(mfInfo.getName());
					info.setDefaultValue(value);
					agreementInfo.addMergeFieldInfoItem(info);
				}
			});
			json.fieldNames().forEachRemaining(name -> {
				if (name.startsWith(PARTICIPANT_PREFIX) && !name.endsWith("-order")) {
					String email = utilities.getStringValue(json, name);
					if (StringUtils.hasText(email)) {
						String sOrder = utilities.getStringValue(json, name + "-order");
						int order = StringUtils.hasText(sOrder) ? Integer.parseInt(sOrder) : 1;
						ParticipantSetInfo participantSetInfo = new ParticipantSetInfo();
						participantSetInfo.setOrder(order);
						participantSetInfo.setRole(
								utilities.getTemplatePrticipantRole(list, name.substring(PARTICIPANT_PREFIX.length())));
						participantSetInfo.setName(name.substring(PARTICIPANT_PREFIX.length()));
						ParticipantSetMemberInfo participantSetMemberInfo = new ParticipantSetMemberInfo();
						participantSetMemberInfo.setEmail(email);
						participantSetInfo.addMemberInfosItem(participantSetMemberInfo);
						agreementInfo.addParticipantSetsInfoItem(participantSetInfo);
					}
				}
			});

			String sValue = utilities.getStringValue(json, EXTERNAL_ID_ENTRY);
			if (StringUtils.hasText(sValue)) {
				ExternalId externalId = new ExternalId();
				externalId.setId(sValue);
				agreementInfo.setExternalId(externalId);
			}
			sValue = utilities.getStringValue(json, AGREEMENT_NAME_ENTRY);
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
			result.put("infoMessage", utilities.toHtmlJsonString(aInfo));
			return result;
		} catch (ApiException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage() + ". Response: " + e.getResponseBody());
		} catch (IOException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage());
		}
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
				String docId = utilities.getStringValue(json, DOC_PREFIX + fInfo.getName());
				if (StringUtils.hasText(docId)) {
					FileInfo fileInfo = new FileInfo();
					fileInfo.setLibraryDocumentId(docId);
					fileInfo.setLabel(fInfo.getLabel());
					agreementInfo.addFileInfosItem(fileInfo);
				}
			});

			json.fieldNames().forEachRemaining(fName -> {
				if (fName.startsWith(FIELD_PREFIX)) {
					String value = utilities.getStringValue(json, fName);
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
				String email = utilities.getStringValue(json, PARTICIPANT_PREFIX + rInfo.getName());
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
			String sValue = utilities.getStringValue(json, EXTERNAL_ID_ENTRY);
			if (StringUtils.hasText(sValue)) {
				ExternalId externalId = new ExternalId();
				externalId.setId(sValue);
				agreementInfo.setExternalId(externalId);
			}
			sValue = utilities.getStringValue(json, AGREEMENT_NAME_ENTRY);
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
			result.put("infoMessage", utilities.toHtmlJsonString(aInfo));
			return result;
		} catch (ApiException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage() + ". Response: " + e.getResponseBody());
		} catch (IOException e) {
			log.error("Create agreement from workflow failed.", e);
			return createErrorJson(e.getMessage());
		}
	}
	@PostMapping(path = "/agreement/search", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonNode searchAgreements(@RequestBody ObjectNode json) {
		log.info("json={}", json);
		try {
			// SearchAPI
			SearchApi searchApi = new SearchApi(getApiClient());
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.getScope().add("AGREEMENT_ASSETS");
			AgreementAssetsCriteria assetsCriteria = new AgreementAssetsCriteria();
			String externalId = utilities.getStringValue(json, "externalId");
			if (StringUtils.hasText(externalId))
				assetsCriteria.addExternalId(externalId);
			String status = utilities.getStringValue(json, "status");
			if (StringUtils.hasText(status))
				assetsCriteria.addStatus(status);
			String query = utilities.getStringValue(json, "query");
			if (StringUtils.hasText(query))
				searchRequest.setQuery(query);
			searchRequest.setAgreementAssetsCriteria(assetsCriteria);
			SearchResponse response = searchApi.search(accessToken.getToken(), searchRequest, null, null);

			AngularContainer root = new AngularContainer();
			root.setClassNames("fixed-width-centered-container");
			AngularContainer body = new AngularContainer();
			body.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
			root.addChild(body);

			AngularContainer container = new AngularContainer();
			container.setLayout("horizontal");
			container.setClassNames(FULLWIDTH_CENTERED_CONTAINER_CSS_NAME);
			container.setHtml("<h2>Seacrh Results</h2>");
			body.addChild(container);
			AngularTable table = new AngularTable();
			AngularToolbarColumn toolbarColumn = new AngularToolbarColumn();
			table.addColumn(toolbarColumn);
			AngularButton toolbarButton = new AngularButton();
			toolbarColumn.addButton(toolbarButton);
			toolbarButton.setClassNames("p-button-rounded p-button-success p-button-text");
			ObjectNode config = objectMapper.createObjectNode();
			config.put("icon", "pi pi-search");
			toolbarButton.setConfig(config);
			
			AngularWidget column = new AngularWidget();
			column.setLabel("Name");
			column.setName("name");
			table.addColumn(column);
			column = new AngularWidget();
			column.setLabel("Date Created");
			column.setName("createdDate");
			table.addColumn(column);
			column = new AngularWidget();
			column.setLabel("Date Modified");
			column.setName("modifiedDate");
			table.addColumn(column);
			column = new AngularWidget();
			column.setLabel("External Id");
			column.setName("externalId");
			table.addColumn(column);
			column = new AngularWidget();
			column.setLabel("Status");
			column.setName("status");
			table.addColumn(column);
			body.addChild(table);
			table.setData(response.getAgreementAssetsResults().getAgreementAssetsResultList());
			AngularContainer toolbar = new AngularContainer();
			toolbar.setLayout("horizontal");
			toolbar.setClassNames("full-width-centered-container dynamic-toolbar");
			root.addChild(toolbar);
			AngularButton button = new AngularButton();
			toolbar.addChild(button);
			button.setLabel("Back");
			AngularMethodCall call = new AngularMethodCall();
			call.setMember(APPLICATION_SERVICE_TS_NAME);
			call.setMethod("getStaticForm");
			call.addParameter("forms/searchTemplate.json");
			button.setOnclick(call);
			ObjectNode result = createSuccessJson();
			result.set(FORM_DEFINITION, objectMapper.valueToTree(root));
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
