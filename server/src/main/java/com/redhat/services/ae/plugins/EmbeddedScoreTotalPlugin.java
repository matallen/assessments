package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.utils.Json;

public class EmbeddedScoreTotalPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(EmbeddedScoreTotalPlugin.class);

	@Override
	public void setConfig(Map<String, Object> config){}

	private Map<String,Integer> sectionScores=new HashMap<String, Integer>();
	private Map<String,Integer> sectionTotals=new HashMap<String, Integer>();
	private Map<String,Integer> sectionCounts=new HashMap<String, Integer>();
	
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			
			Map<String,Object> value=(Map<String,Object>)e.getValue();
			
//			System.out.println("XXX: value.contains('score')=="+value.containsKey("score") +"  AND value.contains('navigationTitle')=="+value.containsKey("navigationTitle"));
			
			if (value.containsKey("score")){
				if (value.containsKey("navigationTitle")){
					String navTitle=(String)value.get("navigationTitle");
					int score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
					
					log.debug(questionId+"::Adding score "+score+" to section ["+navTitle+"]");
					
					sectionTotals.put(navTitle, sectionTotals.containsKey(navTitle)?sectionTotals.get(navTitle)+score:score);
					sectionCounts.put(navTitle, sectionCounts.containsKey(navTitle)?sectionCounts.get(navTitle)+1:1);
					sectionScores.put(navTitle, sectionTotals.get(navTitle)/sectionCounts.get(navTitle));
					
				}else{
					log.error("This question ("+questionId+") has a score ("+value.get("score")+"), so it should have a navigationTitle too ("+value.containsKey("navigationTitle")+")");
				}
			}
			
		}
		
		surveyResults.put("_sectionScore", sectionScores);
		
		if (sectionScores.size()<=0) log.error(this.getClass().getSimpleName()+":: Likely error -> no scores detected therefore no section average scores!");
		
		log.debug("SectionScores = "+Json.toJson(sectionScores));
		return surveyResults;
	}

}
