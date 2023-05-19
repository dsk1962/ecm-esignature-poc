package com.davita.ecm.esign.api.extension;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.davita.ecm.esign.model.extension.workflow.Workflow;
import com.google.gson.reflect.TypeToken;

import io.swagger.client.api.WorkflowsApi;
import io.swagger.client.model.ApiCallback;
import io.swagger.client.model.ApiClient;
import io.swagger.client.model.ApiException;
import io.swagger.client.model.ApiResponse;
import io.swagger.client.model.Pair;
import io.swagger.client.model.ProgressRequestBody;
import io.swagger.client.model.ProgressResponseBody;

public class WorkflowsApiExtension extends WorkflowsApi {

	public WorkflowsApiExtension(ApiClient apiClient) {
		super(apiClient);
	}

	public com.squareup.okhttp.Call getWorkflowCall(String authorization, String workflowId, String xApiUser,
			String groupId, final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/workflows/{workflowId}";

		List<Pair> localVarQueryParams = new ArrayList<>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<>();
		localVarPath = StringUtils.replace(localVarPath, "{workflowId}", workflowId);
		if (groupId != null)
			localVarQueryParams.addAll(getApiClient().parameterToPair("groupId", groupId));

		Map<String, String> localVarHeaderParams = new HashMap<>();
		if (authorization != null)
			localVarHeaderParams.put("Authorization", getApiClient().parameterToString(authorization));
		if (xApiUser != null)
			localVarHeaderParams.put("x-api-user", getApiClient().parameterToString(xApiUser));

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

	private com.squareup.okhttp.Call getWorkflowValidateBeforeCall(String authorization, String workflowId,
			String xApiUser, String groupId, final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

		// verify the required parameter 'authorization' is set
		if (authorization == null) {
			throw new ApiException("Missing the required parameter 'authorization' when calling getWorkflows(Async)");
		}

		return getWorkflowCall(authorization, workflowId, xApiUser, groupId, progressListener, progressRequestListener);
	}

	/**
	 * Retrieves workflows for a user.
	 * 
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;workflow_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;workflow_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;workflow_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param includeDraftWorkflows Include draft workflows (optional)
	 * @param includeInactiveWorkflows Include inactive workflows (optional)
	 * @param groupId The group identifier for which the workflows will be fetched (optional)
	 * @return UserWorkflows
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 */
	public Workflow getWorkflow(String authorization, String workflowId, String xApiUser, String groupId)
			throws ApiException {
		ApiResponse<Workflow> resp = getWorkflowWithHttpInfo(authorization, workflowId, xApiUser, groupId);
		Workflow workflow = resp.getData();
		workflow.setId(workflowId);
		return workflow;
	}

	/**
	 * Retrieves workflows for a user.
	 * 
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;workflow_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;workflow_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;workflow_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param includeDraftWorkflows Include draft workflows (optional)
	 * @param includeInactiveWorkflows Include inactive workflows (optional)
	 * @param groupId The group identifier for which the workflows will be fetched (optional)
	 * @return ApiResponse&lt;UserWorkflows&gt;
	 * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
	 */
	public ApiResponse<Workflow> getWorkflowWithHttpInfo(String authorization, String workflowId, String xApiUser,
			String groupId) throws ApiException {
		com.squareup.okhttp.Call call = getWorkflowValidateBeforeCall(authorization, workflowId, xApiUser, groupId,
				null, null);
		Type localVarReturnType = new TypeToken<Workflow>() {
		}.getType();
		return getApiClient().execute(call, localVarReturnType);
	}

	/**
	 * Retrieves workflows for a user. (asynchronously)
	 * 
	 * @param authorization An &lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc()\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;OAuth Access Token&lt;/a&gt; with scopes:&lt;ul&gt;&lt;li style&#x3D;&#39;list-style-type: square&#39;&gt;&lt;a href&#x3D;\&quot;#\&quot; onclick&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;workflow_read&#39;)\&quot; oncontextmenu&#x3D;\&quot;this.href&#x3D;oauthDoc(&#39;workflow_read&#39;)\&quot; target&#x3D;\&quot;oauthDoc\&quot;&gt;workflow_read&lt;/a&gt;&lt;/li&gt;&lt;/ul&gt;in the format &lt;b&gt;&#39;Bearer {accessToken}&#39;. (required)
	 * @param xApiUser The userId or email of API caller using the account or group token in the format &lt;b&gt;userid:{userId} OR email:{email}.&lt;/b&gt; If it is not specified, then the caller is inferred from the token. (optional)
	 * @param includeDraftWorkflows Include draft workflows (optional)
	 * @param includeInactiveWorkflows Include inactive workflows (optional)
	 * @param groupId The group identifier for which the workflows will be fetched (optional)
	 * @param callback The callback to be executed when the API call finishes
	 * @return The request call
	 * @throws ApiException If fail to process the API call, e.g. serializing the request body object
	 */
	public com.squareup.okhttp.Call getWorkflowAsync(String authorization, String workflowId, String xApiUser,
			String groupId, final ApiCallback<Workflow> callback) throws ApiException {

		ProgressResponseBody.ProgressListener progressListener = null;
		ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

		if (callback != null) {
			progressListener = callback::onDownloadProgress;
			progressRequestListener = callback::onUploadProgress;
		}

		com.squareup.okhttp.Call call = getWorkflowValidateBeforeCall(authorization, workflowId, xApiUser, groupId,
				progressListener, progressRequestListener);
		Type localVarReturnType = new TypeToken<Workflow>() {
		}.getType();
		getApiClient().executeAsync(call, localVarReturnType, callback);
		return call;
	}
}
