package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.utils.Json;

public class EmbeddedScoreTotalPlugin implements Plugin{
	public static final Logger log=LoggerFactory.getLogger(EmbeddedScoreTotalPlugin.class);

	@Override
	public void setConfig(Map<String, Object> config){
		
	}

//	private int totalScore=0;
//	private int scoreCount=0;
	private Map<String,Integer> sectionScores=new HashMap<String, Integer>();
	private Map<String,Integer> sectionTotals=new HashMap<String, Integer>();
	private Map<String,Integer> sectionCounts=new HashMap<String, Integer>();
	
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			Map<String,Object> value=(Map<String,Object>)e.getValue();
			
//			if (value.containsKey("score")){ // ie, if the question has a score set
//				scoreCount+=1;
//				int score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
//				log.debug("EmbeddedScoreTotalPlugin:: question: "+questionId+" has score of: "+score);
//				totalScore+=score;
//			}
			
			if (value.containsKey("score") && value.containsKey("navigationTitle")){ // ie, if the question has a score set & belongs to a section/page
				String navTitle=(String)value.get("navigationTitle");
				int score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
//				Integer questionScore=(Integer)value.get("score");
				
				log.debug(EmbeddedScoreTotalPlugin.class.getSimpleName()+"::"+questionId+"::Adding score "+score+" to section ["+navTitle+"]");
				
//				scoreCount+=1;
//				totalScore+=score;
				sectionTotals.put(navTitle, sectionTotals.containsKey(navTitle)?sectionTotals.get(navTitle)+score:score);
				sectionCounts.put(navTitle, sectionScores.containsKey(navTitle)?sectionScores.get(navTitle)+1:1);
				sectionScores.put(navTitle, sectionTotals.get(navTitle)/sectionCounts.get(navTitle));
			}
			
			
		}
		
//		double averageScore=totalScore / scoreCount;
//		long lAverageScore=Math.round(averageScore);
//		int iAverageScore=(int)lAverageScore;
		
//		surveyResults.put("totalScore", totalScore);
//		surveyResults.put("_averageScore", iAverageScore);
		surveyResults.put("_sectionScore", sectionScores);
		
		log.debug(EmbeddedScoreTotalPlugin.class.getSimpleName()+":: SectionScores = "+Json.toJson(sectionScores));
		return surveyResults;
	}

}
