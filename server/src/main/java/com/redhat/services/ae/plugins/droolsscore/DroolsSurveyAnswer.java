package com.redhat.services.ae.plugins.droolsscore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DroolsSurveyAnswer{

	private String language;
	private String pageId;
	private String questionId;
	private String title;
	private Integer score;
	private List<String> answers;
	private List<String> recommendations;

	public DroolsSurveyAnswer(String questionId, String pageId, String language, Integer score, List<String> answers, String title){
		this.questionId=questionId;
		this.pageId=pageId;
		this.language=language;
		this.score=score;
		this.title=title;
		this.answers=answers;
	}
	
	public String toString(){
//		return String.format(this.getClass().getSimpleName()+": id=%s, page=%s, lang=%s, score=%s, answers=%s, title=%s", questionId, pageId, language, score, answers, title );
		// display field only if not null
		List<String> fields2=Lists.newArrayList(
				"id="+questionId,
				"page="+pageId,
				"lang="+language,
				"title="+title,
				"score="+score,
				"answers="+answers
				);
		List<String> predicated=fields2.stream()
				.filter(field -> !field.contains("null"))
				.collect(Collectors.toList());
		return String.format("%s: %s", this.getClass().getSimpleName(), Joiner.on(", ").join(predicated));
	}
	
	
	public List<String> getAnswers(){
		return answers;
	}
	
	public String getAnswer(){
		return answers.size()==1?answers.get(0):null;
	}
	public void setAnswers(List<String> answers){
		this.answers=answers;
	}
	public void setAnswer(String answer){
		if (this.answers==null)this.answers=Lists.newArrayList();
		this.answers.add(answer);
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


	public Integer getScore(){
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
