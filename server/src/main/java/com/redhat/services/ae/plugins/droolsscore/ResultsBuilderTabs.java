package com.redhat.services.ae.plugins.droolsscore;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.redhat.services.ae.recommendations.domain.Recommendation;

public class ResultsBuilderTabs{

	
	public Map<String,Map<String,Map<String,List<String>>>> build(List<Recommendation> recommendations, Map<String, Integer> sectionScores){
		Map<String,Map<String,Map<String,List<String>>>> resultSections=new LinkedHashMap<String,Map<String,Map<String,List<String>>>>();
		
		for (Recommendation r:recommendations){
//			System.out.println("XXXRecommendation - "+r);
			//if (null==r.getSection()) throw new RuntimeException("Please check the rules, there is a null section");
			if (null==r.getSection()) r.setSection("Recommendations");
			if (null==r.getLevel1()) r.setLevel1(" ");
			if (null==r.getLevel2()) r.setLevel2(" ");
			
			if (!resultSections.containsKey(r.getSection())) resultSections.put(r.getSection(), new LinkedHashMap<>());
			if (!resultSections.get(r.getSection()).containsKey(r.getLevel1())) resultSections.get(r.getSection()).put(r.getLevel1(), new LinkedHashMap<>());
			if (!resultSections.get(r.getSection()).get(r.getLevel1()).containsKey(r.getLevel2())) resultSections.get(r.getSection()).get(r.getLevel1()).put(r.getLevel2(), new LinkedList<>());
			
			resultSections.get(r.getSection()).get(r.getLevel1()).get(r.getLevel2()).add(r.getText());
				
		}
		return resultSections;
	}
}
