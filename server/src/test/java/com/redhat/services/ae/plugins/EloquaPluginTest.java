package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	
	private Map<String, Object> getConfig() throws JsonParseException, JsonMappingException, IOException{
		
		return
				new MapBuilder<String, Object>()
				.put("active", true)
				.put("className", "com.redhat.services.ae.plugins.EloquaPlugin")
				.put("url", "https://s1795.t.eloqua.com/e/f2?elqSiteID=1795&elqFormName=consulting-assessment-integration-sandbox")
				.put("config", new MapBuilder<String,Map<String,String>>()
						.put("mapping", new MapBuilder<String,String>()
								.put("automation-dev",  "UDF_01_Answer")
								.put("automation-ops",  "UDF_02_Answer")
								.put("methodology-dev", "UDF_03_Answer")
								.put("firstName", "C_FirstName")
								.put("lastName", "C_LastName")
								.build())
						.put("values", new MapBuilder<String,String>()
								.put("elqFormName",  "ready-to-innovate")
								.put("elqCustomerGUID",  "60e60dD55c-aec7-4b45-a936-d621ec1e8a6c")
								.put("",  "")
								.put("",  "")
								.put("",  "")
								.put("A_OfferID",  "70160000000xZtrAAE")
								.build()
								)
						.build())
				.build();
		
//		return 
//				Json.toObject(
//						"{    \"active\" : true,\n" + 
//						"    \"className\" : \"com.redhat.services.ae.plugins.EloquaPlugin\",\n" + 
//						"    \"url\" : \"https://s1795.t.eloqua.com/e/f2?elqSiteID=1795&elqFormName=consulting-assessment-integration-sandbox\",\n" + 
//						"    \"config\" : {\n" + 
//						"      \"mapping\" : {\n" + 
//						"        \"automation-dev\" : \"UDF_01_Answer\",\n" + 
//						"        \"automation-ops\" : \"UDF_02_Answer\",\n" + 
//						"        \"methodology-dev\" : \"UDF_03_Answer\"\n" + 
//						"      },\n" + 
//						"      \"values\" : {\n" + 
//						"        \"UDF_Custom1\" : \"Test\",\n" + 
//						"        \"UDF_Custom2\" : \"Test2\"\n" + 
//						"      }\n" + 
//						"}    }\n"
//						, new TypeReference<HashMap<String, Object>>() {});
	}
	private Map<String, String> getSurveyResults1() throws JsonParseException, JsonMappingException, IOException{
		return 
				new MapBuilder<String,String>()
				.put("automation-dev", "answer 1")
				.put("automation-ops", "answer 2")
				.put("firstName", "Fred")
				.put("lastName", "Bloggs")
				.build()
				;
	}
}
