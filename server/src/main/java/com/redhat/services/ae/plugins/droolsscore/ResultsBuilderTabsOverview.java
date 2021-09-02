package com.redhat.services.ae.plugins.droolsscore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.redhat.services.ae.recommendations.domain.Recommendation;

public class ResultsBuilderTabsOverview{
	private boolean includeOverviewTab=true;
	
	public ResultsBuilderTabsOverview includeOverviewTab(boolean value){
		this.includeOverviewTab=value; return this;
	}
	
	public Object build(List<Recommendation> recommendations, Map<String, Integer> sectionScores, Map<String, Integer> thresholds){
		Map<String, Map<String,Object>> overviews=new LinkedHashMap<>();
		
// Example output for overviews:
//		"overviews":{
//			"Mod Plat":{
//				"score": 40,
//				"title": "",
//				"text": ""
//				},
//				"Mod App":{
//				"score": 20,
//				"title": "",
//				"text": ""
//			},
//		}
		
		Map<String, Map<String, Map<String, List<String>>>> tabs=new ResultsBuilderTabs().build(recommendations, sectionScores);
		for(Entry<String, Map<String, Map<String, List<String>>>> e:tabs.entrySet()){
			String section=e.getKey();
			
			Entry<String, Map<String, List<String>>> l1Items=e.getValue().entrySet().iterator().next();
			Entry<String, List<String>> first=l1Items.getValue().entrySet().iterator().next();
			String title=first.getKey();
			List<String> texts=first.getValue();
			
			overviews.put(section, new LinkedHashMap<>());
			overviews.get(section).put(title, texts);
		}
		
		Map<Object,Object> result=new LinkedHashMap<>();
		if (includeOverviewTab)
			result.put("overviews", overviews);
		result.put("tabs", tabs);
		result.put("thresholds", thresholds);
		return result;
	}
}
