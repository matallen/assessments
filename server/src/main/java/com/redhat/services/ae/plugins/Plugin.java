package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

public abstract class Plugin{
	protected Map<String, Object> originalSurveyResults;
	public abstract void setConfig(Map<String, Object> config);

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
	
}
