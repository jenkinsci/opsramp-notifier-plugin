package org.jenkinsci.plugins.opsrampnotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author Srini T
 *
 */
public class OpsRampNotifierClient implements OpsRampNotifierConstants {
	
	private String apiKey 		= null;
	private String apiSecret 	= null;
	private String apiBaseURI 	= null;
	private String tenantId		= null;
	
	/**
	 * Constructor
	 */
	public OpsRampNotifierClient(String apiBaseURI, String tenantId, String apiKey, String apiSecret) {
		this.apiBaseURI = apiBaseURI;
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.tenantId = tenantId;
	}
	
	/** Method to Create OpsRamp alert using json payload
	 * @param jsonPayload
	 * @return Status code 
	 * @throws Exception
	 */
	public String createAlert(String jsonPayload) throws Exception {
		if(jsonPayload == null || jsonPayload.isEmpty()) {
			throw new Exception(INVALID_JSON_PAYLOAD);
		}
		
		String alertsEndPointURL = this.apiBaseURI + OAUTH2_START_URL + this.tenantId + ALERTS;
		return postRequest(alertsEndPointURL, jsonPayload);
	}
	
	/** POST request to OpsRamp 
	 * @param url
	 * @param jsonPayload
	 * @return
	 * @throws Exception
	 */
	private String postRequest(String url, String jsonPayload) throws Exception {
		String response = null;
		try {
			HttpPost httpMethod = new HttpPost(url);
			setMethodHeaders(httpMethod);
			HttpEntity entity = new StringEntity(jsonPayload.toString(), UTF_8);
			httpMethod.setEntity(entity);
			response = handleHttpMethod(httpMethod);
			
			return response;
		} catch(Exception e) {
			throw new Exception(POST_FAILED + e.getMessage());
		}
	}
	
	/** Method to set Required header Parameters
	 * @param httpMethod
	 */
	private void setMethodHeaders(HttpRequestBase httpMethod) throws Exception {
		httpMethod.setHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON);
		httpMethod.setHeader(ACCEPT, MediaType.APPLICATION_JSON);
		String accessToken = generateAccessToken();
		httpMethod.setHeader(AUTHORIZATION, BEARER + accessToken);
	}
	
	/** Handle HttpRequest
	 * @param httpMethod
	 * @throws Exception
	 */
	private String handleHttpMethod(HttpRequestBase httpMethod) throws Exception {
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = builder.build();
		HttpResponse httpResponse = null;
		try {
			httpclient = wrapClient(builder);
			httpResponse = httpclient.execute(httpMethod);
			
			return parseResponse(httpResponse);
		} catch(Exception e) {
			throw new Exception(HTTP_REQ_FAILED + e);
		} finally {
			httpclient.close();
		} 
	}
	
	/** Method to generated Access Token for OAUTH
	 * @return
	 */
	private String generateAccessToken() throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		HttpPost post = new HttpPost(this.apiBaseURI + ACCESS_TOKEN_PATH);

		List<BasicNameValuePair> parametersBody = new ArrayList<BasicNameValuePair>();
		
		parametersBody.add(new BasicNameValuePair(GRANT_TYPE, GRANT_TYPE_CLIENT_CREDS));
		parametersBody.add(new BasicNameValuePair(CLIENT_ID, this.apiKey));
		parametersBody.add(new BasicNameValuePair(CLIENT_SECRET, this.apiSecret));
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient client = builder.build();
		client = wrapClient(builder);
		HttpResponse response = null;
		String result = "";
		try {
			post.setEntity(new UrlEncodedFormEntity(parametersBody, UTF_8));
			response = client.execute(post);
			
			BufferedReader buffer = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        String s = "";
	        while ((s = buffer.readLine()) != null) {
	        	result += s;
	        }
	        
	        if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) { 
	        	throw new Exception(result);
	        }
      		map = new Gson().fromJson(result, new TypeToken<HashMap<String, String>>(){}.getType());
      		return map.get(ACCESS_TOKEN);
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static CloseableHttpClient wrapClient(HttpClientBuilder builder) {
    	try {
	    	SSLContext ctx = SSLContext.getInstance("TLS");
	    	X509TrustManager tm = new X509TrustManager() {

	    	public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    	}

	    	public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    	}

	    	public X509Certificate[] getAcceptedIssuers() {
	    	return null;
	    	}
	    	};
	    	ctx.init(null, new TrustManager[]{tm}, null);
	    	
	    	SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", ssf).build();
			
			PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager(registry);
			CloseableHttpClient httpclient = builder.setConnectionManager(ccm).build();
			
	    	return httpclient;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
    }
	
	/** Parse the status message response
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private String parseResponse(HttpResponse  response) throws Exception {
		int statusCode = response.getStatusLine().getStatusCode();
		
		// this is for Oauth2 special case if 
		if(statusCode == HTTP_ACCESS_TOKEN_EXPIRED) {
    		return ACCESS_TOKEN_EXPIRED;
    	} 
		
		InputStream in = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + EMPTY_STRING);
		}
		
		Gson gson = new Gson();
		Map<String,String> map1 = new HashMap<String,String>();
		map1.put(STATUS, "" + statusCode);
		map1.put(STATUS_MESSAGE, sb.toString());
		return  gson.toJson(map1);
	}
}
