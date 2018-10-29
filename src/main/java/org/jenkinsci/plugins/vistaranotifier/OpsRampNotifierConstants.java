/*
 * The MIT License
 *
 * Copyright  2016 VistaraIT, Inc. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.opsrampnotifier;

/**
 * @author Srini T
 *
 */
public interface OpsRampNotifierConstants {
	
	//Constants
	public final static String JENKINS					= "Jenkins";
	public final static String STARTED 					= "Started";
	public final static String SUCCESS 					= "Success";
	public final static String FAILED 					= "Failed";
	public final static String DEFAULT_NODE 			= "master";
	public final static String ANONYMOUS_USER			= "anonymous";
	public final static String VISTARA_DISPLAY_NAME 	= "Notify Vistara";
	
	//Tags
	public final static String BUILD_NAME 				= "Name";
	public final static String BUILD_NUMBER 			= "Build number";
	public final static String BUILD_URL 				= "URL";
	public final static String BUILD_FULL_URL 			= "Full URL";
	public final static String BUILD_STATE 				= "state";
	public final static String BUILD_SERVICE_NAME 		= "serviceName";
	public final static String BUILD_USER 				= "state";
	public final static String BUILD_AUTHORS 			= "Authors";
	public final static String BUILD_COMMIT_DETAILS		= "Commit Details";
	public final static String SUBJECT 					= "subject";
	public final static String DESCRIPTION 				= "description";
	public final static String HOSTS 					= "hosts";
	
	public final static String EMPTY_STR				= "";
	public final static String COMMA					= ",";
	public final static String COLON					= ":";
	public final static String SPACE					= " ";
	public final static String NEW_LINE					= "\n";
	
	//Parameters
	public final static String CLIENT_ID_PARAM			= "opsrampClientId";
	public final static String API_KEY_PARAM			= "opsrampApiKey";
	public final static String API_SECRET_PARAM			= "opsrampApiSecret";
	public final static String BASE_URI_PARAM			= "opsrampBaseURI";
	
	//Error messages
	public final static String TOKEN_ERR_MSG			= "Please specify a valid OpsRamp token here";
	public final static String API_KEY_ERR_MSG			= "Please specify a valid OpsRamp API key here";
	public final static String API_SECRET_ERR_MSG		= "Please specify a valid OpsRamp API secret here";
	public final static String CLIENT_ID_ERR_MSG		= "Please specify a valid OpsRamp clientId here";
	public final static String BASE_URI_ERR_MSG			= "Please specify a valid OpsRamp base URI here";
	
	public final static String DESC_BUILD_USER			= "Build started by";
	public final static String DESC_CHANGE_LOG			= "Change log";
	public final static String DESC_SUMMARY				= "Summary";
	public final static String DEFAULT_METRIC			= "Build progress";
	public final static String FILE_PATH				= "Modified file(s)";
	public final static String REV_STR					= "Revision";
	public final static String BY_STR					= "by";
	public final static String CONSOLE_LOG_MSG1			= "Last";
	public final static String CONSOLE_LOG_MSG2			= "lines of console output";
	public final static int MAX_LINES					= 100;
	public final static int MIN_LINES					= 20;
	
	//JSON
	public static final String STATUS 					= "status";
	public static final String STATUS_MESSAGE 			= "status_message";
	public static final String EMPTY_STRING				= "";
	
	//Error messages
	public static final String INVALID_JSON_PAYLOAD		= "JsonPayload should not be null or empty.";
	public static final String INVALID_ACCESS_TOKEN		= "Access Token should not be null or empty.";
	public static final String POST_FAILED				= "Failed to POST data to Vistara, Reson :";
	public static final String HTTP_REQ_FAILED			= "Failed to send http request, Reason: ";
	
	//API URLs
	public static final String VISTAR_API_BASE_URI		= "https://api.vistara.io";
	public static final String OAUTH2_START_URL			= "/api/v2/tenants/";
	public static final String ACCESS_TOKEN_PATH        = "/auth/oauth/token";
	public static final String ALERTS        			= "/alerts";
	
	//HTTP
	public static final String UTF_8					= "UTF-8";
	public static final String ACCEPT					= "Accept";
	public static final String CONTENT_TYPE				= "Content-Type";
	public static final String AUTHORIZATION			= "Authorization";
	public static final String BEARER					= "bearer ";
	
	public static final String ACCESS_TOKEN 			= "access_token";
	public static final String CLIENT_ID 				= "client_id";
	public static final String CLIENT_SECRET 			= "client_secret";
	public static final String GRANT_TYPE 				= "grant_type";
	public static final String GRANT_TYPE_CLIENT_CREDS 	= "client_credentials";
	
	public static final String INFO						= "Info";
	public static final String WARNING					= "Warning";
	public static final String CRITICAL					= "Critical";
	public static final String OK						= "Ok";
	
	public static final int HTTP_OK 					= 200;
	public static final int HTTP_FORBIDDEN 				= 403;
	public static final int HTTP_UNAUTHORIZED 			= 401;
	public static final int HTTP_ACCESS_TOKEN_EXPIRED 	= 407;
	
	public static final String ACCESS_TOKEN_EXPIRED 	= "access token expired";
		
}