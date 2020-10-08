package com.redhat.services.ae.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.AnswerProcessor;
import com.redhat.services.ae.controllers.AnswerProcessor.Answer;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.CacheHelper;
import com.redhat.services.ae.utils.Json;

public abstract class EnrichAnswersPluginBase extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(EnrichAnswersPluginBase.class);
	
	/**
	 * 
	 * this plugin needs to flatten the answers (ie, remove panels), merge with question title, and update the data structure to generate the results page
	 * The purpose of this plugin is to enrich the information and make it easier/more consistent to parse
	 * 
	 * for example we turn this:
	 * {
	 *   "q_num_employees" : "100+"	
	 *   "q_cloud_current_culture_process" : [ "Siloed process" ]
	 * }
	 * 
	 * into this:
	 * {
	 * 	"q_cloud_current_culture_process" : {
	 * 		"title" : "What best describes your current culture and process related to automating tasks?",
	 *    "answers": [
	 *    	{
	 *    		"answer" : "Siloed process"
	 *    	}
	 *    ]
	 * 	},
	 *  "q_num_employees" : {
	 *    "title" : "How many employees report to you?",
	 *    "answer" : "100+"
	 *  }
	 * ...
	 * }
	 * 
	 */
	
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		return this;
	}
	
	
	public abstract Map<String,Object> OnSingleStringAnswer(String questionId, String answer, mjson.Json question);
	public abstract Map<String, Object> OnMultipleStringAnswers(String questionId, List<String> answers, mjson.Json question);
	
	
	// ==== TODO: this creates a map of questionId to questionJson, it should be a util class
	protected Map<String, mjson.Json> buildQuestionMap(String surveyId) throws FileNotFoundException, IOException{
		Map<String, mjson.Json> questionsMapping=new HashMap<>();
		Survey s=Survey.findById(surveyId);
		List<mjson.Json> pages=mjson.Json.read(s.getQuestionsAsString()).at("pages").asJsonList();
		for(mjson.Json page:pages){
			for(mjson.Json question:page.at("elements").asJsonList()){
				findInQuestions(question, questionsMapping);
			}
		}
		return questionsMapping;
	}
	private void findInQuestions(mjson.Json question, Map<String, mjson.Json> questionsToTitleMapping){
		if (question.has("elements")){
			// panel or other embedded control?
			for(mjson.Json q:question.at("elements").asJsonList()){
				findInQuestions(q, questionsToTitleMapping);
			}
		}else{
			findInQuestion(question, questionsToTitleMapping);
		}
	}
	private void findInQuestion(mjson.Json question, Map<String, mjson.Json> questionsMapping){
		if ("html".contains(question.at("type").asString())) return; // shortcut these controls since we dont want to know about them
		System.out.println("Question:"+question.at("name").asString());
		questionsMapping.put(question.at("name").asString(), question);
	}
	// ====
	
	
	private Map<String, Object> flattenAndEnrichResults(String surveyId, Map<String,Object> surveyResults) throws FileNotFoundException, IOException{
		Map<String, Object> result=new HashMap<>();
		
		// Extract the question names to titles
		Map<String, mjson.Json> questionsMapping=new HashMap<>();
		Survey s=Survey.findById(surveyId);
		List<mjson.Json> pages=mjson.Json.read(s.getQuestionsAsString()).at("pages").asJsonList();
		for(mjson.Json page:pages){
			for(mjson.Json question:page.at("elements").asJsonList()){
				findInQuestions(question, questionsMapping);
			}
		}
		
		// Now go through answers and enrich with titles
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
//			System.out.println("looking up page for question: "+questionId +"("+e.getValue().getClass().getSimpleName()+")");
			
			if ("language".equalsIgnoreCase(questionId)){
				continue;
			}
			
			if (!questionsMapping.containsKey(questionId)) throw new RuntimeException("Found answers for questions ("+questionId+") that dont exist - config issue?");
				
			
			if (String.class.isAssignableFrom(e.getValue().getClass())){
				String answer=(String)e.getValue();
				
				Map<String,Object> questionData=OnSingleStringAnswer(questionId, answer, questionsMapping.get(questionId));
				
//				Map<String,Object> answerMap=new MapBuilder<String, Object>().put("title", questionsToTitleMapping.get(questionId)).put("answer", answer).build();
				result.put(questionId, questionData);
				
			}else if (ArrayList.class.isAssignableFrom(e.getValue().getClass())){
				ArrayList<String> answerList=(ArrayList<String>)e.getValue();
				
				Map<String, Object> answerData=OnMultipleStringAnswers(questionId, answerList, questionsMapping.get(questionId));
				
//				List<Map<String,Object>> answers=new ArrayList<>();
//				for (String answerString:answerList){
//					Map<String,Object> answerMap=new MapBuilder<String, Object>().put("answer", answerString).build();
//					answers.add(answerMap);
//				}
//				result.put(questionId, new MapBuilder<String,Object>().put("title", questionsToTitleMapping.get(questionId)).put("answers", answers).build());
				result.put(questionId, answerData);
				
			}else if (Map.class.isAssignableFrom(e.getValue().getClass())){
				// assume it's a panel with sub-questions
				//for(Entry<String, String> e2: ((Map<String,String>)e.getValue()).entrySet()){
				//	result.put(e2.getKey(), new MapBuilder<String, Object>().put("title", questionsToTitleMapping.get(questionId)).put("answer", (String)e.getValue()).build());
				//}
				
			}
		}
		
		return result;
	}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
//		try{
			log.debug("Execute. Flattening and enriching the results data");
			
			Map<String, Object> result=flattenAndEnrichResults(surveyId, surveyResults);
			
//			// Store results temporarily to generate the report content (TODO: this could cause a race condition, because if this doesnt get the data in the cache quick enough before the results page tries to load we have an issue)
//			log.debug("putting answers into cache, ready for the results page to render it");
//			log.debug("cache.put("+(surveyId+"_"+visitorId)+") = "+Json.toJson(result));
//			CacheHelper.cache.put(surveyId+"_"+visitorId, Json.toJson(result));
//			
			return result;
//		}catch(JsonProcessingException e){
//			e.printStackTrace();
//			throw e;
//		}
	}

}
