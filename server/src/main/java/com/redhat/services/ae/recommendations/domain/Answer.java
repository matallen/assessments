package com.redhat.services.ae.recommendations.domain;

import java.util.List;

import com.google.common.collect.Lists;
import com.redhat.services.ae.plugins.RecommendationsPlugin;
import com.redhat.services.ae.plugins.droolsscore.DroolsSurveyAnswer;

public class Answer extends DroolsSurveyAnswer{
//	private String questionId,pageId,language,title;
//	private Integer score;
//	private List<String> answers;
	
	private Answer(String questionId, String pageId, String language, Integer score, List<String> answer, String title){
		super(questionId, pageId, language, score, answer, title);
	}
	
	public String getQuestion(){return getQuestionId();};
	
	public static class builder{
		private String language;
		private String pageId=null;
		private String questionId;
		private String title=null;
		private Integer score=RecommendationsPlugin.noScore;
		private List<String> answer=Lists.newArrayList();
		
		public builder language(String language){this.language=language; return this;}
//		public builder pageId(String pageId){this.pageId=pageId; return this;}
		public builder questionId(String questionId){this.questionId=questionId; return this;}
//		public builder title(String title){this.title=title; return this;}
		public builder score(Integer score){this.score=score; return this;}
		public builder addAnswer(String... answer){for(String a:answer)this.answer.add(a); return this;}
		public builder answer(List<String> answer){this.answer.addAll(answer); return this;}
		
		public Answer build(){
			return new Answer(questionId, pageId, language, score, answer, title);
		}
	}
}
