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
import com.redhat.services.ae.controllers.TestBase;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

import org.junit.Assert;

public class SectionScorePluginTest extends TestBase{

	@BeforeEach
	public void init() throws FileNotFoundException, IOException{
//		Database.STORAGE="target/test/"+Database.STORAGE;
//		if (new File(Database.STORAGE).exists())
//			new File(Database.STORAGE).delete();
//		Database.get();
		
//		IOUtils.write(setupDatabase().getBytes(), new FileOutputStream(new File(Database.STORAGE)));
//		Survey s=Survey.findById("TESTING");
//		s.setQuestions(setupQuestions());
	}

	private String getQuestions(){
		return
		"{                                                                                 "+
		" \"pages\": [                                                                     "+
		"  {                                                                               "+
		"   \"name\": \"page1\",                                                           "+
		"   \"elements\": [                                                                "+
		"    {                                                                             "+
		"     \"type\": \"radiogroup\",                                                    "+
		"     \"name\": \"q1\",                                                            "+
		"     \"title\": \"What is your department / organizations main responsability?\", "+
		"     \"choices\": [                                                               "+
		"      {                                                                           "+
		"       \"value\": \"10#infra\",                                                   "+
		"       \"text\": \"Infrastructure delivery\",                                     "+
		"       \"score\": \"1\"                                                           "+
		"      },                                                                          "+
		"      {                                                                           "+
		"       \"value\": \"20#software\",                                                "+
		"       \"text\": \"Software Engineering\",                                        "+
		"       \"score\": \"2\"                                                           "+
		"      }                                                                           "+
		"     ]                                                                            "+
		"    },                                                                            "+
		"    {                                                                             "+
		"     \"type\": \"radiogroup\",                                                    "+
		"     \"name\": \"q2\",                                                            "+
		"     \"title\": \"Question 2\",                                                   "+
		"     \"choices\": [                                                               "+
		"      {                                                                           "+
		"       \"value\": \"15#answer1\",                                                 "+
		"       \"text\": \"Answer 1\",                                                    "+
		"       \"score\": \"1\"                                                           "+
		"      },                                                                          "+
		"      {                                                                           "+
		"       \"value\": \"20#software\",                                                "+
		"       \"text\": \"Answer 2\",                                                    "+
		"       \"score\": \"2\"                                                           "+
		"      }                                                                           "+
		"     ]                                                                            "+
		"    }                                                                             "+
		"   ]}                                                                             "+
		"]}                                                                                "+
		"";
	}
	
	@Test
	public void testAverage() throws Exception{

		String answersJson=                                                                    
		"{                                                                                 "+
		" \"q1\": \"10#infra\",                                                            "+
		" \"q2\": \"15#answer1\"                                                           "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(getQuestions());
		s.saveQuestions();
		s.save();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		answers=new AddTitleAndScorePlugin().execute("test1", "TEST_VISITOR_ID", answers);
		
		
		SectionScorePlugin test=new SectionScorePlugin();
		test.setConfig(new MapBuilder<String,Object>()
				.put("scoreStrategy", "average")
				.build());
		answers=test.execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(answers));
		
		
		Assert.assertEquals(12, ((Map)answers.get("_sectionScore")).get("page1"));
		
	}
	
	@Test
	public void testSumTotal() throws Exception{

		String answersJson=                                                                    
		"{                                                                                 "+
		" \"q1\": \"10#infra\",                                                            "+
		" \"q2\": \"15#answer1\"                                                           "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(getQuestions());
		s.saveQuestions();
		s.save();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		answers=new AddTitleAndScorePlugin().execute("test1", "TEST_VISITOR_ID", answers);
		
		
		SectionScorePlugin test=new SectionScorePlugin();
		test.setConfig(new MapBuilder<String,Object>()
				.put("scoreStrategy", "sum")
				.build());
		answers=test.execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(answers));
		
		
		Assert.assertEquals(25, ((Map)answers.get("_sectionScore")).get("page1"));
		
	}
	
	
	
	
}
