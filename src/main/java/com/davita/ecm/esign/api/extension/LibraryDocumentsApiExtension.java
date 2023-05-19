package com.davita.ecm.esign.api.extension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.davita.ecm.esign.model.extension.librarydocument.FieldList;
import com.google.gson.reflect.TypeToken;

import io.swagger.client.api.LibraryDocumentsApi;
import io.swagger.client.model.ApiCallback;
import io.swagger.client.model.ApiClient;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.ApiResponse;
import io.swagger.client.model.Pair;
import io.swagger.client.model.ProgressRequestBody;
import io.swagger.client.model.ProgressResponseBody;
import io.swagger.client.model.libraryDocuments.LibraryDocumentEventList;

public class LibraryDocumentsApiExtension extends LibraryDocumentsApi {

	public LibraryDocumentsApiExtension(ApiClient apiClient) {
		super(apiClient);
	}

	/**
	 * Build call for getFormFields
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;library_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param libraryDocumentId The document identifier, as retrieved from the API to fetch library documents. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param xOnBehalfOfUser The userId or email in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; of the user that has shared his/her account (optional)
	 * @param ifNoneMatch Pass the value of the e-tag header obtained from the previous response to the same request to get a RESOURCE_NOT_MODIFIED(304) if the resource hasn&#39;t changed. (optional)
	 * @param progressListener Progress listener
	 * @param progressRequestListener Progress request listener
	 * @return Call to execute
	 * @throws ApiException If fail to serialize the request body object
	 */
	public com.squareup.okhttp.Call getFormFieldsCall(String authorization, String libraryDocumentId, String xApiUser,
			String xOnBehalfOfUser, String ifNoneMatch, final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/libraryDocuments/{libraryDocumentId}/formFields"
				.replace("{" + "libraryDocumentId" + "}", getApiClient().escapeString(libraryDocumentId));

		List<Pair> localVarQueryParams = new ArrayList<>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<>();

		Map<String, String> localVarHeaderParams = new HashMap<>();
		if (authorization != null)
			localVarHeaderParams.put("Authorization", getApiClient().parameterToString(authorization));
		if (xApiUser != null)
			localVarHeaderParams.put("x-api-user", getApiClient().parameterToString(xApiUser));
		if (xOnBehalfOfUser != null)
			localVarHeaderParams.put("x-on-behalf-of-user", getApiClient().parameterToString(xOnBehalfOfUser));
		if (ifNoneMatch != null)
			localVarHeaderParams.put("If-None-Match", getApiClient().parameterToString(ifNoneMatch));

		Map<String, Object> localVarFormParams = new HashMap<>();

		final String[] localVarAccepts = { "application/json" };
		final String localVarAccept = getApiClient().selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null)
			localVarHeaderParams.put("Accept", localVarAccept);

		final String[] localVarContentTypes = {

		};
		final String localVarContentType = getApiClient().selectHeaderContentType(localVarContentTypes);
		localVarHeaderParams.put("Content-Type", localVarContentType);

		if (progressListener != null) {
			getApiClient().getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
				@Override
				public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain)
						throws IOException {
					com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
					return originalResponse.newBuilder()
							.body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
				}
			});
		}

		String[] localVarAuthNames = new String[] {};
		return getApiClient().buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
	}

	private com.squareup.okhttp.Call getFormFieldsValidateBeforeCall(String authorization, String libraryDocumentId,
			String xApiUser, String xOnBehalfOfUser, String ifNoneMatch,
			final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

		// verify the required parameter 'authorization' is set
		if (authorization == null) {
			throw new ApiException("Missing the required parameter 'authorization' when calling getFormFields(Async)");
		}

		// verify the required parameter 'libraryDocumentId' is set
		if (libraryDocumentId == null) {
			throw new ApiException(
					"Missing the required parameter 'libraryDocumentId' when calling getFormFields(Async)");
		}

		return getFormFieldsCall(authorization, libraryDocumentId, xApiUser, xOnBehalfOfUser, ifNoneMatch,
				progressListener, progressRequestListener);

	}

	/**
	 * Retrieves the events information for a library document.
	 * 
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;library_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param libraryDocumentId The document identifier, as retrieved from the API to fetch library documents. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param xOnBehalfOfUser The userId or email in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; of the user that has shared his/her account (optional)
	 * @param ifNoneMatch Pass the value of the e-tag header obtained from the previous response to the same request to get a RESOURCE_NOT_MODIFIED(304) if the resource hasn&#39;t changed. (optional)
	 * @return LibraryDocumentEventList
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 */
	public FieldList getFormFields(String authorization, String libraryDocumentId, String xApiUser,
			String xOnBehalfOfUser, String ifNoneMatch) throws ApiException {
		ApiResponse<FieldList> resp = getFormFieldsWithHttpInfo(authorization, libraryDocumentId, xApiUser,
				xOnBehalfOfUser, ifNoneMatch);
		return resp.getData();
	}

	/**
	 * Retrieves the events information for a library document.
	 * 
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;library_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param libraryDocumentId The document identifier, as retrieved from the API to fetch library documents. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param xOnBehalfOfUser The userId or email in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; of the user that has shared his/her account (optional)
	 * @param ifNoneMatch Pass the value of the e-tag header obtained from the previous response to the same request to get a RESOURCE_NOT_MODIFIED(304) if the resource hasn&#39;t changed. (optional)
	 * @return ApiResponse&lt;LibraryDocumentEventList&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 */
	public ApiResponse<FieldList> getFormFieldsWithHttpInfo(String authorization, String libraryDocumentId,
			String xApiUser, String xOnBehalfOfUser, String ifNoneMatch) throws ApiException {
		com.squareup.okhttp.Call call = getFormFieldsValidateBeforeCall(authorization, libraryDocumentId, xApiUser,
				xOnBehalfOfUser, ifNoneMatch, null, null);
		Type localVarReturnType = new TypeToken<FieldList>() {
		}.getType();
		return getApiClient().execute(call, localVarReturnType);
	}

	/**
	 * Retrieves the events information for a library document. (asynchronously)
	 * 
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;library_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;library_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param libraryDocumentId The document identifier, as retrieved from the API to fetch library documents. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param xOnBehalfOfUser The userId or email in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; of the user that has shared his/her account (optional)
	 * @param ifNoneMatch Pass the value of the e-tag header obtained from the previous response to the same request to get a RESOURCE_NOT_MODIFIED(304) if the resource hasn&#39;t changed. (optional)
	 * @param callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 */
	public com.squareup.okhttp.Call getFormFieldsAsync(String authorization, String libraryDocumentId, String xApiUser,
			String xOnBehalfOfUser, String ifNoneMatch, final ApiCallback<LibraryDocumentEventList> callback)
			throws ApiException {

		ProgressResponseBody.ProgressListener progressListener = null;
		ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

		if (callback != null) {
			progressListener = callback::onDownloadProgress;
			progressRequestListener = callback::onUploadProgress;
		}

		com.squareup.okhttp.Call call = getFormFieldsValidateBeforeCall(authorization, libraryDocumentId, xApiUser,
				xOnBehalfOfUser, ifNoneMatch, progressListener, progressRequestListener);
		Type localVarReturnType = new TypeToken<LibraryDocumentEventList>() {
		}.getType();
		getApiClient().executeAsync(call, localVarReturnType, callback);
		return call;
	}
}
