package com.redhat.services.ae.plugins.droolsscore;

import java.util.ArrayList;
import java.util.List;

public class DroolsSurveyAnswer{

	private String language;
	private String pageId;
	private String questionId;
	private String title;
	private Integer score;
	private List<String> recommendations;

	public DroolsSurveyAnswer(String questionId, String pageId, String language, Integer score, String title){
		this.questionId=questionId;
		this.pageId=pageId;
		this.language=language;
		this.score=score;
		this.title=title;
	}
	
	public String toString(){
		return String.format(DroolsSurveyAnswer.class.getSimpleName()+": id=%s, page=%s, lang=%s, score=%s, title=%s", questionId, pageId, language, score, title );
	}
	
	public String getPageId(){
		return pageId;
	}


	public void setPageId(String pageId){
		this.pageId=pageId;
	}


	public String getLanguage(){
		return language;
	}

	public void setLanguage(String language){
		this.language=language;
	}


	public List<String> getRecommendations(){
		if (null==recommendations) recommendations=new ArrayList<>();
		return recommendations;
	}
	public void addRecommendation(String recommendation){
		getRecommendations().add(recommendation);
	}
	public void setRecommendations(List<String> recommendations){
		this.recommendations=recommendations;
	}


	public int getScore(){
		return score;
	}

	public void setScore(Integer score){
		this.score=score;
	}

	public String getQuestionId(){
		return questionId;
	}

	public void setQuestionId(String questionId){
		this.questionId=questionId;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title=title;
	}
	
	
}
