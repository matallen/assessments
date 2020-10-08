package com.redhat.services.ae.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.redhat.services.ae.MapBuilder;

import mjson.Json;

public class AddTitleAndScorePlugin extends EnrichAnswersPluginBase{
	
	private String scoreStrategy="highest";
	private Map<String,String> addQuestionFields=new MapBuilder<String,String>()
			.put("title", "title")
			.put("pageId", "page/name")
			.put("navigationTitle", "page/navigationTitle")
			.build();
	/**
	 * 
	 * This plugin does multiple jobs:
	 * 1) adds a title to the answer structure
	 * 2) extracts the 10# from "10#answer text" as the answer value (ie. we embed the score in the answer rather than parsing questions and answer config)
	 * 2) it then adds the score to the answer structure. for multiple answer questions such as checkboxes, it takes the highest score
	 * 
	 */
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		if (config.containsKey("scoreStrategy")) scoreStrategy=(String)config.get("scoreStrategy");
		if (config.containsKey("addQuestionFields")) addQuestionFields=(Map<String,String>)config.get("addQuestionFields");
		return this;
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
	
	private Answer findInQuestions(List<Json> questionChoices, String answerProvided){
		Answer result=new Answer(null,-1);
//		result.score=-1;
		for (Json ans:questionChoices){
			if (ans.isString()) break;  // this is for surveyjs choicesFromUrl questions that have a null string in the choices
			if (ans.has("value") && ans.has("text")){
				String qValue=ans.at("value").asString();
				String qText=ans.at("text").asString();
				
				if (answerProvided.equals(qValue)){
					if (ans.has("score"))
						result.score=Integer.parseInt(ans.at("score").asString());
					result.value=qValue;
//				System.out.println("found answer in question 'score' property");
					return result;
				}
			}
		}
		return result;
	}
	
	private Answer getAnswerScore(String answer, Json question){
		
		
		if (answer.contains("#")){ // get the score embedded in the answer. ie. "10#answer text"
			return splitThis((String)answer);
		}else{ // get the score from the question 'score' property
			String questionType=question.at("type").asString();
			
//			System.out.println("looking in question "+question.at("name").asString());
			
			if (question.has("choices")){
				return findInQuestions(question.at("choices").asJsonList(), answer);
			}else{
				log.error("Answer didnt contain a # score delimiter, AND question has no 'choices', unable to determine score -> "+question.at("name").asString()+" ");
//				throw new RuntimeException("Question has no 'choices' -> "+question.asString()+" ");
				Answer a=new Answer(answer,-1);
				return a;
			}
		}
	}
	
	@Override
	public Map<String, Object> OnSingleStringAnswer(String questionId, String answer, Json question){
		
		Answer answerSplit=getAnswerScore(answer, question);
		
		Map<String,Object> answerData=new MapBuilder<String,Object>().put("answer", answerSplit.value).build();
		
		if (null==question) return answerData;

		common(answerSplit.score, question, answerData);
		
		return answerData;
	}

	@Override
	public Map<String, Object> OnMultipleStringAnswers(String questionId, List<String> answers, Json question){
		
		int score=-1; /// for RTI v2 we are going to take the highest score vs average or any other complex calc
		
		List<String> newAnswers=new ArrayList<>();
		for (String answerString:answers){
			Answer answerSplit=getAnswerScore(answerString, question);
			
			if (answerSplit.score>=0){
				if ("sum".equalsIgnoreCase(scoreStrategy)){
					if (score==-1) score=0; // this is so we can use -1 as "unset", but that += doesnt start counting at -1
					score+=answerSplit.score;
				}else{ // by default take the highest
					score=Math.max(answerSplit.score, score);
				}
			}
			newAnswers.add(answerSplit.value);
		}
		
		Map<String,Object> answerData=new MapBuilder<String,Object>().put("answers", newAnswers).build();
		
		common(score, question, answerData);
		
		return answerData;
	}
	
	private void common(int score, Json question, Map<String,Object> answerData){
		if (score>=0) answerData.put("score", score);
		
		// now enrich with other question fields
		Json pageJson=getPageJson(question);
		for(Entry<String, String> e:addQuestionFields.entrySet()){
			if (e.getValue().startsWith("page/")){
				String value=e.getValue().split("/")[1];
				if (pageJson!=null){
					if (pageJson.has(value)) answerData.put(e.getKey(), pageJson.at(value).asString());
				}
			}else{ // question level
				if (question.has(e.getValue())) answerData.put(e.getKey(), question.at(e.getValue()).asString());		
			}
		}
		
//		if (question.has("title")) answerData.put("title", question.at("title").asString());
//		Json pageJson=getPageJson(question);
//		if (null!=pageJson){
//			answerData.put("pageId", pageJson.at("name").asString());
//			if (pageJson.has("navigationTitle")) answerData.put("navigationTitle", pageJson.at("navigationTitle").asString());
//		}
	}
	
	@Override
	public void onDestroy(String surveyId, String visitorId, Map<String, Object> surveyResults){
		// At the end of the plugin execution, remove any fields added as cleanup
		removeAnswerProperties(surveyResults, Lists.newArrayList("title", "navigationTitle", "pageId"));
	}

	
	
	class Answer{
		public Answer(String value, int score){
			this.score=score;
			this.value=value;
		}
		private String value;
		private int score;
	}
	
	public Answer splitThis(String answer){
		Answer result=new Answer(answer, -1);
//		result.score=-1; // default score is X point for everything
//		result.value=answer;
		if (answer.contains("#")){
			result.value=answer.split("#")[1];
			result.score=Integer.parseInt(answer.split("#")[0]);
		}
		return result;
	}
}
