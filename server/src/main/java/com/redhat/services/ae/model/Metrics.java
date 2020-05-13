package com.redhat.services.ae.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.redhat.services.ae.utils.FluentCalendar;

public class Metrics implements Serializable{
//	public Metrics(){}
//	private int surveyCount;
//	@JsonIgnore
	private Map<String, Integer> surveyCountByMonth;
//	private Map<String, Integer> pageCount;
//	@JsonIgnore
	private Map<String, Map<String, Integer>> pageCountByMonth;
	
//	public Integer getSurveyCount(){ return surveyCount; }
	public Map<String, Integer> getSurveyCountByMonth(){
		if (null==surveyCountByMonth) surveyCountByMonth=new HashMap<>(); return surveyCountByMonth;
	}
//	private Map<String, Integer> getPageCount(){
//		if (null==pageCount) pageCount=new HashMap<>(); return pageCount;
//	}
	public Map<String, Map<String, Integer>> getPageCountByMonth(){
		if (null==pageCountByMonth) pageCountByMonth=new HashMap<>(); return pageCountByMonth;
	}

	@JsonIgnore
	public void incrementSurvey(String YYMMM){
		// completedSurveyCount vs startedSurveyCount ????
		
//		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		// TODO: FIX FIX FIX - DOESNT WORK, INCREMENTS EVERY PAGE!!!!!
		if (!getSurveyCountByMonth().containsKey(YYMMM)) getSurveyCountByMonth().put(YYMMM, 0);
		getSurveyCountByMonth().put(YYMMM, getSurveyCountByMonth().get(YYMMM)+1);
	}
	
	@JsonIgnore
	public void incrementPage(String YYMMM, String pageId){
//		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		if (!getPageCountByMonth().containsKey(YYMMM)) getPageCountByMonth().put(YYMMM, new HashMap<>());
		if (!getPageCountByMonth().get(YYMMM).containsKey(pageId)) getPageCountByMonth().get(YYMMM).put(pageId, 0);
		getPageCountByMonth().get(YYMMM).put(pageId, getPageCountByMonth().get(YYMMM).get(pageId)+1);
		
	}
}
