package org.jenkinsci.plugins.vistaranotifier;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vistara.sdk.dto.alert.Alert;
import com.vistara.sdk.dto.device.Device;
import com.vistara.sdk.utils.APIConstants;
import com.vistara.sdk.utils.TimeUtils;

import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Cause.UserIdCause;
import hudson.model.Cause.UpstreamCause;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**A Util class to prepare required objects 
 * @author Srini T
 *
 */
public class VistaraNotifierUtils implements VistaraNotifierConstants {
	
	/** Method to prepare Vistara alert object 
	 * @param build
	 * @param state
	 * @return
	 */
	public static Alert prepareVistaraAlert(AbstractBuild<?, ?> build, String state) {
    	Alert vistaraAlert = new Alert();
    	
    	vistaraAlert.setSubject(prepareAlertSubject(build, state));
    	vistaraAlert.setCurrentState(prepareAlertState(state));
    	vistaraAlert.setDescription(prepareAlertDescription(build, state, vistaraAlert.getSubject()));
    	vistaraAlert.setDevice(prepareHost(build));
    	vistaraAlert.setAlertTime(TimeUtils.getDateTime(TimeUtils.DB_DATE_FORMAT, new Date()));
    	vistaraAlert.setServiceName(DEFAULT_METRIC);
    	vistaraAlert.setApp(JENKINS);
    	vistaraAlert.setExtAlertId(String.valueOf(build.getNumber()));
    	vistaraAlert.setUniqueId(build.getProject().getName());
    	
    	return vistaraAlert;
    }
	
	/** Method to prepare Vistara alert subject
	 * @param build
	 * @param state
	 * @return
	 */
	public static String prepareAlertSubject(AbstractBuild<?, ?> build, String state) {
    	String subject = build.getFullDisplayName() + SPACE;
        if(state.equalsIgnoreCase(FAILED)) {
        	subject += build.getResult();
        } else {
        	subject += state;
        }
        
        return subject;
    }
	
	/** Method to prepare Vistara alert description
	 * 
	 * STARTED - send all change log, name, number, URL details as alert description along with build summary
	 * FAILED  - send last 200 lines console output as description along with build summary
	 * SUCCESS - send build summary as description
	 * 
	 * @param build
	 * @param subject
	 * @return
	 */
	public static String prepareAlertDescription(AbstractBuild<?, ?> build, String state, String subject) {
    	StringBuffer description = new StringBuffer(subject).append(NEW_LINE);
    	if(state.equals(STARTED)) {
	    	//Build triggered by
	        String buildUser = prepareBuildUser(build.getCauses());
	        
	        //Change log details
	        String changeLog = prepareChanelogDetails(build, build.getCauses());
	        
	        description.append(BUILD_NAME).append(COLON).append(SPACE).append(build.getProject().getName()).append(NEW_LINE)
	        .append(BUILD_NUMBER).append(COLON).append(SPACE + build.getNumber()).append(NEW_LINE)
	        .append(BUILD_URL).append(COLON).append(SPACE).append(build.getUrl()).append(NEW_LINE)
	        .append(BUILD_FULL_URL).append(COLON).append(SPACE).append(Jenkins.getInstance().getRootUrl()).append(build.getUrl()).append(NEW_LINE)
	        .append(DESC_BUILD_USER + COLON + SPACE + buildUser + NEW_LINE)
	        .append(DESC_CHANGE_LOG).append(COLON).append(NEW_LINE).append(changeLog.toString());
    	} else if(state.equals(FAILED)) {
    		String consoleLog = prepareConsoleLog(build);
    		if(!consoleLog.equals(EMPTY_STR)) {
    			description.append(consoleLog);
    		}
	    } 
    	
        return description.toString();
    }
	
	/** Method to prepare build started user
	 * @param causes
	 * @return
	 */
	private static String prepareBuildUser(List<Cause> causes) {
		String buildUser = ANONYMOUS_USER;
        if(causes != null && causes.size() > 0) {
	        if(causes.get(0) instanceof UserIdCause){
	        	buildUser =  ((UserIdCause)causes.get(0)).getUserName();
	        } else if(causes.get(0) instanceof UpstreamCause) {
	        	List<Cause> upstreamCauses = ((UpstreamCause)causes.get(0)).getUpstreamCauses();
	        	prepareBuildUser(upstreamCauses);
	        } 
        }
        return buildUser;
	}
	
