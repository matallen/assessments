package com.redhat.services.ae.plugins;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;


/**
 * purpose of this class is to store industry trend metrics / averages for comparison purposes within future reports
 * 
 * */
public class IndustryStatisticsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(IndustryStatisticsPlugin.class);

	@Override
	public void setConfig(Map<String, Object> config){}

	class MultiLevelMetrics{
		private Map<String, Object> data;

		public MultiLevelMetrics(Map<String, Object> data){
			this.data=data;
		}

		@SuppressWarnings("unchecked")
		public void increment(Integer incrementBy, String... levels){
			int max=levels.length;
			int c=0;
			Map<String,Object> data=this.data;
			for(String category:levels){
				c=c+1;
				if (c==max){
					data.put(category, data.containsKey(category)?(Integer)data.get(category)+incrementBy:incrementBy);
				}else{
					if (!data.containsKey(category)) 
						data.put(category, new HashMap<String,Object>());
					data=(Map<String,Object>)data.get(category);
				}
			}
		}
	}
	
	
	public static void main(String[] asd) throws JsonProcessingException{
		Map<String,Object> metrics=new HashMap<>();
		MultiLevelMetrics mlm=new IndustryStatisticsPlugin().new MultiLevelMetrics(metrics);
		mlm.increment(1, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Agriculture");
		mlm.increment(1, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Technology");
		mlm.increment(1, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Agriculture");
		System.out.println(Json.toJson(metrics));
	}
	

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		Survey survey=Survey.findById(surveyId);
		
		
//		survey.getMetrics().getIndustrySectionByMonth()
		
		
		
//		"metrics" : {
//      "industrySectionByMonth" : {
//        "20-Jul" : {
//					"Modernizing Platforms" : {
//						"Agriculture" : 1,
//						"Technology" : 3
//					}
//				},
//        "20-Aug" : {
//					
//				}
//      },
//		}
		
		// load industry stats from database/survey
		
		// extract industry field (_industry?)
		String YYMMM=new SimpleDateFormat("yy-MMM").format(new Date(System.currentTimeMillis()));
		String industry=(String)surveyResults.get("_Industry");
		
		// extract _sectionScores
		Map<String,Integer> sectionScores=(Map<String,Integer>)surveyResults.get("_sectionScore");
		
		// increment industry stats
		Map<String,Object> metrics=new HashMap<>();
		MultiLevelMetrics mlm=new IndustryStatisticsPlugin().new MultiLevelMetrics(metrics);
		for(Entry<String, Integer> e:sectionScores.entrySet()){
			String topic=e.getKey();
			mlm.increment(1, "industryTopicByMonth", YYMMM, topic, "Agriculture");
		}
		
		
		// save industry stats
		
		// return an unchanged surveyResults
		return surveyResults;
	}

}
