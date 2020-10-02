package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.TestBase;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class AddTitleAndScorePluginTest extends TestBase{

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
	
	private String getQuestionsJson(){
		return 
				"{                                                                                 "+
						" \"pages\": [                                                                     "+
						"  {                                                                               "+
						"   \"name\": \"page1\",                                                           "+
						"   \"elements\": [                                                                "+
						"    {                                                                             "+
						"     \"type\": \"checkbox\",                                                      "+
						"     \"name\": \"q2\",                                                            "+
						"     \"title\": \"this is a checkbox question\",                                  "+
						"     \"choices\": [                                                               "+
						"      {                                                                           "+
						"       \"value\": \"10#Waterfall\",                                               "+
						"       \"text\": \"Waterfall\"                                                    "+
						"      },                                                                          "+
						"      {                                                                           "+
						"       \"value\": \"20#Agile\",                                                   "+
						"       \"text\": \"Agile\"                                                        "+
						"      },                                                                          "+
						"      {                                                                           "+
						"       \"value\": \"30#DevOps\",                                                  "+
						"       \"text\": \"DevOps or DevSecOps\"                                          "+
						"      }                                                                           "+
						"     ]                                                                            "+
						"    },                                                                            "+
						"    {                                                                             "+
						"     \"type\": \"radiogroup\",                                                    "+
						"     \"name\": \"q1\",                                                            "+
						"     \"title\": \"What is your department / organizations main responsability?\", "+
						"     \"choices\": [                                                               "+
						"      {                                                                           "+
						"       \"value\": \"10#infra\",                                                   "+
						"       \"text\": \"Infrastructure delivery\"                                      "+
						"      },                                                                          "+
						"      {                                                                           "+
						"       \"value\": \"20#software\",                                                "+
						"       \"text\": \"Software Engineering\"                                         "+
						"      },                                                                          "+
						"      {                                                                           "+
						"       \"value\": \"30#Operations\",                                              "+
						"       \"text\": \"Operations and software/infrastructure  support\"              "+
						"      }                                                                           "+
						"     ]                                                                            "+
						"    }                                                                             "+
						"   ]}                                                                             "+
						"]}                                                                                "+
						"";
	}
	private String getQuestionsJsonScore(){
		return 
				"{                                                                                 "+
				" \"pages\": [                                                                     "+
				"  {                                                                               "+
				"   \"name\": \"page1\",                                                           "+
				"   \"elements\": [                                                                "+
				"    {                                                                             "+
				"     \"type\": \"checkbox\",                                                      "+
				"     \"name\": \"q2\",                                                            "+
				"     \"title\": \"this is a checkbox question\",                                  "+
				"     \"choices\": [                                                               "+
				"      {                                                                           "+
				"       \"value\": \"Waterfall\",                                                  "+
				"       \"text\": \"Waterfall\",                                                   "+
				"       \"score\": \"10\"                                                          "+
				"      },                                                                          "+
				"      {                                                                           "+
				"       \"value\": \"Agile\",                                                      "+
				"       \"text\": \"Agile\",                                                       "+
				"       \"score\": \"20\"                                                          "+
				"      },                                                                          "+
				"      {                                                                           "+
				"       \"value\": \"DevOps\",                                                     "+
				"       \"text\": \"DevOps or DevSecOps\",                                         "+
				"       \"score\": \"30\"                                                          "+
				"      }                                                                           "+
				"     ]                                                                            "+
				"    },                                                                            "+
				"    {                                                                             "+
				"     \"type\": \"radiogroup\",                                                    "+
				"     \"name\": \"q1\",                                                            "+
				"     \"title\": \"What is your department / organizations main responsability?\", "+
				"     \"choices\": [                                                               "+
				"      {                                                                           "+
				"       \"value\": \"infra\",                                                      "+
				"       \"text\": \"Infrastructure delivery\",                                     "+
				"       \"score\": \"10\"                                                          "+
				"      },                                                                          "+
				"      {                                                                           "+
				"       \"value\": \"software\",                                                   "+
				"       \"text\": \"Software Engineering\",                                        "+
				"       \"score\": \"20\"                                                          "+
				"      },                                                                          "+
				"      {                                                                           "+
				"       \"value\": \"Operations\",                                                 "+
				"       \"text\": \"Operations and software/infrastructure  support\",             "+
				"       \"score\": \"30\"                                                          "+
				"      }                                                                           "+
				"     ]                                                                            "+
				"    }                                                                             "+
				"   ]}                                                                             "+
				"]}                                                                                "+
				"";
	}
	private Object readAnswer(Map<String,Object> answers,String question, String field){
		Map<String,Object> answer=(Map<String,Object>)answers.get(question);
		return (Object)answer.get(field);
	}
	
	@Test
	public void testHighestStrategy() throws Exception{
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "highest")
				.build();

		
		String answersJson=                                                                    
		"{                                                                                 "+
		" \"q2\": [\"10#Waterfall\",\"20#Agile\"],                                         "+
		" \"q1\": \"10#infra\"                                                             "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(getQuestionsJson());
		s.saveQuestions();
		s.save();
		
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().setConfig(config).execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
		
		Assert.assertEquals(10, readAnswer(newData, "q1", "score"));
		Assert.assertEquals("page1", readAnswer(newData, "q1", "pageId"));
		Assert.assertEquals("What is your department / organizations main responsability?", readAnswer(newData, "q1", "title"));
		
		Assert.assertEquals(20, readAnswer(newData, "q2", "score"));
		Assert.assertEquals("page1", readAnswer(newData, "q2", "pageId"));
		Assert.assertEquals("this is a checkbox question", readAnswer(newData, "q2", "title"));
		
	}
	
	@Test
	public void testUsingScorePropertyInsteadOfEmbeddedHashScore() throws Exception{
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "highest")
				.build();
		
		String answersJson=                                                                    
		"{                                                                                 "+
		" \"q2\": [\"Waterfall\",\"Agile\"],                                               "+
		" \"q1\": \"infra\"                                                                "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(getQuestionsJsonScore());
		s.saveQuestions();
		s.save();
		
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().setConfig(config).execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
		
		Assert.assertEquals(10, readAnswer(newData, "q1", "score"));
		Assert.assertEquals("page1", readAnswer(newData, "q1", "pageId"));
		Assert.assertEquals("What is your department / organizations main responsability?", readAnswer(newData, "q1", "title"));
		
		Assert.assertEquals(20, readAnswer(newData, "q2", "score"));
		Assert.assertEquals("page1", readAnswer(newData, "q2", "pageId"));
		Assert.assertEquals("this is a checkbox question", readAnswer(newData, "q2", "title"));
	}
	
	@Test
	public void testSumStrategy() throws Exception{
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "sum")
				.build();
		
		String answersJson=                                                                    
		"{                                                                                 "+
		" \"q2\": [\"10#Waterfall\",\"20#Agile\"],                                         "+
		" \"q1\": \"10#infra\"                                                             "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(getQuestionsJson());
		s.saveQuestions();
		s.save();
		
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().setConfig(config).execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
		
		Assert.assertEquals(10, readAnswer(newData, "q1", "score"));
		Assert.assertEquals("page1", readAnswer(newData, "q1", "pageId"));
		Assert.assertEquals("What is your department / organizations main responsability?", readAnswer(newData, "q1", "title"));
		
		Assert.assertEquals(30, readAnswer(newData, "q2", "score"));
		Assert.assertEquals("page1", readAnswer(newData, "q2", "pageId"));
		Assert.assertEquals("this is a checkbox question", readAnswer(newData, "q2", "title"));
		
	}
	
	
	
}
