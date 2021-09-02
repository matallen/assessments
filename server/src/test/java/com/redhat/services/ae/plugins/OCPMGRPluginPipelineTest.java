package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.PluginPipelineExecutor;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

import io.quarkus.test.junit.QuarkusTest;

public class OCPMGRPluginPipelineTest{
	private static String testResourceKey="ocpmgr-";
	
	
	@Test
	public void test() throws Exception{
		
		String pluginsJson=IOUtils.toString(this.getClass().getClassLoader().getResource(testResourceKey+"test_1_plugins.json"), "UTF-8");
		Map<String, Map<String, Object>> plugins=Json.newObjectMapper(true).readValue(pluginsJson, new TypeReference<Map<String,Map<String,Object>>>(){});
		String surveyResultsJson=IOUtils.toString(this.getClass().getClassLoader().getResource(testResourceKey+"test_1_answers.json"), "UTF-8");
		Map<String,Object> surveyResults=Json.toObject(surveyResultsJson, new TypeReference<HashMap<String,Object>>(){});
		
		String surveyId="OCP1";
		Survey s=Survey.builder().id(surveyId).name(surveyId).build();
		s.getPlugins().putAll(plugins);
		s.persist();
		
		
		PluginPipelineExecutor pluginExecutor=new PluginPipelineExecutor();
		List<Plugin> activePlugins=pluginExecutor.getActivePlugins(s, surveyResults);
		pluginExecutor.execute(s, surveyResults, "visitorId", activePlugins);

		
//		for(Plugin p:activePlugins){
//			System.out.println(p.getClass().getSimpleName()+":: From:\n"+Json.toJson(surveyResults));
//			surveyResults=p.execute(surveyId, "visitorId", surveyResults);
//			System.out.println(p.getClass().getSimpleName()+":: To:\n"+Json.toJson(surveyResults));
//		}
		
	}
}
