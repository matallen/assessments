package com.redhat.services.ae.plugins.droolsscore;

public class DroolsSurveyScore{
	private int score;
	private String language;

	public String toString(){
		return String.format(DroolsSurveyScore.class.getSimpleName()+": score=%s, lang=%s", score, language);
	}
	
	public DroolsSurveyScore(int score, String language){
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
