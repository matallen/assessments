package com.redhat.services.ae.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.plugins.IndustryStatisticsPlugin;

public class SurveyTest{

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
		
		Survey s=Survey.builder()
		.id("12345")
		.name("My Test Survey")
		.theme("rti-theme")
		.build();
		
		// create test data for metrics
		s.getMetrics().put("testmetric1", 1);
		s.getResults().put("resultsID", "{'some json':true}");
		s.setQuestionsAsString("{\"Surname\":\"Bloggs\"}");
		
		s.save();
		
		s.saveResults();
		s.saveMetrics();
		s.saveQuestions();
		
//		IndustryStatisticsPlugin p=new IndustryStatisticsPlugin();
//		p.setConfig(null);
//		Map<String,Object> surveyResults=new MapBuilder<String,Object>().build();
//		surveyResults.put("_Industry", "Agriculture");
//		surveyResults.put("_sectionScore", new MapBuilder<String,Integer>()
//				.put("Modernizing Platforms", 50)
//				.build());
//		p.execute(s, "visitorId", surveyResults);
		
		
	}
}
