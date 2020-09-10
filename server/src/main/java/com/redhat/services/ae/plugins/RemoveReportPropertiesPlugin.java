package com.redhat.services.ae.plugins;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RemoveReportPropertiesPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(RemoveReportPropertiesPlugin.class);
	private String fieldsToRemoveRegEx;
	
	@Override
	public void setConfig(Map<String, Object> config){
		fieldsToRemoveRegEx=(String)config.get("fieldsToRemoveRegEx");
	}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		log.debug("removing the following answer properties before persisting:");
		
		Predicate<String> regExPredicate=new Predicate<String>(){public boolean apply(@Nullable String str){
			return !"_sectionScore".equals(str) && !str.startsWith("_report") && str.matches(fieldsToRemoveRegEx);
		}};
		
		for(String key:Lists.newArrayList(Iterables.filter(surveyResults.keySet(), null==fieldsToRemoveRegEx?Predicates.allNonReportFields:regExPredicate).iterator())){
			log.debug("   - "+key);
			surveyResults.remove(key);
		}
		
		return surveyResults;
	}

}
