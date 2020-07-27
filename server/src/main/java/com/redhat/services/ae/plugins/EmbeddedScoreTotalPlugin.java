package com.redhat.services.ae.plugins;

import java.util.Map;
import java.util.Map.Entry;

public class EmbeddedScoreTotalPlugin implements Plugin{

	@Override
	public void setConfig(Map<String, Object> config){
		
	}

	private int totalScore=0;
	private int scoreCount=0;
	
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			Map<String,Object> value=(Map<String,Object>)e.getValue();
			
			if (value.containsKey("score")){ // ie, if the question has a score set
				scoreCount+=1;
				int score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
				System.out.println("EmbeddedScoreTotalPlugin:: score "+score+" detected for question: "+questionId);
				totalScore+=score;
			}
			
		}
		
		double averageScore=totalScore / scoreCount;
		long lAverageScore=Math.round(averageScore);
		int iAverageScore=(int)lAverageScore;
		
		surveyResults.put("totalScore", totalScore);
		surveyResults.put("averageScore", iAverageScore);
		return surveyResults;
	}

}
