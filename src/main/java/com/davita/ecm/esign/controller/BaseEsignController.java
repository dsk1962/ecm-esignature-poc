package com.davita.ecm.esign.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.davita.ecm.esign.model.AngularCheckbox;
import com.davita.ecm.esign.model.AngularCombobox;
import com.davita.ecm.esign.model.AngularDateField;
import com.davita.ecm.esign.model.AngularInputField;
import com.davita.ecm.esign.model.AngularNumericField;
import com.davita.ecm.esign.model.AngularTextField;
import com.davita.ecm.esign.model.EsignAccessToken;
import com.davita.ecm.esign.model.Option;
import com.davita.ecm.esign.model.Utilities;
import com.davita.ecm.esign.model.extension.librarydocument.Field;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.client.api.BaseUrisApi;
import io.swagger.client.model.ApiClient;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.baseUris.BaseUriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseEsignController {

	public static final String ADOBE_CLIENT_HEADER = "X-ADOBESIGN-CLIENTID";
	public static final String FIELD_PREFIX = "$FIELD$";
	public static final String DOC_PREFIX = "$DOC$";
	public static final String PARTICIPANT_PREFIX = "$PCNT$";

	public static final String APPLICATION_SERVICE_TS_NAME = "applicationServiceService";
	public static final String FULLWIDTH_CENTERED_CONTAINER_CSS_NAME = "full-width-centered-container";

	public static final String EMAIL_PATTERN = "^[\\w\\-\\.]+@([\\w\\-]+\\.)+[\\w\\-]{2,4}$";

	public static final String FORM_DEFINITION = "formDefinition";
	public static final String FORM_REQUEST = "formRequest";
	public static final String WORKFLOW_ID_ENTRY = "$$workflowId$$";
	public static final String TEMPLATE_ID_ENTRY = "$$templateId$$";
	public static final String EXTERNAL_GROUP_ID_ENTRY = "$$externalGroupId$$";
	public static final String EXTERNAL_ID_ENTRY = "$$agreement_externalId$$";
	public static final String AGREEMENT_NAME_ENTRY = "$$agreement_name$$";

	public static final String X_USER = "email:dmitry.kuvshinov@davita.com";

	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected Utilities utilities;

	protected EsignAccessToken accessToken = new EsignAccessToken();

	@Value("${esign.base.url}")
	private String baseUrl;

	private void getAccessToken() throws IOException {
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContexts.createDefault(),
				new String[] { "TLSv1.2" }, null, new NoopHostnameVerifier());

		HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(sslsf).build();
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(factory);
		HttpHeaders headers = new HttpHeaders();
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "refresh_token");
		map.add("client_id", "CBJCHBCAABAABBba40-L6myrU3C3MamBPk-Ku6LNh15O");
		map.add("client_secret", "UWBiKTv7ymdX8LXCQ1WcUkmF6t81tHx1");
		map.add("refresh_token", "3AAABLblqZhBkTC5BUx20e6S-Si1xDpAfjgl_rl15NWzWvqzVl66JpecClMfnVqy-yfa4xZWz9P0*"); // Dmitry partner app
//		map.add("refresh_token", "3AAABLblqZhBwWMQ0B0dOAabFbYyI-P57gEqpzINuZ4o4gpnRcACAuzCSepUI5Ou_fUoC1R8CWls*"); // Manesh partner app
//		map.add("refresh_token", "3AAABLblqZhA2rjgUvDRp4pJxsOZEg2pzphG99g6UgAa9z5kz_YeeqH4rb9MhBCqaq255rxyR9Fw*");// Manesh customer app
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/oauth/v2/refresh", request,
				String.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			JsonNode node = objectMapper.readTree(response.getBody());
			accessToken.setToken("Bearer " + node.get("access_token").asText());
			log.debug("Token: {}", accessToken.getToken());
			accessToken.setExpirationTime(System.currentTimeMillis() + node.get("expires_in").asLong()*1000L);
		}
	}

	protected ApiClient getApiClient() throws ApiException, IOException {

		if (accessToken.isExpired())
			getAccessToken();
		ApiClient apiClient = new ApiClient();
		String endpointUrl = "/api/rest/v6";
		apiClient.setBasePath(baseUrl + endpointUrl);

		// Get the baseUris for the user and set it in apiClient.
		BaseUrisApi baseUrisApi = new BaseUrisApi(apiClient);
		BaseUriInfo baseUriInfo = baseUrisApi.getBaseUris(accessToken.getToken());
		apiClient.setBasePath(baseUriInfo.getApiAccessPoint() + endpointUrl);
		return apiClient;
	}
	
	protected ObjectNode createSuccessJson(JsonNode result) {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("success", true);
		root.set("result", result != null ? result:objectMapper.createObjectNode());
		return root;
	}
	protected ObjectNode createSuccessJson() {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("success", true);
		root.set("result", objectMapper.createObjectNode());
		return root;
	}
	protected ObjectNode createSuccessJson(String result) {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("success", true);
		root.put("result", result);
		return root;
	}
	protected ObjectNode createErrorJson(String errorMessage) {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("success", false);
		root.put("errorMessage", errorMessage);
		return root;
	}
	protected AngularInputField createAngularField(Field field) {
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
}
