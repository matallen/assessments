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
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class AddTitleAndScorePluginTest{

	@BeforeEach
	public void init() throws FileNotFoundException, IOException{
		Database.STORAGE="target/test/"+Database.STORAGE;
		if (new File(Database.STORAGE).exists())
			new File(Database.STORAGE).delete();
		Database.get();
		
//		IOUtils.write(setupDatabase().getBytes(), new FileOutputStream(new File(Database.STORAGE)));
//		Survey s=Survey.findById("TESTING");
//		s.setQuestions(setupQuestions());
	}

	
	@Test
	public void test1() throws Exception{
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("QuestionTitlePlugin_test1_questions.json"), "UTF-8");
//		String answersJson="{\n" + 
//				"  \"platforms_q3\": {\n" + 
//				"    \"answers\": [\n" + 
//				"      {\n" + 
//				"        \"answer\": \"20#Agile\"\n" + 
//				"      }\n" + 
//				"    ],\n" + 
//				"    \"title\": \"Which of the following practices/methodologies are implemented in your organization?\"\n" + 
//				"  },\n" + 
//				"  \"platforms_q2\": {\n" + 
//				"    \"answer\": \"20#21-50\",\n" + 
//				"    \"title\": \"What is the size of your department?\"\n" + 
//				"  },\n" + 
//				"  \"FirstName\": {\n" + 
//				"    \"answer\": \"Mat\",\n" + 
//				"    \"title\": \"First Name\"\n" + 
//				"  },\n" + 
//				"  \"WorkEmail\": {\n" + 
//				"    \"answer\": \"mallen@redhat.com\",\n" + 
//				"    \"title\": \"Work Email\"\n" + 
//				"  },\n" + 
//				"  \"interests\": {\n" + 
//				"    \"answers\": [\n" + 
//				"      {\n" + 
//				"        \"answer\": \"platforms\"\n" + 
//				"      }\n" + 
//				"    ],\n" + 
//				"    \"title\": \"interests\"\n" + 
//				"  },\n" + 
//				"  \"platforms_q1\": {\n" + 
//				"    \"answer\": \"20#software\",\n" + 
//				"    \"title\": \"What is your department / organizations main responsability?\"\n" + 
//				"  }\n" + 
//				"}";
		String answersJson="{\n" + 
				"  \"interests\": [\n" + 
				"    \"platforms\"\n" + 
				"  ],\n" + 
				"  \"platforms_q1\": \"20#software\",\n" + 
				"  \"platforms_q2\": \"20#21-50\",\n" + 
				"  \"platforms_q3\": [\n" + 
				"    \"20#Agile\"\n" + 
				"  ],\n" +
				"  \"FirstName\": \"Mat\"\n" +
				"}";

		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestions(questionsJson);
		s.persist();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=new AddTitleAndScorePlugin().execute("test1", "TEST_VISITOR_ID", answers);
		
		System.out.println("to:"+Json.toJson(newData));
		
	}
}
