package com.redhat.services.ae.plugins;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class RevertAnswersPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(RevertAnswersPlugin.class);
	private String fieldsToRetainRegEx;
	private List<String> additionalFieldsToRetain=Lists.newArrayList("_reportId", "_timestamp");
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		fieldsToRetainRegEx=(String)config.get("fieldsToRetainRegEx");
		
		if (null==fieldsToRetainRegEx) throw new RuntimeException("RevertAnswersPlugin not configured. Missing 'fieldsToRetainRegEx'.");
		
		return this;
	}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		log.debug("reverting answers and merging new fields:");
		
		// Revert answers
		Map<String, Object> newSurveyResults=new LinkedHashMap<String, Object>(super.originalSurveyResults);
		
		// Merge new fields back
		for(Entry<String, Object> e:surveyResults.entrySet()){
			if (e.getKey().matches(fieldsToRetainRegEx) || additionalFieldsToRetain.contains(e.getKey())){
				log.debug("Retainng Field: "+e.getKey());
				newSurveyResults.put(e.getKey(), e.getValue());
			}
		}
		
		return newSurveyResults;
	}

}
