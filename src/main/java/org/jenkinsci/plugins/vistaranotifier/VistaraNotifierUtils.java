package org.jenkinsci.plugins.vistaranotifier;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.vistara.sdk.dto.alert.Alert;
import com.vistara.sdk.dto.device.Device;
import com.vistara.sdk.utils.APIConstants;
import com.vistara.sdk.utils.TimeUtils;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**A Util class to prepare required objects 
 * @author Srini T
 *
 */
public class VistaraNotifierUtils implements VistaraNotifierConstants {
	
	/** Prepare Vistara alert object 
	 * @param build
	 * @param state
	 * @return
	 */
	public static Alert prepareVistaraAlert(final AbstractBuild<?, ?> build, final String state) {
    	Alert vistaraAlert = new Alert();
    	
    	vistaraAlert.setSubject(prepareAlertSubject(build, state));
    	vistaraAlert.setDescription(prepareAlertDescription(build, vistaraAlert.getSubject()));
    	vistaraAlert.setDevice(prepareHost(build));
    	vistaraAlert.setAlertTime(TimeUtils.getDateTime(TimeUtils.DB_DATE_FORMAT, new Date()));
    	vistaraAlert.setServiceName(DEFAULT_METRIC);
    	vistaraAlert.setCurrentState(prepareAlertState(state));
    	vistaraAlert.setApp(JENKINS);
    	vistaraAlert.setExtAlertId(String.valueOf(build.getNumber()));
    	vistaraAlert.setUniqueId(String.valueOf(build.getNumber()));
    	
    	return vistaraAlert;
    }
	
	/** Prepare Vistara alert subject
	 * @param build
	 * @param state
	 * @return
	 */
	public static String prepareAlertSubject(final AbstractBuild<?, ?> build, final String state) {
    	String subject = build.getFullDisplayName() + SPACE;
        if(state.equalsIgnoreCase(FAILED)) {
        	subject += build.getResult();
        } else {
        	subject += state;
        }
        
        return subject;
    }
	
	/** Prepare Vistara alert description
	 * @param build
	 * @param subject
	 * @return
	 */
	public static String prepareAlertDescription(final AbstractBuild<?, ?> build, final String subject) {
    	final AbstractBuild<?, ?> rootBuild = build.getRootBuild();
    	
    	StringBuffer description = new StringBuffer(subject + NEW_lINE);
    	 
    	//Build triggered by
        List<Cause> causes = build.getCauses();
        String buildUser = null;
        if (causes.size() > 0 && (causes.get(0) instanceof Cause.UserIdCause)){
        	buildUser =  ((UserIdCause)causes.get(0)).getUserName();
        } else {
        	buildUser = ANONYMOUS_USER;
        }
         
        //Change log details
        StringBuffer changeLog = new StringBuffer(EMPTY_STR);
        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = rootBuild.getChangeSet();
        List<ChangeLogSet.Entry> entries = new LinkedList<ChangeLogSet.Entry>();
        for(Object o : changeSet.getItems()) {
        	ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
            entries.add(entry);
        }
         
        if(entries.isEmpty()) {
            if(build.getDescription() != null && !build.getDescription().equals(EMPTY_STR)) {
            	changeLog.append(build.getDescription());
            } else { 
             	changeLog.append(causes.get(0).getShortDescription());
            }
        } else {
        	String authors = EMPTY_STR;
            String commitAndMessages = EMPTY_STR;
            for(ChangeLogSet.Entry entry : entries) {
            	authors += entry.getAuthor().getDisplayName() + COMMA;
                commitAndMessages += entry.getCommitId() + COLON + entry.getMsg() + COMMA;
            }
            changeLog.append(BUILD_AUTHORS + COLON + SPACE + authors);
            changeLog.append(BUILD_COMMIT_DETAILS + COLON + SPACE + commitAndMessages);
        }
         
        description.append(DESC_BUILD_USER + COLON + SPACE + buildUser + NEW_lINE)
        .append(DESC_CHANGE_LOG + COLON + SPACE + changeLog.toString() + NEW_lINE)
        .append(BUILD_NAME + COLON + SPACE + build.getProject().getName() + NEW_lINE)
        .append(BUILD_NUMBER + COLON + SPACE + build.getNumber() + NEW_lINE)
        .append(BUILD_URL + COLON + SPACE + build.getUrl() + NEW_lINE)
        .append(BUILD_FULL_URL + COLON + SPACE + Jenkins.getInstance().getRootUrl() + build.getUrl() + NEW_lINE);
         
        return description.toString();
    }
	
