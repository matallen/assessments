package com.redhat.services.ae.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AnswerProcessor{
	
	boolean includeUnderscoreQuestions;
	public AnswerProcessor(boolean includeUnderscoreQuestions){
		this.includeUnderscoreQuestions=includeUnderscoreQuestions;
	}
	public abstract void onStringAnswer(String question, String answer, Integer score);
	public abstract void onArrayListAnswer(String question, List<Answer> answers, Integer averageScore);
	public abstract void onMapAnswer(String question, Answer answer);
	
	public class Answer{
		public String id;
		public Integer score;
	}
	
	public Answer splitThis(String answer){
		Answer result=new Answer();
		result.score=0; // default score is 1 point for everything. Shouldnt this be 0 as default? what would this impact?
		result.id=answer;
		if (answer.contains("#")){
//			try{
				result.id=answer.split("#")[1];
				result.score=Integer.parseInt(answer.split("#")[0]);
//			}catch(Exception ex){
//				System.err.println("error: questionId was '"++"', answer was '"+answer+"'");
//				throw ex;
//			}
		}
		return result;
	}
	
	public void process(Map<String,Object> data){
		for (Entry<String, Object> e:data.entrySet()){
			String questionId=e.getKey();
			
			if (!includeUnderscoreQuestions && questionId.startsWith("_")) continue;
			
			// TODO: What do we do if the answer has no score, split evenly from the number of available options? but that means we need to parse the questions configuration too
			
			if (String.class.isAssignableFrom(e.getValue().getClass())){
				Answer answer=splitThis((String)e.getValue());
				onStringAnswer(questionId, answer.id, answer.score);
				
			}else if (ArrayList.class.isAssignableFrom(e.getValue().getClass())){
				// quick safe string conversion to prevent the list containing anything but a string
				List<String> safeAnswers=new ArrayList<>();
				for (Object s:(ArrayList)e.getValue()){
					safeAnswers.add(s.toString());
				}
				
//				List<String> answerList=safe;
				List<Answer> answers=new ArrayList<>();
				int totalScore=0;
				for (String answerString:safeAnswers){
					answers.add(splitThis(answerString));
				}
				for (Answer a:answers)
					totalScore+=a.score;
				
				onArrayListAnswer(questionId, answers, answers.size()>0?totalScore/answers.size():0);
				
//				System.out.println("erm, what if the answer is not a string????");
			}else if (Map.class.isAssignableFrom(e.getValue().getClass())){
				// assume it's a panel with sub-questions
				for(Entry<String, String> e2: ((Map<String,String>)e.getValue()).entrySet()){
					onMapAnswer(e2.getKey(), splitThis(e2.getValue()));
				}
				
			}
		}
	}
}
