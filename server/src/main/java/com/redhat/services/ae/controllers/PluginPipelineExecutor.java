package com.redhat.services.ae.controllers;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.plugins.UpdateAnswerMetricsPlugin;
import com.redhat.services.ae.utils.Json;

public class PluginPipelineExecutor{
	public static final Logger log=LoggerFactory.getLogger(PluginPipelineExecutor.class);
	
	public List<Plugin> getActivePlugins(Survey o, Map<String,Object> surveyData) throws IOException{
		Map<String, Map<String, Object>> activePluginMap=new LinkedHashMap<String, Map<String,Object>>();
		// Plugins:: Add any mandatory plugins at the start of the plugin chain
		if (!Iterables.any(o.getActivePlugins().values(), new Predicate<Map<String,Object>>(){public boolean apply(@Nullable Map<String,Object> pluginCfg){
			return pluginCfg.get("className").equals(UpdateAnswerMetricsPlugin.class.getName());}})){
			log.info("Adding default 'UpdateAnswerMetrics' since it was not found in plugin chain");
			activePluginMap.put("UpdateAnswerMetrics", new MapBuilder<String,Object>().put("active", true).put("className",UpdateAnswerMetricsPlugin.class.getName()).build());
		}
		activePluginMap.putAll(o.getActivePlugins());
		// Plugins:: Add any mandatory plugins at the end of the plugin chain
//		if (!Iterables.any(activePluginMap.values(), new Predicate<Map<String,Object>>(){public boolean apply(@Nullable Map<String,Object> pluginCfg){
//			return pluginCfg.get("className").equals(RemovePIIAnswersPlugin.class.getName());}})){
//			activePluginMap.put("RemovePIIAnswers", new MapBuilder<String,Object>().put("active", true).put("className",RemovePIIAnswersPlugin.class.getName()).build());
//		}
		
		
		// Plugins:: Execute post-survey plugins
		List<Plugin> activePlugins=new LinkedList<Plugin>();
		Map<String,Object> originalSurveyData=new LinkedHashMap<>(surveyData);
		// Plugins:: Create Plugin list
		for(Entry<String, Map<String, Object>> pl:activePluginMap.entrySet()){
			try{
				Plugin plugin=(Plugin)Class.forName((String)pl.getValue().get("className")).newInstance();
				plugin._setConfig(pl.getValue());
				plugin.setOriginalSurveyResults(originalSurveyData);
				activePlugins.add(plugin);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return activePlugins;
	}
	
	public Map<String,Object> execute(Survey o, Map<String,Object> surveyData, String visitorId, List<Plugin> activePlugins) throws IOException{
		
		// Plugins:: Execute post-survey plugins
		for(Plugin plugin:activePlugins){
			try{
				surveyData=plugin.execute(o.getSurveyId(), visitorId, surveyData); // after each plugin, keep the changes to the data (similar to the concept of Tomcat filters)
//				WARNING: THIS IS HEAVY DEBUGGING
//				System.out.println("After plugin ["+plugin.getClass().getSimpleName()+"]: "+Json.toJson(surveyData));
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		// Plugins:: Post-execution, cleanup
		for(Plugin plugin:activePlugins){
			plugin.onDestroy(o.getSurveyId(), visitorId, surveyData);
		}
		
		return surveyData;
	}
}
