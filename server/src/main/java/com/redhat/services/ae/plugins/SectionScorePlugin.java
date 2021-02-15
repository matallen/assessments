package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.redhat.services.ae.utils.Json;

// EmbeddedScoreTotalPlugin
public class SectionScorePlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(SectionScorePlugin.class);
	private String sectionScoreName="_sectionScore";
	private String scoreStrategy="average";
	private String scoreBy="section";
	private String sectionTitle="navigationTitle";
	private String defaultQuestionScore="0";
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		sectionScoreName=getConfigValueAsString(config, "sectionScoreName", "_sectionScore");
		scoreStrategy=getConfigValueAsString(config, "scoreStrategy", "average");
		scoreBy=getConfigValueAsString(config, "scoreBy", "section");
		sectionTitle=getConfigValueAsString(config, "sectionTitle", "navigationTitle");
		defaultQuestionScore=getConfigValueAsString(config, "defaultQuestionScore", "0");
		
		if (!Lists.newArrayList("average","sum").contains(scoreStrategy))
			throw new RuntimeException("property 'scoreStrategy' must contain either 'average' or 'sum'");
		
		return this;
	}

	private Map<String,Integer> sectionTotals=new HashMap<String, Integer>();
	private Map<String,Integer> sectionCounts=new HashMap<String, Integer>();
	private Map<String,Integer> sectionAverages=new HashMap<String, Integer>();
	
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			
			if (questionId.startsWith("_")) continue;
			
			if (!Map.class.isAssignableFrom(e.getValue().getClass())){
				log.error("Expecting a set of map values for ("+e.getKey()+"), but received the following: "+Json.toJson(e.getValue()));
			}
			Map<String,Object> value=(Map<String,Object>)e.getValue();
			
			// determine question score
			int score=Integer.parseInt(defaultQuestionScore); // default score to plugin default, or 0 if no default specified
			if (value.containsKey("score"))
				score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
			
			// determine which section to attribute the score to
			String sectionName="survey";
			if ("section".equalsIgnoreCase(scoreBy)){
				if (value.containsKey(sectionTitle)){
					sectionName=(String)value.get(sectionTitle);
				}else{
					log.error("Question ("+questionId+") has a score ("+score+"), and scoreBy is 'section' but the question doesnt have a '"+sectionTitle+"' property to group by  ("+value.containsKey(sectionTitle)+"). It's score is being omitted");
					continue;
				}
			}else{ // score the whole assessment together
				sectionName=scoreBy;
			}
			
			// Perform the scoring & assignment
//			score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
			log.debug(questionId+"::Adding score "+score+" to section ["+sectionName+"]");
			sectionTotals.put(sectionName, sectionTotals.containsKey(sectionName)?sectionTotals.get(sectionName)+score:score);
			sectionCounts.put(sectionName, sectionCounts.containsKey(sectionName)?sectionCounts.get(sectionName)+1:1);
			sectionAverages.put(sectionName, sectionTotals.get(sectionName)/sectionCounts.get(sectionName));
			
			
//			if (value.containsKey("score")){
//				
//				String sectionName="survey";
//				int score=0;
//				if ("section".equalsIgnoreCase(scoreBy)){
//					if (value.containsKey(sectionTitle)){
//						sectionName=(String)value.get(sectionTitle);
//						
//					}else{
//						log.error("Question ("+questionId+") has a score ("+value.get("score")+"), and scoreBy is 'section' but the question doesnt have a '"+sectionTitle+"' property to group by  ("+value.containsKey(sectionTitle)+"). It's score is being omitted");
//						continue;
//					}
//				}else{ // score the whole assessment together
//					sectionName=scoreBy;
//				}
//				
//				score=Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):0; // it must be an integer score, I dont want to deal with string conversions
//				log.debug(questionId+"::Adding score "+score+" to section ["+sectionName+"]");
//				sectionTotals.put(sectionName, sectionTotals.containsKey(sectionName)?sectionTotals.get(sectionName)+score:score);
//				sectionCounts.put(sectionName, sectionCounts.containsKey(sectionName)?sectionCounts.get(sectionName)+1:1);
//				sectionAverages.put(sectionName, sectionTotals.get(sectionName)/sectionCounts.get(sectionName));
//				
//			}
			
		}
		
		if ("sum".equalsIgnoreCase(scoreStrategy)){
			surveyResults.put(sectionScoreName, sectionTotals);
		}else{ // 'average' then do this
			surveyResults.put(sectionScoreName, sectionAverages);
		}
		
		if (sectionAverages.size()<=0){
			log.error(this.getClass().getSimpleName()+":: Likely error -> no scores detected therefore no section average scores!");
			log.error("surveyResults at time of ERROR (it's looking for 'score' property of each question, then sum-by 'navigationTitle'):/n"+Json.toJson(surveyResults));
		}
		
		log.debug("SectionScores = "+Json.toJson(sectionAverages));
		return surveyResults;
	}

}
