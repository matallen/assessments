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
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;


/**
 * purpose of this class is to store industry trend metrics / averages for comparison purposes within future reports
 * 
 * */
public class IndustryStatisticsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(IndustryStatisticsPlugin.class);
	
	private String metricsName;      // industryTopicByMonth
	private String monthFormat;      // YY-MMM
	private String groupByFieldName; // _Industry
	
	@Override
	public void setConfig(Map<String, Object> config){
		metricsName="industryTopicByMonth";
		monthFormat="yy-MMM";
		groupByFieldName="_Industry";
	}

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
					putValue(data, category, incrementBy);
//					data.put(category, data.containsKey(category)?(Integer)data.get(category)+incrementBy:incrementBy);
				}else{
					if (!data.containsKey(category)) 
						data.put(category, new HashMap<String,Object>());
					data=(Map<String,Object>)data.get(category);
				}
			}
		}
		
		// default implementation, increment by the incrementBy value
		public void putValue(Map<String,Object> data, String category, Integer incrementBy){
			data.put(category, data.containsKey(category)?(Integer)data.get(category)+incrementBy:incrementBy);
		}
	}
	
	class MultiLevelMetricsAverage extends MultiLevelMetrics{
		public MultiLevelMetricsAverage(Map<String, Object> data){ super(data); }
		
		// store the count and increment total so we can extract an average from it at any point
		public void putValue(Map<String,Object> data, String category, Integer incrementBy){
			data.put(category, data.containsKey(category)?(Integer)data.get(category)+incrementBy:incrementBy);
		}
		
		public Integer getAverage(){
			return 0;
		}
	}
	
	
	public static void main(String[] asd) throws Exception{
//		Map<String,Object> metrics=new HashMap<>();
//		MultiLevelMetrics mlm=new IndustryStatisticsPlugin().new MultiLevelMetrics(metrics);
//		mlm.increment(20, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Agriculture");
//		mlm.increment(40, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Technology");
//		mlm.increment(30, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Agriculture");
		
		IndustryStatisticsPlugin p=new IndustryStatisticsPlugin();
		p.setConfig(null);
		Survey s=new Survey();
		Map<String,Object> surveyResults=new MapBuilder<String,Object>().build();
		surveyResults.put("_Industry", "Agriculture");
		surveyResults.put("_sectionScore", new MapBuilder<String,Integer>()
				.put("Modernizing Platforms", 50)
				.build());
		
		p.execute(s, "V_123", surveyResults);
		
		System.out.println(Json.toJson(s.getMetrics()));
	}
	

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		return execute(Survey.findById(surveyId), visitorId, surveyResults);
	}		
	public Map<String, Object> execute(Survey survey, String visitorId, Map<String, Object> surveyResults) throws Exception{

		Map<String,Object> metrics=survey.getMetrics();
		
//		survey.getMetrics().getIndustrySectionByMonth()
		
		
		
//		{
//		  "industryTopicByMonth_total" : {
//		    "20-Sep" : {
//		      "Modernizing Platforms" : {
//		        "Agriculture" : 50
//		      }
//		    }
//		  },
//		  "industryTopicByMonth_count" : {
//		    "20-Sep" : {
//		      "Modernizing Platforms" : {
//		        "Agriculture" : 1
//		      }
//		    }
//		  }
//		}
		
		// load industry stats from database/survey
		
		// extract industry field (_industry?)
		String YYMMM=new SimpleDateFormat(monthFormat).format(new Date(System.currentTimeMillis()));
		String industry=(String)surveyResults.get(groupByFieldName);
		
		// extract _sectionScores
		Map<String,Integer> sectionScores=(Map<String,Integer>)surveyResults.get("_sectionScore");
		
		// increment industry stats
//		Map<String,Object> metrics=new HashMap<>();
		MultiLevelMetrics mlm=new IndustryStatisticsPlugin().new MultiLevelMetrics(metrics);
		for(Entry<String, Integer> e:sectionScores.entrySet()){
			String topic=e.getKey();
			mlm.increment(e.getValue(), metricsName, YYMMM, topic, industry);
//			mlm.increment(1, metricsName+"_count", YYMMM, topic, industry);
		}
		
		// save industry stats
//		survey.persist();
		
		// return an unchanged surveyResults
		return surveyResults;
	}

}
