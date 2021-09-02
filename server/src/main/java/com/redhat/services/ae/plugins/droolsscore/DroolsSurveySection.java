package com.redhat.services.ae.plugins.droolsscore;

import java.util.ArrayList;
import java.util.List;

public class DroolsSurveySection{

	private String language;
	private String section;
	private int score;
	private List<String> recommendations;

	public DroolsSurveySection(String section, String language, int score){
		this.section=section;
		this.language=language;
		this.score=score;
	}
	
	public String toString(){
		return String.format(this.getClass().getSimpleName()+": section=%s, lang=%s, score=%s", section, language, score );
	}
	
	public String getSection(){
		return section;
	}
	public void setSection(String section){
		this.section=section;
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
	
	
}