	/** Method to prepare build change log details
	 * @param build
	 * @param causes
	 * @return
	 */
	private static String prepareChanelogDetails(AbstractBuild<?, ?> build, List<Cause> causes) {
		StringBuilder changeLog = new StringBuilder(EMPTY_STR);
        StringBuilder summary = new StringBuilder(EMPTY_STR);
        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = build.getRootBuild().getChangeSet();
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
            for(ChangeLogSet.Entry entry : entries) {
            	summary.append(entry.getCommitId() + COLON + entry.getMsg() + NEW_LINE);
                changeLog.append(REV_STR).append(SPACE).append(BY_STR).append(SPACE)
                .append(entry.getAuthor().getDisplayName()).append(COLON).append(NEW_LINE)
                .append(entry.getCommitId()).append(COLON).append(entry.getMsg()).append(NEW_LINE);
                
                if(entry.getAffectedFiles() != null && entry.getAffectedFiles().size() > 0) {
	                Iterator<?extends AffectedFile> files = entry.getAffectedFiles().iterator();
	                changeLog.append(FILE_PATH).append(COLON).append(SPACE);
	                while(files.hasNext()) {
	                	AffectedFile file = files.next();
	                	changeLog.append(file.getPath()).append(NEW_LINE);
	                }
                }
                changeLog.append(NEW_LINE);
            }
        }
        
        if(!summary.toString().equals(EMPTY_STR)) {
        	summary.insert(0, DESC_SUMMARY + COLON + NEW_LINE);
        	changeLog.insert(0, summary.toString() + NEW_LINE);
        }
        return changeLog.toString();
	}
	
	/** Method to prepare console output
	 * @param build
	 * @return
	 */
	private static String prepareConsoleLog(AbstractBuild<?, ?> build) {
		StringBuilder consoleLog = new StringBuilder(EMPTY_STR);
		try {
			int maxLogLines = MAX_LINES;
			if(build.getResult() != Result.FAILURE) { //Don't get max lines if it is other than failure
				maxLogLines = MIN_LINES;
			}
    		List<String> logLines = build.getLog(maxLogLines);
    		if(logLines != null && !logLines.isEmpty()) {
    			consoleLog.append(CONSOLE_LOG_MSG1).append(SPACE).append(maxLogLines).append(SPACE)
    			.append(CONSOLE_LOG_MSG2).append(COLON).append(NEW_LINE);
    			for(String log : logLines) {
    				consoleLog.append(log).append(NEW_LINE);
    			}
    		}
		} catch(Exception e) {
			//Ignore
		}
		
		return consoleLog.toString();
	}
	
	/** Method to prepare hosts details
	 * @param build
	 * @return
	 */
	public static Device prepareHost(AbstractBuild<?, ?> build) {
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
	
	/** Method to prepare Vistara alert state based on build status
	 * @param state
	 * @return
	 */
	public static String prepareAlertState(String state) {
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
	
	/** Method to prepare build status details as JSON body
     * @param build
     * @param state
     * @return
     */
    @SuppressWarnings("unused")
    @Deprecated
	private String prepareVistaraAlertJson(AbstractBuild<?, ?> build, String state) {
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
        StringBuffer description = new StringBuffer(subject + NEW_LINE);
        description.append("Build triggered by: " + json.get("triggeredBy") + NEW_LINE);
        description.append("Change log: " + changeLog.toString() + NEW_LINE);
        description.append("Build project name: " + build.getProject().getName() + NEW_LINE);
        description.append("Root build project name: " + rootBuild.getProject().getName() + NEW_LINE);
        description.append("Build project full display name: " + build.getProject().getFullDisplayName() + NEW_LINE);
        description.append("Build project full name: " + build.getProject().getFullName() + NEW_LINE);
        description.append("Build project URL: " + build.getProject().getUrl() + NEW_LINE);
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