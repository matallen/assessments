package com.redhat.services.ae.plugins.droolsscore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
//import java.util.TreeMap;


public class ResultsBuilderTabsOverview{

	
	public Object build(List<DroolsRecommendation> recommendations, Map<String, Integer> sectionScores){
//		Map<String,Map<String,Map<String,List<String>>>> tabs=new TreeMap<String,Map<String,Map<String,List<String>>>>();
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
			
			
//			Map<String,Map<String,Object>> anOverview=new TreeMap<>();
//			anOverview.put(section, new TreeMap<>());
//			anOverview.get(section).put(title, texts);
			overviews.put(section, new LinkedHashMap<>());
			overviews.get(section).put(title, texts);
//			anOverview.put(title, new MapBuilder<String,Object>()
//					.put("title","")
//					.put("text",texts)
//					.put("score",sectionScores.get(section))
//					.build());
			
//			overviews.put
			
			
//			for (Entry<String, Map<String, List<String>>> e1:e.getValue().entrySet()){
//				String l1Header=e1.getKey();
//				e1.getValue().entrySet()
//				
//				for (Entry<String, List<String>> e2:e1.getValue().entrySet()){
//					String l2Header=e2.getKey();
//				}
//			}
		}
		
		
//		
//		for (DroolsRecommendation r:recommendations){
//			if (null==r.getSection()) throw new RuntimeException("Please check the rules, there is a null section");
//			
//			if (!overviews.containsKey(r.getSection())) overviews.put(r.getSection(), new TreeMap<>());
//			if (r.getLevel1().toLowerCase().contains("overview") || r.getLevel2().toLowerCase().contains("overview")){
//				overviews.get(r.getSection()).put("title", r.getLevel2());
//				overviews.get(r.getSection()).put("text", r.getText());
//				overviews.get(r.getSection()).put("score", sectionScores.get(r.getSection()));
//			}
//			
//			//if (!overviews.get(r.getSection()).containsKey(r.getLevel1())) tabs.get(r.getSection()).put(r.getLevel1(), new LinkedHashMap<>());
//			
//			if (!tabs.containsKey(r.getSection())) tabs.put(r.getSection(), new LinkedHashMap<>());
//			if (!tabs.get(r.getSection()).containsKey(r.getLevel1())) tabs.get(r.getSection()).put(r.getLevel1(), new LinkedHashMap<>());
//			if (!tabs.get(r.getSection()).get(r.getLevel1()).containsKey(r.getLevel2())) tabs.get(r.getSection()).get(r.getLevel1()).put(r.getLevel2(), new LinkedList<>());
//			
//			tabs.get(r.getSection()).get(r.getLevel1()).get(r.getLevel2()).add(r.getText());
//		}
		
		
		Map<Object,Object> result=new LinkedHashMap<>();
		result.put("overviews", overviews);
		result.put("tabs", tabs);
		return result;
	}
}
