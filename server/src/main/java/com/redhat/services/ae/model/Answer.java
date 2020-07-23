package com.redhat.services.ae.model;

import java.util.ArrayList;
import java.util.List;

public class Answer{

	private String language;
	private String questionId;
	private String title;
	private int score;
	private List<String> recommendations;

	public Answer(String questionId, String language, int score, String title){
		this.questionId=questionId;
		this.language=language;
		this.score=score;
		this.title=title;
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

	public void setScore(int score){
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
