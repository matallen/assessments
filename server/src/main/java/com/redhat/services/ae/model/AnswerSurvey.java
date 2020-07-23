package com.redhat.services.ae.model;

public class AnswerSurvey{
	private int score;
	private String language;

	public AnswerSurvey(int score, String language){
		this.score=score;
		this.language=language;
	}
	
	public String getLanguage(){
		return language;
	}

	public void setLanguage(String language){
		this.language=language;
	}

	public int getScore(){
		return score;
	}

	public void setScore(int score){
		this.score=score;
	}
	
}
