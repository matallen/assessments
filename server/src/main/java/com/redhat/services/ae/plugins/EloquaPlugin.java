package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	// gather the data from surveyResults, map it for the Eloqua http post request, build and send it
	@Override
	public void execute(Map<String, Object> surveyResults){
		Map<String,String> eloquaFields=new HashMap<String, String>();
		
		// add mapped answers
		for(Entry<String, String> e:mapping.entrySet()){
			if (surveyResults.get(e.getKey()) instanceof String){
				eloquaFields.put(e.getValue(), (String)surveyResults.get(e.getKey()));
			}else{
				System.out.println("erm, what if the answer is not a string????");
			}
		}
		
		// add literal values
		for(Entry<String, String> e:values.entrySet()){
			eloquaFields.put(e.getKey(), e.getValue());
		}
		
		// Build & POST the request
		RequestSpecification rs=given()
		.urlEncodingEnabled(true)
		.contentType(ContentType.JSON)
		.header("Accept", ContentType.JSON.getAcceptHeader())
		;
		
		for (Entry<String, String> e:eloquaFields.entrySet()){
			log.debug(String.format("Sending Eloqua as querystring:: field=%s, value=%s", e.getKey(), e.getValue()));
			System.out.println(String.format("->Eloqua:: field=%s, value=%s", e.getKey(), e.getValue()));
			rs.queryParam(e.getKey(), e.getValue());
		}
//		url="https://s1795.t.eloqua.com/e/f2?elqSiteID=8091&elqFormName=consulting-assessment-integration-sandbox";
//		url="https://s1795.t.eloqua.com/e/f2";
		
		boolean dummy=true;
		if (!dummy){
			Response response=rs.post(url).andReturn();
			System.out.println(response.statusCode());
		}else{
			System.out.println("Dummy call, not sent to Eloqua");
		}
		
		
//		TODO: What happens if we get a statusCode != 200 ??? notify chat? log error?
		
	}
}
