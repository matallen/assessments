package com.redhat.services.ae.plugins;

import java.util.Map;

public interface Plugin{

	void setConfig(Map<String, Object> config);

	Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception;

}
