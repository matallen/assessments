package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

public abstract class Plugin{
	protected Map<String, Object> originalSurveyResults;
	Map<String,String> cfg;
	
	public abstract Plugin setConfig(Map<String, Object> config);
	protected Object getConfigValue(Map<String, Object> config, String propertyName, String propertyDefault){
		if (config!=null) return config.containsKey(propertyName)?config.get(propertyName):propertyDefault;
		return propertyDefault;
	}
	protected String getConfigValueAsString(Map<String, Object> config, String propertyName, String propertyDefault){
		if (config!=null) return config.containsKey(propertyName)?(String)config.get(propertyName):propertyDefault;
		return propertyDefault;
	}
	
	public abstract Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception;
	
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
	
}
