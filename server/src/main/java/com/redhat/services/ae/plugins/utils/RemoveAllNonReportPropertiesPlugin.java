package com.redhat.services.ae.plugins.utils;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.redhat.services.ae.plugins.Plugin;

public class RemoveAllNonReportPropertiesPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(RemoveAllNonReportPropertiesPlugin.class);

	private static final Predicate<String> allNonReportFields=new Predicate<String>(){public boolean apply(@Nullable String str){
		return !"_sectionScore".equals(str) && !"_report".equals(str);
	}};


	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		log.debug("removing the following answer properties before persisting:");
		
		for(String key:Lists.newArrayList(Iterables.filter(surveyResults.keySet(), allNonReportFields).iterator())){
			log.debug("   - "+key);
			surveyResults.remove(key);
		}
		
		return surveyResults;
	}


	@Override
	public Plugin setConfig(Map<String, Object> config){
		return null;
	}

}
