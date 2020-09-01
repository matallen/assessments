package com.redhat.services.ae.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redhat.services.ae.MapBuilder;

import mjson.Json;

public class AddTitleAndScorePlugin extends EnrichAnswersPluginBase{

	/**
	 * 
	 * This plugin does multiple jobs:
	 * 1) adds a title to the answer structure
	 * 2) extracts the 10# from "10#answer text" as the answer value (ie. we embed the score in the answer rather than parsing questions and answer config)
	 * 2) it then adds the score to the answer structure. for multiple answer questions such as checkboxes, it takes the highest score
	 * 
	 */
	
	private String getPageName(Json question){
		Json page=getPageJson(question);
		if (null!=page) return page.at("name").asString();
		return null;
	}
	
	private Json getPageJson(Json question){
		int i=0;
		List<Json> l=new ArrayList<>();
		Json p=question;
		Json page=null;
		while(p!=null){
			if (!p.isArray() && p.has("elements")){
				page=p;
//				break; // first?
			}
			l.add(p);
			i=i+1;
			p=p.up();
		}
		if (null!=page && page.has("name")){
			return page;
		}
		return null;
	}
	
	@Override
	public Map<String, Object> OnSingleStringAnswer(String questionId, String answer, Json question){
		
		Answer answerSplit=splitThis((String)answer);
		
		Map<String,Object> answerData=new MapBuilder<String,Object>().put("answer", answerSplit.text).build();
		
		if (null==question) return answerData;

//		if (answerSplit.score>=0){
//			answerData.put("score", answerSplit.score);
//		}
		
		common(answerSplit.score, question, answerData);
		
		return answerData;
	}

	@Override
	public Map<String, Object> OnMultipleStringAnswers(String questionId, List<String> answers, Json question){
		
		int highestScore=0; /// for RTI v2 we are going to take the highest score vs average or any other complex calc
		
		List<String> newAnswers=new ArrayList<>();
		for (String answerString:answers){
			Answer answerSplit=splitThis((String)answerString);
			if (answerSplit.score>=0){
				highestScore=Math.max(answerSplit.score, highestScore);
			}
			newAnswers.add(answerSplit.text);
		}
		
		Map<String,Object> answerData=new MapBuilder<String,Object>().put("answers", newAnswers).build();
		
//		if (highestScore>=0){
//			answerData.put("score", highestScore);
//		}
		
		common(highestScore, question, answerData);
		
		return answerData;
	}
	
	private void common(int score, Json question, Map<String,Object> answerData){
		if (score>=0) answerData.put("score", score);
		if (question.has("title")) answerData.put("title", question.at("title").asString());
		Json pageJson=getPageJson(question);
		if (null!=pageJson){
			answerData.put("pageId", pageJson.at("name").asString());
			if (pageJson.has("navigationTitle")) answerData.put("navigationTitle", pageJson.at("navigationTitle").asString());
		}
	}

	
	
	class Answer{
		private String text;
		private int score;
	}
	
	public Answer splitThis(String answer){
		Answer result=new Answer();
		result.score=-1; // default score is X point for everything
		result.text=answer;
		if (answer.contains("#")){
			result.text=answer.split("#")[1];
			result.score=Integer.parseInt(answer.split("#")[0]);
		}
		return result;
	}
}
