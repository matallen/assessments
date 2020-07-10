package com.redhat.services.ae.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.AnswerProcessor;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.CacheHelper;
import com.redhat.services.ae.utils.Json;

public class RTIResultsPlugin implements Plugin{
	public static final Logger log=LoggerFactory.getLogger(RTIResultsPlugin.class);
	
	@Override
	public void setConfig(Map<String, Object> config){
		
	}
	
	
	private Map<String, Map<String,String>> flattenAndEnrichResults(String surveyId, Map<String,Object> surveyResults) throws FileNotFoundException, IOException{
		Map<String, Map<String,String>> result=new HashMap<String, Map<String,String>>();
		
		Map<String, String> questionsToTitleMapping=new HashMap<>();
		
		Survey s=Survey.findById(surveyId);
		List<mjson.Json> pages=mjson.Json.read(s.getQuestions()).at("pages").asJsonList();
		for(mjson.Json page:pages){
			for(mjson.Json question:page.at("elements").asJsonList()){
				String title=question.at("name").asString();
				if (question.has("title"))
					title=question.at("title").isString()?question.at("title").asString():question.at("title").at("default").asString();
				questionsToTitleMapping.put(question.at("name").asString(), title);
				
			}
		}
		
		new AnswerProcessor(){
			@Override public void onStringAnswer(String questionId, String answerId, Integer score){
				String title=questionsToTitleMapping.get(questionId);
				result.put(questionId, new MapBuilder<String,String>().put("title",title).put("answer",answerId).put("score", String.valueOf(score)).build());
			}
			@Override public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){
//				result.add(averageScore);
			}
			@Override public void onMapAnswer(String question, Answer answer){
//				log.warn("Due to not expecting mapped answers, not logging score for: "+question);
//				result.add(0);
			}
		}.process(surveyResults);
		
		return result;
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
					log.debug("Incrementing score by "+score+" for "+questionId);
					scores.add(score);
				}
				@Override public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){
					log.debug("Incrementing score by the average "+averageScore+" for "+questionId);
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
