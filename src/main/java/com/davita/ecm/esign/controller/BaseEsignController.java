package com.davita.ecm.esign.controller;

import java.io.IOException;
import java.util.Map;

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
import org.springframework.web.client.RestTemplate;

import com.davita.ecm.esign.model.EsignAccessToken;
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

	@Autowired
	protected ObjectMapper objectMapper;

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
		map.add("refresh_token", "3AAABLblqZhA2rjgUvDRp4pJxsOZEg2pzphG99g6UgAa9z5kz_YeeqH4rb9MhBCqaq255rxyR9Fw*");
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

//	private String getAccessToken()
//			throws KeyManagementException, NoSuchAlgorithmException, ClientProtocolException, IOException {
//		SSLContext context = SSLContext.getInstance("TLSv1.2");
//		context.init(null, null, null);
//
//		try (CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context).build()) {
//			HttpGet httpget = new HttpGet("http://localhost/");
//			CloseableHttpResponse response = httpClient.execute(httpget);
//			response.getCEntity().
//		}
//	}

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
	protected ObjectNode createErrorJson(String errorMessage) {
		ObjectNode root = objectMapper.createObjectNode();
		root.put("success", false);
		root.put("errorMessage", errorMessage);
		return root;
	}
}
