package com.redhat.services.ae.plugins;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

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
		Map<String,String> flattenedSurveyResults=new HashMap<>();
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			if (String.class.isAssignableFrom(e.getValue().getClass())){
				flattenedSurveyResults.put(e.getKey(), (String)e.getValue());
			}else if (List.class.isAssignableFrom(e.getValue().getClass())){
				List<String> answers=new ArrayList<String>();
				for(Object v: (List)e.getValue()){
					if (String.class.isAssignableFrom(v.getClass())){
						answers.add((String)v);	
					}else{
						System.err.println("surveyResults item was a list, but not a list of strings... please investigate. key = "+e.getKey());
					}
				}
				flattenedSurveyResults.put(e.getKey(), Joiner.on(",").join(answers));
			}else if (Map.class.isAssignableFrom(e.getValue().getClass())){
				for(Entry<String, String> e2: ((Map<String,String>)e.getValue()).entrySet()){
					flattenedSurveyResults.put(e2.getKey(), e2.getValue());
				}
			}
		}
		
		
		// add mapped answers
		for(Entry<String, String> e:mapping.entrySet()){
			if (flattenedSurveyResults.containsKey(e.getKey())){
				if (String.class.isAssignableFrom(flattenedSurveyResults.get(e.getKey()).getClass())){
					eloquaFields.put(e.getValue(), (String)flattenedSurveyResults.get(e.getKey()));
				}else{
					System.err.println("error: what if the answer is not a string? do we flatten it into a comma separated string?");
					throw new RuntimeException("Encountered a non-String answer, don't know how to handle that yet");
				}
				
				//TODO: handle other answer structures, such as lists or sub-maps of answers
			}else{
				System.err.println("configured/mapped field '"+e.getKey()+"' was not found in survey results. Check your config");
			}
		}
		
		// add literal values
		for(Entry<String, String> e:values.entrySet()){
			eloquaFields.put(e.getKey(), e.getValue());
		}
		
		// Build & POST the request
		RequestSpecification rs=given()
//		.urlEncodingEnabled(true)
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
