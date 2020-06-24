package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.services.ae.controllers.SurveyAdminController;
import com.redhat.services.ae.utils.Json;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

// https://integrate.com/static/Home2/1.6.78.2/documents/marketing/Integrate_Eloqua_Implementation_Guide.pdf

public class EloquaPlugin implements Plugin{
	public static final Logger log=LoggerFactory.getLogger(EloquaPlugin.class);
	private String url;
	private Map<String,String> mapping;
	private Map<String,String> values;
	
	
	@SuppressWarnings({"unchecked", "unused"})
	@Override
	public void setConfig(Map<String, Object> cfg){
		url=(String)cfg.get("url");
		Map<String,Object> config=(Map<String,Object>)cfg.get("config");
		mapping=(Map<String,String>)config.get("mapping");
		values=(Map<String,String>)config.get("values");
		
		if (null==url || null==mapping)
			throw new RuntimeException(String.format("Config not set correctly. Url is %s, and config==null is %s", url, (config==null)));
	}

	@SuppressWarnings("unused")
	@Override
	public void execute(Map<String, String> surveyResults){
		
		// gather the data from surveyResults, map it for the Eloqua http post request, build and send it
		
		Map<String,String> eloquaFields=new HashMap<String, String>();
		
		for(Entry<String, String> e:mapping.entrySet()){
			eloquaFields.put(e.getValue(), surveyResults.get(e.getKey()));
		}
		
		for(Entry<String, String> e:values.entrySet()){
			eloquaFields.put(e.getKey(), e.getValue());
		}
		
		boolean asJson=false;
		boolean asForm=true;
		
		try{
			
			RequestSpecification rs=given()
			.urlEncodingEnabled(true)
			.contentType(ContentType.JSON)
			.header("Accept", ContentType.JSON.getAcceptHeader())
			;
			
			if (asJson){
				String json=Json.toJson(eloquaFields);
				log.debug(String.format("Sending Eloqua as json:: \n%s", json));
				rs.body(json);
			}
			if (asForm){
				for (Entry<String, String> e:eloquaFields.entrySet()){
					log.debug(String.format("Sending Eloqua as form:: field=%s, value=%s", e.getKey(), e.getValue()));
					rs.param(e.getKey(), e.getValue());
				}
			}
			
				
			Response response=rs.post(url).andReturn();
			
//			Response response=given()
//					.urlEncodingEnabled(true)
//					.contentType(ContentType.JSON)
//					.body(Json.toJson(eloquaFields))
//					.post(url).andReturn();
			
		}catch (JsonProcessingException e1){
			e1.printStackTrace();
		}
		
		
	}
}
