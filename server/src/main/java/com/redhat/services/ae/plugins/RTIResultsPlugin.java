package com.redhat.services.ae.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.services.ae.controllers.AnswerProcessor;
import com.redhat.services.ae.utils.CacheHelper;
import com.redhat.services.ae.utils.Json;

public class RTIResultsPlugin implements Plugin{
	public static final Logger log=LoggerFactory.getLogger(RTIResultsPlugin.class);
	
	@Override
	public void setConfig(Map<String, Object> config){
		
	}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		try{
			log.debug("RTIResultsPlugin:: execute. Should generate and cache the results");
			// Do any parsing and formatting of the result data for the result page to display
			
			
			// calculate the score and add to the data structure
			List<Integer> scores=new LinkedList<>();
			new AnswerProcessor(){
				@Override public void onStringAnswer(String questionId, String answerId, Integer score){
					scores.add(score);
				}
				@Override public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){
					scores.add(averageScore);
				}
				@Override public void onMapAnswer(String question, Answer answer){
					log.warn("Due to not expecting mapped answers, not logging score for: "+question);
					scores.add(0);
				}
			}.process(surveyResults);
			
			Integer totalScore=0;
			for(Integer s:scores){
				totalScore+=s;
			}
			
			surveyResults.put("overallScore", totalScore);
			
			
			// Store results temporarily to generate the report content (TODO: this could cause a race condition, because if this doesnt get the data in the cache quick enough before the results page tries to load we have an issue)
			log.debug("RTIResultsPlugin:: putting answers into cache, ready for the results page to render it");
			CacheHelper.cache.put(surveyId+"_"+visitorId, Json.toJson(surveyResults));
			
			return surveyResults;
		}catch(JsonProcessingException e){
			e.printStackTrace();
			throw e;
		}
	}

}
