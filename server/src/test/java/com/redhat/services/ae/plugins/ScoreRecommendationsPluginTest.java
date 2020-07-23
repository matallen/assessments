package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class ScoreRecommendationsPluginTest{

	
	@BeforeEach
	public void init() throws FileNotFoundException, IOException{
		Database.STORAGE="target/test/"+Database.STORAGE;
		if (new File(Database.STORAGE).exists())
			new File(Database.STORAGE).delete();
		Database.get();
	}

	
	@Test
	public void test1() throws Exception{
		
//		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("QuestionTitlePlugin_test1_questions.json"), "UTF-8");
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_1_questions.json"), "UTF-8");
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_1_answers.json"), "UTF-8");
//		String answersJson="{\n" + 
//				"  \"interests\": [\n" + 
//				"    \"platforms\"\n" + 
//				"  ],\n" + 
//				"  \"platforms_q1\": \"20#software\",\n" + 
//				"  \"platforms_q2\": \"20#21-50\",\n" + 
//				"  \"platforms_q3\": [\n" + 
//				"    \"20#Agile\"\n" + 
//				"  ],\n" +
//				"  \"FirstName\": \"Mat\"\n" +
//				"}";
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		
		String surveyId="1";
		Survey s=Survey.builder().id(surveyId).name("Test Survey").build();
		s.setQuestions(questionsJson);
		s.persist();
		
		String visitorId="1";
		
		
		
		ScoreRecommendationsPlugin test=new ScoreRecommendationsPlugin();
		test.setConfig(new MapBuilder<String,Object>().put("decisionTableLocation", "https://docs.google.com/spreadsheets/d/19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4").build());
		
		System.out.println("AddTitleAndScorePlugin:: From:\n"+Json.toJson(answers));
		answers=new AddTitleAndScorePlugin().execute(surveyId, visitorId, answers);
		System.out.println("AddTitleAndScorePlugin:: To:\n"+Json.toJson(answers));

//		System.out.println("EmbeddedScoreTotalPlugin:: From:\n"+Json.toJson(answers));
		answers=new EmbeddedScoreTotalPlugin().execute(surveyId, visitorId, answers);
		System.out.println("EmbeddedScoreTotalPlugin:: To:\n"+Json.toJson(answers));

//		System.out.println("ScoreRecommendationsPlugin:: From:\n"+Json.toJson(answers));
		answers=test.execute(surveyId, visitorId, answers);
		System.out.println("ScoreRecommendationsPlugin:: To:\n"+Json.toJson(answers));
		
//		System.out.println("To:\n"+Json.toJson(answers));
		
	}
}
