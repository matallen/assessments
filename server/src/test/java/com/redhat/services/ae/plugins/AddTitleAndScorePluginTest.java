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

	@Test
	public void test1() throws Exception{
		
		String questionsJson=
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
		"      },                                                                          "+
		"      {                                                                           "+
		"       \"value\": \"30#Operations\",                                              "+
		"       \"text\": \"Operations and software/infrastructure  support\",             "+
		"       \"score\": \"3\"                                                           "+
		"      }                                                                           "+
		"     ]                                                                            "+
		"    }                                                                             "+
		"   ]}                                                                             "+
		"]}                                                                                "+
		"";
		String answersJson=                                                                    
		"{                                                                                 "+
		" \"q1\": \"10#infra\"                                                             "+
		"}                                                                                 "+
		"";
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(questionsJson);
		s.saveQuestions();
		s.save();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().execute("test1", "TEST_VISITOR_ID", answers);
		System.out.println("to:"+Json.toJson(newData));
	}
	
	
	
	
}
