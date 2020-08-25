package com.redhat.services.ae.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Metrics implements Serializable{
	private Map<String, Integer> completedByMonth;
	public Map<String, Integer> getCompletedByMonth(){
		if (null==completedByMonth) completedByMonth=new LinkedHashMap<>(); return completedByMonth;
	}
	
	
	//          Type        Month       Geo
	private Map<String, Map<String, Map<String, Integer>>> byMonth;
	private Map<String, Map<String, Map<String, Map<String,Integer>>>> answersByMonth;
	@JsonIgnore
	public Map<String, Map<String, Integer>> getByMonth(String type){
		if (null==byMonth) byMonth=new LinkedHashMap<>();
		if (!byMonth.containsKey(type)) byMonth.put(type, new LinkedHashMap<>());
		return byMonth.get(type);
	}
	@JsonIgnore
	public Map<String, Integer> getByMonth(String type, String YYMMM){
		if (null==byMonth) byMonth=new LinkedHashMap<>();
		if (!getByMonth(type).containsKey(YYMMM)) getByMonth(type).put(YYMMM, new LinkedHashMap<>());
		return byMonth.get(type).get(YYMMM);
	}
	
	@JsonIgnore
	public Map<String, Map<String,Map<String,Integer>>> getAnswersByMonth(String type){
		if (null==answersByMonth) answersByMonth=new LinkedHashMap<>();
		if (!answersByMonth.containsKey(type)) answersByMonth.put(type, new LinkedHashMap<>());
		return answersByMonth.get(type);
	}
	@JsonIgnore
	public Map<String, Map<String,Integer>> getAnswersByMonth(String type, String YYMMM){
		if (null==answersByMonth) answersByMonth=new LinkedHashMap<>();
		if (!answersByMonth.containsKey(type)) answersByMonth.put(type, new LinkedHashMap<>());
		if (!answersByMonth.get(type).containsKey(YYMMM)) answersByMonth.get(type).put(YYMMM, new LinkedHashMap<>());
		return answersByMonth.get(type).get(YYMMM);
	}
	
}
