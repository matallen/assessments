package com.redhat.services.ae.plugins;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.MetricsDecorator;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;


/**
 * purpose of this class is to store industry trend metrics / averages for comparison purposes within future reports
 * 
 * */
public class IndustryStatisticsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(IndustryStatisticsPlugin.class);
	
	private String metricsName;      // industryTopicByMonth
	private String monthFormat;      // yy-MMM
	private String groupByFieldName; // _Industry
	private String disabledIfExpression;
	boolean extraDebug=false;
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		metricsName=(String)config.get("metricsName");
		monthFormat=(String)config.get("monthFormat");
		groupByFieldName=(String)config.get("groupByFieldName");
		disabledIfExpression=(String)config.get("disabledIf");
		extraDebug=getBooleanFromConfig(config, "extraDebug", false);
		
		if (null==metricsName) metricsName="IndustryScoresByMonth";
		if (null==monthFormat) monthFormat="yy-MMM";
//		if (null==groupByFieldName) groupByFieldName="_Industry";
		if (null==groupByFieldName) throw new RuntimeException("'groupByFieldName' must be set in config. It's the question name of the field you want to group metrics by");
		
		return this;
	}


	class MultiLevelMetricsList extends MetricsDecorator{
		public MultiLevelMetricsList(Map<String, Object> data){ super(data); }
		
		// store the count and increment total so we can extract an average from it at any point
		public void putValue(Map<String,Object> data, String category, Integer incrementBy){
//			data.put(category, data.containsKey(category)?(Integer)data.get(category)+incrementBy:incrementBy);
			
			if (!data.containsKey(category)) data.put(category, new ArrayList<>());
			
			if (!List.class.isAssignableFrom(data.get(category).getClass())){
				log.error("Unable to store score of "+incrementBy+" for to industry "+category+"");
				return;
			}
			
			// Insert data
			List<Integer> categoryList=(List<Integer>)data.get(category);
			categoryList.add(incrementBy);
			data.put(category, categoryList);
			
			// Calculate average
			int total=0;
			for (Integer i:categoryList)
				total+=i;
			data.put(category+"_average", total/categoryList.size());
		}
		
	}
	
//	public static void main(String[] asd) throws Exception{
////		Map<String,Object> metrics=new HashMap<>();
////		MultiLevelMetrics mlm=new IndustryStatisticsPlugin().new MultiLevelMetrics(metrics);
////		mlm.increment(20, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Agriculture");
////		mlm.increment(40, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Technology");
////		mlm.increment(30, "industryTopicByMonth", "20-Jul", "Modernizing Platforms", "Agriculture");
//		
//		IndustryStatisticsPlugin p=new IndustryStatisticsPlugin();
//		p.setConfig(null);
//		Survey s=new Survey();
//		Map<String,Object> surveyResults=new MapBuilder<String,Object>().build();
//		surveyResults.put("_Industry", "Agriculture");
//		surveyResults.put("_sectionScore", new MapBuilder<String,Integer>()
//				.put("Modernizing Platforms", 50)
//				.build());
//		
//		p.execute(s, "Visitor123", surveyResults);
//		p.execute(s, "Visitor456", surveyResults);
//		((Map)surveyResults.get("_sectionScore")).put("Modernizing Platforms", 40);
//		p.execute(s, "Visitor789", surveyResults);
//		
//		
//		System.out.println(Json.toJson(s.getMetrics()));
//	}
	

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		return execute(Survey.findById(surveyId), visitorId, surveyResults);
	}		
	public Map<String, Object> execute(Survey survey, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		boolean disabled=false;
		try{
			Object eval=MVEL.eval(disabledIfExpression, surveyResults);
			disabled=disabled || (eval instanceof Boolean && (boolean)eval);
			if (disabled)
				log.debug(this.getClass().getSimpleName()+":: disabled due to expression evaluating to true: "+disabledIfExpression);
		}catch(Exception ex){
			log.error("Not disabling eloqua plugin, however an expression error occured: "+ex.getMessage());
			ex.printStackTrace();
		}
		
		if (!disabled){
			Map<String,Object> metrics=survey.getMetrics();
			
			// extract industry field (_industry?)
			String YYMMM=new SimpleDateFormat(monthFormat).format(new Date(System.currentTimeMillis()));
			String industry=(String)surveyResults.get(groupByFieldName);
			
			
			// extract _sectionScores
			Map<String,Integer> sectionScores=(Map<String,Integer>)surveyResults.get("_sectionScore");
			
			// increment industry stats
//		Map<String,Object> metrics=new HashMap<>();
//		MultiLevelMetrics mlm=new IndustryStatisticsPlugin().new MultiLevelMetrics(metrics);
			
			MultiLevelMetricsList mlm=new IndustryStatisticsPlugin().new MultiLevelMetricsList(metrics);
			for(Entry<String, Integer> e:sectionScores.entrySet()){
				String section=e.getKey();
				log.info(this.getClass().getSimpleName()+":: Incrementing section "+section+" for "+industry+" industry by "+e.getValue());
				mlm.increment(e.getValue(), metricsName, YYMMM, section, industry);
//			mlm.increment(1, metricsName+"_count", YYMMM, topic, industry);
			}
			
			// save industry stats
//		survey.persist();
			
			survey.saveMetrics();
			
			if (extraDebug)
				log.debug("IndustryStatisticsPlugin:: "+Json.toJson(survey.getMetrics()));
			
		}
		
		// return an unchanged surveyResults
		return surveyResults;
	}

}
