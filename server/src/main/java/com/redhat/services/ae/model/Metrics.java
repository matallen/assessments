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
	private Map<String, Integer> completedByMonth;
//	private Map<String, Integer> pageCount;
//	@JsonIgnore
//	private Map<String, Map<String, Integer>> pageCountByMonth;
	
//	public Integer getSurveyCount(){ return surveyCount; }
	public Map<String, Integer> getCompletedByMonth(){
		if (null==completedByMonth) completedByMonth=new HashMap<>(); return completedByMonth;
	}
//	private Map<String, Integer> getPageCount(){
//		if (null==pageCount) pageCount=new HashMap<>(); return pageCount;
//	}
//	public Map<String, Map<String, Integer>> getPageByMonth(){
//		if (null==pageCountByMonth) pageCountByMonth=new HashMap<>(); return pageCountByMonth;
//	}

//	@JsonIgnore
//	public void incrementSurvey(String YYMMM){
//		// completedSurveyCount vs startedSurveyCount ????
//		
////		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
//		// TODO: FIX FIX FIX - DOESNT WORK, INCREMENTS EVERY PAGE!!!!!
//		if (!getSurveyByMonth().containsKey(YYMMM)) getSurveyByMonth().put(YYMMM, 0);
//		getSurveyByMonth().put(YYMMM, getSurveyByMonth().get(YYMMM)+1);
//	}
	
//	@JsonIgnore
//	public Map<String, Integer> getPageByMonth(String YYMMM){
//		if (!getPageByMonth().containsKey(YYMMM)) getPageByMonth().put(YYMMM, new HashMap<>());
//		return getPageByMonth().get(YYMMM);
//	}
//	@JsonIgnore
//	public void incrementPage(String YYMMM, String pageId){
//		if (!getPageByMonth(YYMMM).containsKey(pageId)) getPageByMonth(YYMMM).put(pageId, 0);
//		getPageByMonth(YYMMM).put(pageId, getPageByMonth(YYMMM).get(pageId)+1);
//	}
	
	
	//          Type        Month       Geo
	private Map<String, Map<String, Map<String, Integer>>> byMonth;
	@JsonIgnore
	public Map<String, Map<String, Integer>> getByMonth(String type){
		if (null==byMonth) byMonth=new HashMap<>();
		if (!byMonth.containsKey(type)) byMonth.put(type, new HashMap<>());
		return byMonth.get(type);
	}
	@JsonIgnore
	public Map<String, Integer> getByMonth(String type, String YYMMM){
		if (null==byMonth) byMonth=new HashMap<>();
		if (!getByMonth(type).containsKey(YYMMM)) getByMonth(type).put(YYMMM, new HashMap<>());
		return byMonth.get(type).get(YYMMM);
	}
	
	
}
