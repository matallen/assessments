package com.redhat.services.ae.plugins;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.redhat.services.ae.Predicates;

public class RemoveReportPropertiesPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(RemoveReportPropertiesPlugin.class);
	private String fieldsToRemoveRegEx;
	private String fieldsToObfuscateRegEx;
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		fieldsToRemoveRegEx=(String)config.get("fieldsToRemoveRegEx");
		fieldsToObfuscateRegEx=(String)config.get("fieldsToObfuscateRegEx");
		return this;
	}
	

	public static boolean looksLikeAnEmail(String value){
		return value.matches(".+@.+\\..+");
	}
	private String chr(String c, int len){
		return String.format("%-"+len+"s", "").replaceAll(" ", c);
	}
	protected String obfuscate(String value){
		if (null==value) return value;
		if (value.contains("@")){ // obfuscate everything before the @ symbol
			return chr("*",value.indexOf("@"))+value.substring(value.indexOf("@"));// String.format("%-"+value.indexOf("@")+"s", "").replaceAll(" ", "*")+value.substring(value.indexOf("@"));
		}else{ // * the first and last chars
			if (value.length()>2){
				int starCharsEachEnd=(int)Math.min(Math.ceil(value.length()/3), 3);
				return chr("*",starCharsEachEnd) + value.substring(starCharsEachEnd,value.length()-starCharsEachEnd) + chr("*",starCharsEachEnd);
			}else{
				return chr("*",value.length());
			}
		}
	}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		log.debug("obfuscating answers:");
		if (!StringUtils.isAllBlank(fieldsToObfuscateRegEx)){
			
			for(Entry<String, Object> e:surveyResults.entrySet()){
				if (e.getKey().matches(fieldsToObfuscateRegEx) && !"_sectionScore".equals(e.getKey()) && !e.getKey().startsWith("_report")){
					String obfuscated=obfuscate((String)e.getValue());
//					log.debug("   - "+e.getKey()+" ("+e.getValue()+"->"+obfuscated+")"); // this debug line shows the conversion which is useful for dev but violate PII for a live system
					log.debug("   - "+e.getKey());
					surveyResults.put(e.getKey(), obfuscated);
				}
			}
		}
		
		log.debug("removing answers:");
		Predicate<String> removeRegExPredicate=new Predicate<String>(){public boolean apply(@Nullable String str){
			return !"_sectionScore".equals(str) && !str.startsWith("_report") && str.matches(fieldsToRemoveRegEx);
		}};
		for(String key:Lists.newArrayList(Iterables.filter(surveyResults.keySet(), null==fieldsToRemoveRegEx?Predicates.allNonReportFields:removeRegExPredicate).iterator())){
			log.debug("   - "+key);
			surveyResults.remove(key);
		}
		
		return surveyResults;
	}

}
