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
package org.jenkinsci.plugins.vistaranotifier;

/**
 * @author Srini T
 *
 */
public interface VistaraNotifierConstants {
	
	//Constants
	public final static String JENKINS					= "Jenkins";
	public final static String STARTED 					= "Started";
	public final static String SUCCESS 					= "Success";
	public final static String FAILED 					= "Failed";
	public final static String DEFAULT_NODE 			= "master";
	public final static String ANONYMOUS_USER			= "anonymous";
	public final static String VISTARA_DISPLAY_NAME 	= "Vistara notification";
	
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
	public final static String CLIENT_ID_PARAM			= "vistaraClientId";
	public final static String API_KEY_PARAM			= "vistaraApiKey";
	public final static String API_SECRET_PARAM			= "vistaraApiSecret";
	public final static String BASE_URI_PARAM			= "vistaraBaseURI";
	
	//Error messages
	public final static String TOKEN_ERR_MSG			= "Please specify a valid Vistara token here";
	public final static String API_KEY_ERR_MSG			= "Please specify a valid Vistara API key here";
	public final static String API_SECRET_ERR_MSG		= "Please specify a valid Vistara API secret here";
	public final static String CLIENT_ID_ERR_MSG		= "Please specify a valid Vistara clientId here";
	public final static String BASE_URI_ERR_MSG			= "Please specify a valid Vistara base URI here";
	
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
}