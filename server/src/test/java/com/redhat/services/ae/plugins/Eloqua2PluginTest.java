package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mvel2.MVEL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.ReportsController;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;


/**
 * Can only be run after plugins AddTitleAndScore & EmbeddedScoreTotal 
 */
public class Eloqua2PluginTest{

	
	@Test
	public void xTest(){
		Map<String,Object> vars=new MapBuilder<String,Object>()
				.put("abc", "mallen@redhat.com")
				.build();
		System.out.println(MVEL.eval("abc contains \"@redhat.com\"", vars)); 
	}
	
	@Test
	public void testEloquaWithFields() throws Exception{
		
		String questionsJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_1_questions.json"), "UTF-8");
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_1_answers.json"), "UTF-8");
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		
		String surveyId="1";
		String visitorId="1";
		Survey s=Survey.builder().id(surveyId).name("Test Survey").build();
		s.setQuestionsAsString(questionsJson);
		s.persist();

		// execute this plugin because it changes the structure of the answers
		System.out.println("AddTitleAndScorePlugin:: From:\n"+Json.toJson(answers));
		answers=new AddTitleAndScorePlugin().execute(surveyId, visitorId, answers);
		System.out.println("AddTitleAndScorePlugin:: To:\n"+Json.toJson(answers));
		
		answers=new EmbeddedScoreTotalPlugin().execute(surveyId, visitorId, answers);
		System.out.println("EmbeddedScoreTotalPlugin:: To:\n"+Json.toJson(answers));

		
		
		Eloqua2Plugin p=new Eloqua2Plugin(){
			@Override public void sendToEloqua(String url, boolean disabled, Map<String,String> eloquaData){
				Assert.assertTrue(disabled);
				Assert.assertEquals(true, eloquaData.size()>0);
				Assert.assertEquals("qwerty Bloggs", eloquaData.get("TestSubstitution"));
			}
		};
		
		
		p.setConfig(getConfig());
		p.disabled=false; // the @redhat.com email in the answers should disable the plugin
		answers=p.execute(surveyId, visitorId, answers);
		System.out.println("Eloqua2Plugin:: To:\n"+Json.toJson(answers));
		
	}
	
	/**
	 * TO DEBUG, GO TO https://tyrion-workbenchprod.int.open.paas.redhat.com/ and search for 8091 (elqFormName in database.json config)
	 * 
	 * Here is the link to the mojo space that explains Tyrion and how you can use it:
	 * https://mojo.redhat.com/videos/932000
	 * 
	 * Sandbox Consulting Assessment Integration form id = 8091 (use this id once you start testing) 
   * Ready to Innovate Offer id (to see how the data currently looks in Eloqua with the active assessment) - 70160000000xZtmAAE
	 *
	 * Example field mapping: https://docs.google.com/spreadsheets/d/1ZQ4sUfvW2Ukfv0D43NoylKVjm_qyBYw50uwLLgfd8Yw/edit?usp=sharing
	 * 
	 */
	
	private Map<String, Object> getConfig() throws JsonParseException, JsonMappingException, IOException{
		return
				new MapBuilder<String, Object>()
				.put("active", true)
				.put("className", "com.redhat.services.ae.plugins.EloquaPlugin")
//				.put("url", "https://s1795.t.eloqua.com/e/f2?elqSiteID=1795&elqFormName=consulting-assessment-integration-sandbox")
				.put("url", "https://s1795.t.eloqua.com/e/f2")
				.put("disabled", "true")
				.put("disabledIf","WorkEmail contains @redhat.com")
				.put("config", new MapBuilder<String,Map<String,String>>()
						.put("mapping", new MapBuilder<String,String>()
								.put("platforms_q1",  "UDF_01")
								.put("platforms_q2",  "UDF_02")
								.put("platforms_q3", "UDF_03")
								.put("FirstName", "C_FirstName")
								.put("LastName", "C_LastName")
								.put("Company", "C_Company")
								.build())
						.put("values", new MapBuilder<String,String>()
								.put("elqSiteID",  "1795")
								.put("elqFormName",  "consulting-assessment-integration-sandbox")
								.put("A_OfferID",  "70160000000xZtmAAE")
								.put("elqCustomerGUID",  "60e60dD55c-aec7-4b45-a936-d621ec1e8a6c")
								.put("TestSubstitution","${FirstName} ${LastName}")
								.build()
								)
						.build())
				.build();
	}
}