	/** Prepare hosts details
	 * @param build
	 * @return
	 */
	public static Device prepareHost(final AbstractBuild<?, ?> build) {
        String nodeName = build.getBuiltOn().getNodeName();
        if (!build.getProject().getName().equals(build.getRootBuild().getProject().getName())) {
            nodeName = build.getProject().getName();  //label
        }
        if(nodeName == null || nodeName.equals(EMPTY_STR)){
            nodeName = DEFAULT_NODE;
        }
        
        Device vistaraDevice = new Device();
    	vistaraDevice.setHostName(nodeName);
        
        return vistaraDevice;
	}
	
	/** Prepare Vistara alert state based on build status
	 * @param state
	 * @return
	 */
	public static String prepareAlertState(final String state) {
		if(state == null || state.equals(EMPTY_STR)) {
			return APIConstants.INFO;
		}
		
		if(state.equals(STARTED)) {
			return APIConstants.WARNING;
		} else if(state.equals(FAILED)) {
			return APIConstants.CRITICAL;
		} else if(state.equals(SUCCESS)) {
			return APIConstants.OK;
		} else {
			return APIConstants.INFO;
		}
	}
	
	/** Prepare Vistara alert JSON body
     * @param build
     * @param state
     * @return
     */
    @SuppressWarnings("unused")
	private String prepareVistaraAlertJson(final AbstractBuild<?, ?> build, final String state) {
    	final AbstractBuild<?, ?> rootBuild = build.getRootBuild();
        JSONObject json = new JSONObject();
        
        json.put(BUILD_NAME, build.getFullDisplayName() + " - " + build.getDisplayName());
        json.put(BUILD_NUMBER, build.getNumber());
        json.put(BUILD_URL, build.getUrl());
        json.put(BUILD_FULL_URL, Jenkins.getInstance().getRootUrl() + build.getUrl());
        json.put(BUILD_STATE, state);
        json.put(BUILD_SERVICE_NAME, JENKINS);
        
        //Build triggered by
        List<Cause> causes = build.getCauses();
        if (causes.size() > 0 && (causes.get(0) instanceof Cause.UserIdCause)){
            json.put(BUILD_USER, ((UserIdCause)causes.get(0)).getUserName());
        } else {
            json.put(BUILD_USER, ANONYMOUS_USER);
        }
        
        //Change log details
        StringBuffer changeLog = new StringBuffer(EMPTY_STR);
        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = rootBuild.getChangeSet();
        List<ChangeLogSet.Entry> entries = new LinkedList<ChangeLogSet.Entry>();
        for(Object o : changeSet.getItems()) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
            entries.add(entry);
        }
        
        if(entries.isEmpty()) {
            if(build.getDescription() != null && !build.getDescription().equals(EMPTY_STR)) {
            	changeLog.append(build.getDescription());
            } else { 
            	changeLog.append(causes.get(0).getShortDescription());
            }
        } else {
            String authors = EMPTY_STR;
            String commitAndMessages = EMPTY_STR;
            for(ChangeLogSet.Entry entry : entries) {
            	authors += entry.getAuthor().getDisplayName() + COMMA;
                commitAndMessages += entry.getCommitId() + COLON + entry.getMsg() + COMMA;
            }
            changeLog.append(BUILD_AUTHORS + COLON + SPACE + authors);
            changeLog.append(BUILD_COMMIT_DETAILS + COLON + SPACE + commitAndMessages);
        }
        
        //Subject
        String subject = build.getDisplayName() + SPACE;
        if(state.equalsIgnoreCase(FAILED)) {
        	subject += build.getResult();
        } else {
        	subject += state;
        }
        json.put(SUBJECT, subject);
        
        //Description
        StringBuffer description = new StringBuffer(subject + NEW_lINE);
        description.append("Build triggered by: " + json.get("triggeredBy") + NEW_lINE);
        description.append("Change log: " + changeLog.toString() + NEW_lINE);
        description.append("Build project name: " + build.getProject().getName() + NEW_lINE);
        description.append("Root build project name: " + rootBuild.getProject().getName() + NEW_lINE);
        description.append("Build project full display name: " + build.getProject().getFullDisplayName() + NEW_lINE);
        description.append("Build project full name: " + build.getProject().getFullName() + NEW_lINE);
        description.append("Build project URL: " + build.getProject().getUrl() + NEW_lINE);
        json.put(DESCRIPTION, description.toString());
        
        //devices
        JSONArray hosts = new JSONArray();
        String nodeName = build.getBuiltOn().getNodeName();
        if (!build.getProject().getName().equals(rootBuild.getProject().getName())) {
            nodeName = build.getProject().getName();  //label
        }
        if(nodeName == null || nodeName.equals(EMPTY_STR)){
            nodeName = DEFAULT_NODE;
        }
        hosts.add(nodeName);
        json.put(HOSTS, hosts);
        
        return json.toString();
    }
}