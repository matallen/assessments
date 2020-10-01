package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.TestBase;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class DroolsScoreRecommendationsPluginTest extends TestBase{

	
	@BeforeEach
	public void init() throws FileNotFoundException, IOException{
//		Database.STORAGE="target/test/"+Database.STORAGE;
//		if (new File(Database.STORAGE).exists())
//			new File(Database.STORAGE).delete();
//		Database.get();
	}

	
	@Test
	public void testAllRTIPlugins() throws Exception{
		
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_1_questions.json"), "UTF-8");
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_1_answers.json"), "UTF-8");
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		
		String surveyId="1";
		Survey s=Survey.builder().id(surveyId).name("Test Survey").build();
		s.setQuestionsAsString(questionsJson);
		s.persist();
		s.saveQuestions();
		
		String visitorId="1";
		
		
		System.out.println("AddTitleAndScorePlugin:: From:\n"+Json.toJson(answers));
		answers=new AddTitleAndScorePlugin().execute(surveyId, visitorId, answers);
		System.out.println("AddTitleAndScorePlugin:: To:\n"+Json.toJson(answers));

//		System.out.println("EmbeddedScoreTotalPlugin:: From:\n"+Json.toJson(answers));
		answers=new SectionScorePlugin().execute(surveyId, visitorId, answers);
		System.out.println("EmbeddedScoreTotalPlugin:: To:\n"+Json.toJson(answers));

//		answers=new Eloqua2Plugin().execute(surveyId, visitorId, answers);
//		System.out.println("Eloqua2Plugin:: To:\n"+Json.toJson(answers));
//		
//		System.out.println("ScoreRecommendationsPlugin:: From:\n"+Json.toJson(answers));
		DroolsScoreRecommendationsPlugin test=new DroolsScoreRecommendationsPlugin();
		test.setConfig(new MapBuilder<String,Object>()
				.put("decisionTableLocation", "https://docs.google.com/spreadsheets/d/19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4")
				.put("configSheetName", "Config")
				.put("thresholdSection", "Report Thresholds")
				.build());
		answers=test.execute(surveyId, visitorId, answers);
		System.out.println("ScoreRecommendationsPlugin:: To:\n"+Json.toJson(answers));
		
//		System.out.println("To:\n"+Json.toJson(answers));
		
		
		Assert.assertEquals(true, answers.containsKey("_report"));
		Assert.assertEquals(true, Map.class.isAssignableFrom(answers.get("_report").getClass()));
		Assert.assertEquals(true, ((Map)answers.get("_report")).size()>0);
		
		
		Assert.assertEquals(17, ((Map)answers.get("_sectionScore")).get("Modernizing Platforms"));
		
	}
}
