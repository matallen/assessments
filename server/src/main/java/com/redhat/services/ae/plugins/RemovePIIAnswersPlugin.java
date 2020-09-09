package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RemovePIIAnswersPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(RemovePIIAnswersPlugin.class);

	private static final Predicate<String> personalIdentifiableInfoFields=new Predicate<String>(){public boolean apply(@Nullable String str){
			return (str.startsWith("C_") || str.startsWith("_")) && !("_sectionScore".equals(str) || "_report".equals(str));
	}};
	

	@Override
	public void setConfig(Map<String, Object> config){}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		log.debug("removing the following PII keys before persisting:");
		
		for(String key:Lists.newArrayList(Iterables.filter(surveyResults.keySet(), personalIdentifiableInfoFields).iterator())){
			log.debug("   - "+key);
			surveyResults.remove(key);
		}
		
		return surveyResults;
	}

}
