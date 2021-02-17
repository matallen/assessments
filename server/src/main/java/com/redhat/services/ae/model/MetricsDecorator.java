package com.redhat.services.ae.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Maps;

public class MetricsDecorator{
	public static final Logger log=LoggerFactory.getLogger(MetricsDecorator.class);
	private Map<String, Object> data;

	public MetricsDecorator(Map<String, Object> data){
		this.data=data;
	}
	
	@SuppressWarnings("unchecked")
	public void increment(Integer incrementBy, String... levels){
		int max=levels.length;
		int c=0;
		Map<String,Object> data=this.data;
		for(String category:levels){
			c=c+1;
			if (null==category){ // code to discover an exception
				log.error("Cant put a null key in a map if it's going to be written to Json - aborting metrics increment here");
				return;
			}
			if (c==max){
				putValue(data, category, incrementBy);
				
			}else{
				if (!data.containsKey(category)) 
					data.put(category, new LinkedHashMap<String,Object>());
				
				data=(Map<String,Object>)data.get(category);
			}
		}
	}
	
	// default implementation, increment by the incrementBy value
	protected void putValue(Map<String,Object> data, String category, Integer incrementBy){
		data.put(category, data.containsKey(category)?(Integer)data.get(category)+incrementBy:incrementBy);
	}
	
	
	// ALIAS METHODS FOR THE GENERIC CALLS BELOW
	public Map<String, Integer> getCompletedSurveys(){
		return get("completedSurveysByMonth");
	}
	public Map<String, Map<String, Integer>> getPageTransitions(){
		return get2("pageTransitionsByMonth");
	}
	public Map<String, Map<String, Integer>> getGeo(){
		return get2("geoByMonth");
	}
	public Map<String, Map<String, Map<String, Integer>>> getAnswersDistribution(){
		return get3("answerDistribution");
	}
	
	
//	public Map<String, Map<String, Integer>> getByMonth(String key){
//		return get2("byMonth", key);
//	}
//	public Map<String, Integer> getByMonth(String key, String key2){
//		return get("byMonth", key, key2);
//	}
	
//	public Map<String, Map<String, Map<String, Integer>>> getAnswersByMonth(String key){
//		return get3("answersByMonth", key);
//	}
	
	
	public Map<String, Map<String, Map<String, Integer>>> get3(String... keys){
		if (null==this.data) return Maps.newHashMap();
		Object thisData=this.data;
		for(String k:keys){
			Map<String, Object> newMap=((Map<String,Object>)thisData);
			if (!newMap.containsKey(k)) return Maps.newHashMap();
			thisData=newMap.get(k);
		}
		return (Map<String, Map<String, Map<String, Integer>>>)thisData;
	}
	
	
	public Map<String, Map<String, Integer>> get2(String... keys){
		if (null==this.data) return Maps.newHashMap();
		Object thisData=this.data;
		for(String k:keys){
			Map<String, Object> newMap=((Map<String,Object>)thisData);
			if (!newMap.containsKey(k)) return Maps.newHashMap();
			thisData=newMap.get(k);
		}
		return (Map<String, Map<String, Integer>>)thisData;
	}
	
	
	public Map<String, Integer> get(String... keys){
		if (null==this.data) return Maps.newHashMap();
		Object thisData=this.data;
		for(String k:keys){
			Map<String, Object> newMap=((Map<String,Object>)thisData);
			if (!newMap.containsKey(k)) return Maps.newHashMap();
			thisData=newMap.get(k);
		}
		
		return (Map<String,Integer>)thisData;
	}
}
