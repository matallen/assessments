package com.redhat.services.ae.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.TestBase;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class RevertAnswersPluginTest extends TestBase{
	
	@Test
	public void testRevertAnswersButRetainSome() throws Exception{
		
		Survey s=Survey.builder().id("test1").name("Test Survey").build();
		s.setQuestionsAsString(dummyQuestions());
		s.saveQuestions();
		s.persist();

		
		Map<String, Object> newAnswers=dummyAnswers();
		newAnswers=new AddTitleAndScorePlugin().execute("test1", "TEST_VISITOR_ID", newAnswers);
		newAnswers=new EmbeddedScoreTotalPlugin().execute("test1", "TEST_VISITOR_ID", newAnswers);
		
		
		RevertAnswersPlugin p=new RevertAnswersPlugin();
		p.setConfig(new MapBuilder<String,Object>()
				.put("fieldsToRetainRegEx", "(_sectionScore|_timestamp|_report)")
				.build());
		p.setOriginalSurveyResults(dummyAnswers());
		
		System.out.println("before:"+Json.toJson(newAnswers));
		newAnswers=p.execute("test1", "TEST_VISITOR_ID", newAnswers);
		System.out.println("after:"+Json.toJson(newAnswers));
		
		Assert.assertTrue(newAnswers.containsKey("interests") && List.class.isAssignableFrom(newAnswers.get("interests").getClass()));
		Assert.assertTrue(newAnswers.containsKey("platforms_q1") && String.class.isAssignableFrom(newAnswers.get("platforms_q1").getClass()));
		Assert.assertTrue(newAnswers.containsKey("_sectionScore"));
		
	}
	
	private String dummyQuestions() throws IOException{
		return IOUtils.toString(this.getClass().getClassLoader().getResource("QuestionTitlePlugin_test1_questions.json"), "UTF-8");
	}
	
	private Map<String,Object> dummyAnswers(){
		return new MapBuilder<String,Object>()
				.put("interests", Lists.newArrayList("platforms"))
				.put("platforms_q1", "20#software")
				.put("platforms_q2", "20#21-50")
				.put("platforms_q3", Lists.newArrayList("20#Agile"))
				.put("FirstName", "Fred")
				.build();
	}
	
}
