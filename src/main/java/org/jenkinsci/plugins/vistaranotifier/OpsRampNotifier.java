/*
 * The MIT License
 *
 * Copyright  2018 OpsRamp, Inc. All Rights Reserved.
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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

/**A class to notify OpsRamp about build status
 * @author Srini T
 *
 */
public class OpsRampNotifier extends Notifier implements OpsRampNotifierConstants {
	
    /**
     * Constructor
     */
    @DataBoundConstructor
    public OpsRampNotifier() {
    }

    /* (non-Javadoc)
     * @see hudson.tasks.BuildStep#getRequiredMonitorService()
     */
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    /* (non-Javadoc)
     * @see hudson.tasks.BuildStepCompatibilityLayer#prebuild(hudson.model.AbstractBuild, hudson.model.BuildListener)
     */
    @Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		return processEvent(build, listener, STARTED);
	}

    /* (non-Javadoc)
     * @see hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    	String state = null;
        if((build.getResult() == null) || (!build.getResult().equals(Result.SUCCESS))) {
        	state = FAILED;
 		} else {
 			state = SUCCESS;
 		}
        return processEvent(build, listener, state);
    }

    /** Process build event
     * @param build
     * @param listener
     * @param state
     * @return
     */
    private boolean processEvent(final AbstractBuild<?, ?> build, final BuildListener listener, final String state) {
	   PrintStream logger = listener.getLogger();
       
	   OpsRampDescriptor descriptor = getDescriptor();
       if (!descriptor.isEnabled()){
           logger.println("WARNING: OpsRamp Notification Disabled - Not configured.");
           return true; //NOTE: Don't fail build because of our notification...
       }
       
       try {
    	   OpsRampNotifierClient apiClient = new OpsRampNotifierClient(descriptor.getOpsRampBaseURI(),
    			   descriptor.getOpsRampClientId(), descriptor.getOpsRampApiKey(), descriptor.getOpsRampApiSecret());
    	   
    	   String alertPayload = OpsRampNotifierUtils.prepareOpsRampAlert(build, state);
   		   String response = apiClient.createAlert(alertPayload);
   		   logger.println("OpsRamp notifier response: " + response);
       } catch (Exception e){
           logger.println("Failed to send notification to OpsRamp, Reason: " + e.toString());
           //return false;
       }
       return true;
    }
    
    /* (non-Javadoc)
     * @see hudson.tasks.Notifier#getDescriptor()
     */
    @Override
    public OpsRampDescriptor getDescriptor() {
        return (OpsRampDescriptor)super.getDescriptor();
    }

    /**
     * @author Srini T
     *
     */
    @Extension
    public static final class OpsRampDescriptor extends BuildStepDescriptor<Publisher> {
    	private String opsRampClientId;
		private String opsRampApiKey;
		private String opsRampApiSecret;
		private String opsRampBaseURI;
		private final String opsRampDefaultBaseURI = "https://api.opsramp.com";
		
		 public OpsRampDescriptor() {
	            load();
	        }
		/**
		 * @return
		 */
		public boolean isEnabled() {
			return getOpsRampApiKey() != null && getOpsRampApiSecret() != null && 
					getOpsRampClientId() != null && getOpsRampBaseURI() != null;
		}
		
		/**
		 * @return
		 */
		public String getOpsRampDefaultBaseURI() {
		    return opsRampDefaultBaseURI;
		}
		 
		/**
		 * @return
		 */
		public String getOpsRampClientId() {
			if(opsRampClientId == null || opsRampClientId.equals(EMPTY_STR)) {
				return null;
			}
			return opsRampClientId;
		}
		
		/**
		 * @return
		 */
		public String getOpsRampApiKey() {
			if(opsRampApiKey == null || opsRampApiKey.equals(EMPTY_STR)) {
				return null;
			}
			return opsRampApiKey;
		}
		
		/**
		 * @return
		 */
		public String getOpsRampApiSecret() {
			if(opsRampApiSecret == null || opsRampApiSecret.equals(EMPTY_STR)) {
				return null;
			}
			return opsRampApiSecret;
		}
		
		/**
		 * @return
		 */
		public String getOpsRampBaseURI() {
		    if (opsRampBaseURI == null){
		        return opsRampBaseURI;
		    } else if(opsRampBaseURI.equals(EMPTY_STR)) {
		        return opsRampDefaultBaseURI;
		    } 
		    return opsRampBaseURI;
		}
		
		/* (non-Javadoc)
		 * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
		 */
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
		    return true;
		}
		
		/* (non-Javadoc)
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		public String getDisplayName() {
		    return VISTARA_DISPLAY_NAME;
		}
		
		/** Validate OpsRamp API key
		 * @param value
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public FormValidation doCheckOpsRampApiKey(@QueryParameter("opsRampApiKey") String value)
		    throws IOException, ServletException {
			String apiKey = value;
			if((apiKey != null) && (!apiKey.trim().equals(EMPTY_STR))) {
				apiKey = apiKey.trim();
			} else {
				apiKey = opsRampApiKey != null ? opsRampApiKey.trim() : null;
			}
		
			if((apiKey == null) || apiKey.equals(EMPTY_STR)) {
				return FormValidation.error(API_KEY_ERR_MSG);
		    } else {
		        return FormValidation.ok();
		    }
		}
		
		/** Validate OpsRamp API secret
		 * @param value
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public FormValidation doCheckOpsRampApiSecret(@QueryParameter("opsRampApiSecret") String value)
		    throws IOException, ServletException {
			String apiSecret = value;
			if((apiSecret != null) && (!apiSecret.trim().equals(EMPTY_STR))) {
				apiSecret = apiSecret.trim();
			} else {
				apiSecret = opsRampApiSecret != null ? opsRampApiSecret.trim() : null;
			}
		
			if((apiSecret == null) || apiSecret.equals(EMPTY_STR)) {
				return FormValidation.error(API_SECRET_ERR_MSG);
		    } else {
		        return FormValidation.ok();
		    }
		}
		
		/** Validate OpsRamp client ID
		 * @param value
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public FormValidation doCheckOpsRampClientId(@QueryParameter("opsRampClientId") String value)
		    throws IOException, ServletException {
			String clientId = value;
			if((clientId != null) && (!clientId.trim().equals(EMPTY_STR))) {
				clientId = clientId.trim();
			} else {
				clientId = opsRampClientId != null ? opsRampClientId.trim() : null;
			}
		
			if((clientId == null) || clientId.equals("")) {
				return FormValidation.error(CLIENT_ID_ERR_MSG);
		    } else {
		        return FormValidation.ok();
		    }
		}
		
		/** Validate OpsRamp base URI
		 * @param value
		 * @return
		 * @throws IOException
		 * @throws ServletException
		 */
		public FormValidation doCheckOpsRampBaseURI(@QueryParameter("opsRampBaseURI") String value)
		    throws IOException, ServletException {
			String url = value;
			if((url != null) && (!url.trim().equals(EMPTY_STR))) {
			    url = url.trim();
			} else {
			    url = opsRampBaseURI != null ? opsRampBaseURI.trim() : null;
			}
			
			try {
			    new URL(url);
			    return FormValidation.ok();
			} catch (Exception e) {
			    return FormValidation.error(BASE_URI_ERR_MSG);
			}
		}
		
		/* (non-Javadoc)
		 * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
		 */
		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
		    opsRampClientId = formData.getString(CLIENT_ID_PARAM);
			opsRampApiKey = formData.getString(API_KEY_PARAM);
			opsRampApiSecret = formData.getString(API_SECRET_PARAM);
			opsRampBaseURI = formData.getString(BASE_URI_PARAM);
		    save();
		    return super.configure(req, formData);
		}
    }
}