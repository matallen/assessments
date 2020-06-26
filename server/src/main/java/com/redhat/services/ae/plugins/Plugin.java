package com.redhat.services.ae.plugins;

import java.util.Map;

public interface Plugin{

	void setConfig(Map<String, Object> config);

	void execute(Map<String, Object> surveyResults);

}
