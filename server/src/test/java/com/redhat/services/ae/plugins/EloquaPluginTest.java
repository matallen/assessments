package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

public class EloquaPluginTest{

	
	@Test
	public void testEloquaWithFields() throws ParseException, IOException{
		EloquaPlugin p=new EloquaPlugin();
		p.setConfig(getConfig());
		p.execute(getSurveyResults1());
		
//		System.out.println(
//			new ReportsController().getSurveyCount("TESTING", "2020-Jan", "2020-Apr").getEntity()
//		);
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
	 * Exalple field mapping: https://docs.google.com/spreadsheets/d/1ZQ4sUfvW2Ukfv0D43NoylKVjm_qyBYw50uwLLgfd8Yw/edit?usp=sharing
	 * 
	 */
	
	private Map<String, Object> getConfig() throws JsonParseException, JsonMappingException, IOException{
		return
				new MapBuilder<String, Object>()
				.put("active", true)
				.put("className", "com.redhat.services.ae.plugins.EloquaPlugin")
//				.put("url", "https://s1795.t.eloqua.com/e/f2?elqSiteID=1795&elqFormName=consulting-assessment-integration-sandbox")
				.put("url", "https://s1795.t.eloqua.com/e/f2")
				.put("config", new MapBuilder<String,Map<String,String>>()
						.put("mapping", new MapBuilder<String,String>()
								.put("automation-dev",  "UDF_01_Answer")
								.put("automation-ops",  "UDF_02_Answer")
								.put("methodology-dev", "UDF_03_Answer")
								.put("firstName", "C_FirstName")
								.put("lastName", "C_LastName")
								.put("companyName", "C_Company")
								.build())
						.put("values", new MapBuilder<String,String>()
								.put("elqSiteID",  "1795")
								.put("elqFormName",  "consulting-assessment-integration-sandbox")
								.put("A_OfferID",  "70160000000xZtmAAE")
								.put("elqCustomerGUID",  "60e60dD55c-aec7-4b45-a936-d621ec1e8a6c")
								.put("C_Salutation",  "Mr")
								.put("C_EmailAddress",  "mallen@redhat.com")
								.build()
								)
						.build())
				.build();
	}
	private Map<String, Object> getSurveyResults1() throws JsonParseException, JsonMappingException, IOException{
		return 
				new MapBuilder<String,Object>()
				.put("automation-dev", "answer 1")
				.put("automation-ops", "answer 2")
				.put("firstName", "Fred")
				.put("lastName", "Bloggs")
				.put("companyName", "Red Hat Inc")
				.build()
				;
	}
	private Map<String, Object> getSurveyResults2() throws JsonParseException, JsonMappingException, IOException{
		return 
				new MapBuilder<String,Object>()
				.put("interests", Lists.newArrayList("Clouds","AppDev"))
				.put("automation-dev", "answer 1")
				.put("automation-ops", "answer 2")
				.put("firstName", "Fred")
				.put("lastName", "Bloggs")
				.put("companyName", "Red Hat Inc")
				.build()
				;
	}
}
