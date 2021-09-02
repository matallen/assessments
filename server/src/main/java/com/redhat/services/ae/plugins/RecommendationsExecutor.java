package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.services.ae.recommendations.domain.Recommendation;

public abstract class RecommendationsExecutor{
	public abstract List<Recommendation> execute(String surveyId, Map<String,Object> surveyResults) throws Exception;
	public abstract List<String> getMandatoryConfigs();
	
	private Map<String, String> configMap;
	protected boolean extraDebug;
	public String getConfig(String key){ return configMap.get(key);}
	public void setConfig(Map<String, String> configMap){ this.configMap=configMap; }
	
	public List<String> getConfigErrors(){
		return getMandatoryConfigs().stream().filter(e -> !configMap.containsKey(e)).map(e -> {return String.format("Missing config '%s' for RecommendationsPlugin executor '%s'",e,this.getClass().getSimpleName());}).collect(Collectors.toList());
	}

}
