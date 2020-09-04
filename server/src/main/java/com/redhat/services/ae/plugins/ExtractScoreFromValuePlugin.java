package com.redhat.services.ae.plugins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.utils.Pair;

public class ExtractScoreFromValuePlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(ExtractScoreFromValuePlugin.class);
	
	/**
	 * 
	 * results plugin needs to flatten the answers, merge with question text, id and score, and update the data structure to generate the results page
	 * 
	 * for example:
	 * {
	 * 	"q_cloud_current_culture_process" : {
	 * 		"title" : "What best describes your current culture and process related to automating tasks?",
	 *    "answers": [
	 *    	{
	 *    		"answer" : "Siloed process",
	 *    		"score" : 10
	 *    	}
	 *    ]
	 * 	},
	 * ...
	 * }
	 * 
	 */
	
	
	@Override
	public void setConfig(Map<String, Object> config){
	}
	
	
	public Pair<String,Integer> splitThis(String answer){
		int score=0; // default score is 1 point for everything
		String id=answer;
		if (answer.contains("#")){
			id=answer.split("#")[1];
			score=Integer.parseInt(answer.split("#")[0]);
		}
		return new Pair<String,Integer>(id, score);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		log.debug("ResultsPlugin:: execute. Flattening and enriching the results data");
		
		Map<String, Object> result=new LinkedHashMap<>();
		
		result.putAll(surveyResults);
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
//			String questionId=e.getKey();
			
			if (Map.class.isAssignableFrom(e.getValue().getClass())){
				Map<String,Object> value=((Map<String,Object>)e.getValue());
				
				if (value.containsKey("answer")){ // single answer
					Pair<String,Integer> answer=splitThis((String)value.get("answer"));
					result.remove("answer");
					Map<String,Object> newAnswerMap=new MapBuilder<String,Object>().put("answer", answer.getFirst()).put("score", answer.getSecond()).build();
					result.put("answer", newAnswerMap);
					
				}else if (value.containsKey("answers")){ // list of answers
					ArrayList<String> answerList=(ArrayList<String>)value.get("answers");
					List<Map<String,Object>> newAnswers=new ArrayList<>();
					result.remove("answer");
					for (String answerString:answerList){
						Pair<String,Integer> answer=splitThis(answerString);
						newAnswers.add(new MapBuilder<String,Object>().put("answer", answer.getFirst()).put("score", answer.getSecond()).build());
					}
					result.put("answer", newAnswers);
				}
				
			}else{
				System.err.println("EEK!");
			}
			
		}
		
		
		return result;
	}

}
