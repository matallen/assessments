package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.redhat.services.ae.utils.Json;

// EmbeddedScoreTotalPlugin
public class SectionScorePlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(SectionScorePlugin.class);
	private String sectionScoreName="_sectionScore";
	private String arithmaticMethod="average";
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		sectionScoreName=getConfigValueAsString(config, "sectionScoreName", "_sectionScore");
		arithmaticMethod=getConfigValueAsString(config, "arithmaticMethod", "average");
		
		if (!Lists.newArrayList("average","sum").contains(arithmaticMethod)){
			throw new RuntimeException("property 'arithmaticMethod' must contain either 'average' or sum'");
		}
		
		return this;
	}

	private Map<String,Integer> sectionScores=new HashMap<String, Integer>();
	private Map<String,Integer> sectionTotals=new HashMap<String, Integer>();
	private Map<String,Integer> sectionCounts=new HashMap<String, Integer>();
	
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			
			Map<String,Object> value=(Map<String,Object>)e.getValue();
			
			
			if (value.containsKey("score")){
				if (value.containsKey("navigationTitle") || value.containsKey("pageId")){
					String sectionName=(String)(value.containsKey("navigationTitle")?value.get("navigationTitle"):value.get("pageId"));
					int score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
					
					log.debug(questionId+"::Adding score "+score+" to section ["+sectionName+"]");
					
					sectionTotals.put(sectionName, sectionTotals.containsKey(sectionName)?sectionTotals.get(sectionName)+score:score);
					sectionCounts.put(sectionName, sectionCounts.containsKey(sectionName)?sectionCounts.get(sectionName)+1:1);
					sectionScores.put(sectionName, sectionTotals.get(sectionName)/sectionCounts.get(sectionName));
					
				}else{
					log.error("This question ("+questionId+") has a score ("+value.get("score")+"), so it should have a navigationTitle too to know which section to add it to ("+value.containsKey("navigationTitle")+"). It's score is being omitted");
				}
			}
			
		}
		
		if ("sum".equalsIgnoreCase(arithmaticMethod)){
			surveyResults.put(sectionScoreName, sectionTotals);
		}else{
			surveyResults.put(sectionScoreName, sectionScores);
		}
		
		if (sectionScores.size()<=0){
			log.error(this.getClass().getSimpleName()+":: Likely error -> no scores detected therefore no section average scores!");
			log.debug("surveyResults as of ERROR (it's looking for 'score' property of each question, then sumby 'navigationTitle'):/n"+Json.toJson(surveyResults));
		}
		
		log.debug("SectionScores = "+Json.toJson(sectionScores));
		return surveyResults;
	}

}
