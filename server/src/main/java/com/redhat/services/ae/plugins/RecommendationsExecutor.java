package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.redhat.services.ae.recommendations.domain.Recommendation;

public abstract class RecommendationsExecutor{
	
	public enum Type{drlBuilder,recommender} // recommender means it returns a list of Recommendations, a drlBuilder adds to the drls to be executed later/together
	public abstract Type getType();
//	public abstract List<Recommendation> getListOfRecommendations(String surveyId, Map<String,Object> surveyResults) throws Exception;
//	public abstract List<String> getListOfDrlRules(String surveyId) throws Exception;
	public List<Recommendation> getListOfRecommendations(String surveyId, Map<String,Object> surveyResults) throws Exception{ return Lists.newArrayList(); }
	public List<String> getListOfDrlRules(String surveyId) throws Exception{ return Lists.newArrayList(); }
	
	
	private Map<String, String> configMap;
	protected boolean extraDebug;
	public String getConfig(String key){ return configMap.get(key);}
	public void setConfig(Map<String, String> configMap){ this.configMap=configMap; }
	
	public abstract List<String> getMandatoryConfigs();
	public List<String> getConfigErrors(){
		return getMandatoryConfigs().stream().filter(e -> !configMap.containsKey(e)).map(e -> {return String.format("Missing config '%s' for RecommendationsPlugin executor '%s'",e,this.getClass().getSimpleName());}).collect(Collectors.toList());
	}

}
