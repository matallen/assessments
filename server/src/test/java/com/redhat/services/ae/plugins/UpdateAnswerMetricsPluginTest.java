package com.redhat.services.ae.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.api.client.util.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.TestBase;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class UpdateAnswerMetricsPluginTest extends TestBase{

	@BeforeEach
	public void init() throws FileNotFoundException, IOException{
	}
	
	
	@SuppressWarnings({"unchecked", "unused"})
	@Test
	public void testTryingToTestMetricsWithNullKey() throws Exception{
		String surveyId="TestOther1";
		
		String answersJson=  IOUtils.toString(this.getClass().getClassLoader().getResource("updateanswermetrics-questions-1.json"), "UTF-8");
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("addtitlescore-rta-questions.json"), "UTF-8");
		
		Survey s=createSurvey(surveyId, questionsJson);
		s.clearMetrics();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		
		Assert.assertTrue(((String)answers.get("q_automation_2")).contains("Technology decisions are sometimes made solely within the department"));
		
		answers=new Pipeline()
		.add(new UpdateAnswerMetricsPlugin())
		.execute(surveyId, answers);
		
		System.out.println(Json.toJson(s.getMetrics()));
		
		Map<String,Object> ad=(Map<String,Object>)s.getMetrics().get("answerDistribution");
		Map<String,Object> thisMonth=(Map<String,Object>)ad.get(new SimpleDateFormat("YY-MMM").format(new Date()));
		
		// would be better to parse the answers and loop the metrics, but that's a lot of complex testing code, so here's the abridged version
		Map<String,Integer> q2=(Map<String,Integer>)thisMonth.get("q_automation_2");
		Assert.assertTrue(q2.get("Technology decisions are sometimes made solely within the department")==1);
		
	}
	

	
	// ###########################
	// ##### utility methods #####
	// ###########################
	
	private class Pipeline{
		List<Plugin> pipeline=Lists.newArrayList();
		public Pipeline add(Plugin p){pipeline.add(p); return this;}
		public Map<String,Object> execute(String surveyId, Map<String,Object> answers) throws Exception{
			System.out.println("Before pipeline:"+Json.toJson(answers));
			for(Plugin p:pipeline){
				answers=p.execute(surveyId, "VISITOR_ID", answers);
				System.out.println("After["+p.getClass().getSimpleName()+"]:"+Json.toJson(answers));
			}
			return answers;
		}
	}
	
	private Survey createSurvey(String idName, String questionsJson) throws JsonParseException, JsonMappingException, IOException{
		Survey s=Survey.builder().id(idName).name(idName).build();
		s.setQuestionsAsString(questionsJson);
		s.saveQuestions();
		s.save();
		return s;
	}
	
	
}
