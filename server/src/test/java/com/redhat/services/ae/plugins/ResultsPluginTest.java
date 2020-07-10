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

public class ResultsPluginTest{

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
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("ResultsPlugin_test1_questions.json"), "UTF-8");
		String answersJson="{\"q_cloud_current_culture_process\":[\"10#Siloed process\",\"30#DevOps team created to change processes\"],\"overallScore\":21,\"interests\":[\"cloud\"],\"contactForm\":{\"FirstName\":\"aaa\",\"LastName\":\"aaa\",\"Email\":\"test@redhat.com\"}}";

		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestions(questionsJson);
		s.persist();
		
		ResultsPlugin test=new ResultsPlugin();
		
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		System.out.println("from:"+Json.toJson(answers));
		Map<String, Object> newData=test.execute("test1", "TEST_VISITOR_ID", answers);
		
		System.out.println("to:"+Json.toJson(newData));
		
	}
}
