package com.redhat.services.ae.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
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
					  "     \"type\": \"boolean\",                                                       "+
					  "     \"name\": \"q3\",                                                            "+
					  "     \"title\": \"this is a boolean question?\",                                  "+
					  "     \"valueTrue\": \"40#Yes\",                                                   "+
					  "     \"valueFalse\": \"0#No\"                                                     "+
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
			  "     \"type\": \"boolean\",                                                       "+
			  "     \"name\": \"q3\",                                                            "+
			  "     \"title\": \"this is a boolean question?\",                                  "+
			  "     \"valueTrue\": \"40#Yes\",                                                   "+
			  "     \"valueFalse\": \"0#No\"                                                     "+
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
	public void test1() throws Exception{
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "highest")
				.build();
		
		String answersJson=                                                                    
		"{                                                                                 "+
		" \"Q19-interested_in_learning_more\": \"item1\",                                  "+
		" \"First Name\": \"Mat\"                                                          "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("ocpmigration-1.json"), "UTF-8");
		s.setQuestionsAsString(questionsJson);
		s.saveQuestions();
		s.save();
		
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().setConfig(config).execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
		
//		Assert.assertEquals(10, readAnswer(newData, "q1", "score"));
//		Assert.assertEquals("page1", readAnswer(newData, "q1", "pageId"));
//		Assert.assertEquals("What is your department / organizations main responsability?", readAnswer(newData, "q1", "title"));
//		
//		Assert.assertEquals(20, readAnswer(newData, "q2", "score"));
//		Assert.assertEquals("page1", readAnswer(newData, "q2", "pageId"));
//		Assert.assertEquals("this is a checkbox question", readAnswer(newData, "q2", "title"));
		
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
	
	
	@Test
	public void testOther() throws Exception{
		
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "highest")
				.build();
		
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("addtitlescore-sap-answers.json"), "UTF-8");
		
		
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("sap-test-questions-1.json"), "UTF-8");
		s.setQuestionsAsString(questionsJson);
		s.saveQuestions();
		s.save();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().setConfig(config).execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
		
		// result we want is to strip the "other" answer for now so it doesnt cause exceptions, but doesnt get scored either
		
		Map<String, Object> osInUse=(Map<String, Object>)newData.get("os_in_use");
		List<String> resultAnswers=(List<String>)osInUse.get("answers");
		Assert.assertTrue(resultAnswers.contains("Solaris"));
		Assert.assertTrue(resultAnswers.size()==1);
		
	}
	
	@Test
	public void testRTA() throws Exception{
		
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "highest")
				.build();
		
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("addtitlescore-rta-answers.json"), "UTF-8");
		
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("addtitlescore-rta-questions.json"), "UTF-8");
		s.setQuestionsAsString(questionsJson);
		s.saveQuestions();
		s.save();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().setConfig(config).execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
		
		
	}
	
	
	// Expectation is that "other" answers are not used for scoring, and dont impact the pipeline execution since they cannot be mapped
	@SuppressWarnings({"unchecked", "unused"})
	@Test
	public void testQuestionsWithOther() throws Exception{
		String surveyId="TestOther1";
		
		Map<String,Object> config=new MapBuilder<String,Object>()
				.put("scoreStrategy", "highest")
				.build();
		
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("addtitlescore-sap-answers-2.json"), "UTF-8");
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("addtitlescore-sap-questions-2.json"), "UTF-8");
		
		Survey s=createSurvey(surveyId, questionsJson);
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		
		Assert.assertTrue("other".equals(answers.get("db_in_use")));
		Assert.assertTrue(((List<String>)answers.get("who_manages")).size()==2);
		Assert.assertTrue(((List<String>)answers.get("who_manages")).contains("other"));
		
		answers=new Pipeline()
		.add(new AddTitleAndScorePlugin().setConfig(config))
		.execute(surveyId, answers);
		
		Map<String, Object> checkOnlyOther=(Map<String, Object>)answers.get("db_in_use");
		Assert.assertTrue("other".equals(checkOnlyOther.get("answer")));
//		Assert.assertTrue(((List<String>)checkOnlyOther.get("answers")).size()==0);
//		Assert.assertTrue(((List<String>)checkOnlyOther.get("answers")).contains("other"));
		
		Map<String, Object> checkOtherWithOtherSelection=(Map<String, Object>)answers.get("who_manages");
		Assert.assertTrue(((List<String>)checkOtherWithOtherSelection.get("answers")).size()==1);
//		Assert.assertTrue(((List<String>)checkOtherWithOtherSelection.get("answers")).contains("other"));
		
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
