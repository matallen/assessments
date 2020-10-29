package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class Plugin{
	public static final Logger log=LoggerFactory.getLogger(Plugin.class);
	protected Map<String, Object> originalSurveyResults;
//	private Map<String,Object> config;
	private boolean disabled;
	private String disabledIfExpression;
	
	
	public Plugin _setConfig(Map<String, Object> config){
//		this.config=config;
		disabled=getBooleanFromConfig(config, "disabled", false);
		disabledIfExpression=(String)config.get("disabledIf");
		return setConfig(config);
	}
	public abstract Plugin setConfig(Map<String, Object> config);
	protected Object getConfigValue(Map<String, Object> config, String propertyName, String propertyDefault){
		if (config!=null) return config.containsKey(propertyName)?config.get(propertyName):propertyDefault;
		return propertyDefault;
	}
	protected String getConfigValueAsString(Map<String, Object> config, String propertyName, String propertyDefault){
		if (config!=null) return config.containsKey(propertyName)?(String)config.get(propertyName):propertyDefault;
		return propertyDefault;
	}
	
//	public Map<String, Object> _execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
//		if (disabled) log.warn("Skipping "+this.getClass().getSimpleName()+" because disabled is True");
//		
//		boolean disabledIfResult=false;
//		if (null!=disabledIfExpression){
//			try{
//				Object eval=MVEL.eval(disabledIfExpression, surveyResults);
//				disabledIfResult=disabledIfResult || (eval instanceof Boolean && (boolean)eval);
//				log.debug("'disabledIfResult' is "+disabledIfResult);
//			}catch(Exception ex){
//				log.error("Not disabling "+this.getClass().getSimpleName()+", however an expression error occured: "+ex.getMessage());
//				ex.printStackTrace();
//			}
//		}else
//			log.warn("No 'disabledIfExpression' config found");
//		
//		if (disabledIfResult) log.warn("Skipping "+this.getClass().getSimpleName()+" because disabledIfExpression '"+disabledIfExpression+"' evaluated to true");
//		
//		if (!disabled && !disabledIfResult)
//			return execute(surveyId, visitorId, surveyResults);
//		
//		return surveyResults;
//	}
	public abstract Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception;
	
	
	public boolean isDisabled(Map<String, Object> surveyResults){
		if (disabled) log.warn("Skipping "+this.getClass().getSimpleName()+" because disabled is true");
		
		boolean disabledIfResult=false;
		if (null!=disabledIfExpression){
			try{
				Object eval=MVEL.eval(disabledIfExpression, surveyResults);
				disabledIfResult=disabledIfResult || (eval instanceof Boolean && (boolean)eval);
				log.debug(this.getClass().getSimpleName()+":: 'disabledIfResult' is "+disabledIfResult);
			}catch(Exception ex){
				log.error(this.getClass().getSimpleName()+":: Not disabling, however an expression error occured: "+ex.getMessage());
				ex.printStackTrace();
			}
		}else
			log.warn(this.getClass().getSimpleName()+":: No 'disabledIfExpression' config found");
		
		if (disabledIfResult) log.warn(this.getClass().getSimpleName()+":: Skipping because disabledIfExpression '"+disabledIfExpression+"' evaluated to true");
		
		return disabled || disabledIfResult;
	}
	
	public void setOriginalSurveyResults(Map<String, Object> originalSurveyResults){
		this.originalSurveyResults=originalSurveyResults;
	}
	
	public void onDestroy(String surveyId, String visitorId, Map<String, Object> surveyResults){}
	
	public void removeAnswerProperties(Map<String, Object> surveyResults, List<String> answerPropertiesToRemove){
		if (null!=surveyResults){
			for (Entry<String, Object> e:surveyResults.entrySet()){
				if (Map.class.isAssignableFrom(e.getValue().getClass())){ // to prevent things like _reportId causing an exception
					Map<String,String> answerData=(Map<String,String>)e.getValue();
					for(String answerKey:Lists.newArrayList(answerData.keySet())){
						if (answerPropertiesToRemove.contains(answerKey))
							answerData.remove(answerKey);
					}
				}
			}
		}
	}
	
	protected boolean hasExtraDebug(Map<String, Object> config, String extraDebugParam){
		boolean result=false;
		if (config.containsKey(extraDebugParam)){
			if (String.class.isAssignableFrom(config.get(extraDebugParam).getClass()))  result="true".equalsIgnoreCase((String)config.get(extraDebugParam));
			if (Boolean.class.isAssignableFrom(config.get(extraDebugParam).getClass())) result=(Boolean)config.get(extraDebugParam);
		}
		return result;
	}
	
	protected boolean getBooleanFromConfig(Map<String, Object> cfg, String paramName, boolean d3fault){
		boolean result=d3fault;
		if (cfg.containsKey(paramName)){
			if (String.class.isAssignableFrom(cfg.get(paramName).getClass()))  result="true".equalsIgnoreCase((String)cfg.get(paramName));
			if (Boolean.class.isAssignableFrom(cfg.get(paramName).getClass())) result=(Boolean)cfg.get(paramName);
		}
		return result;
	}
	
}
