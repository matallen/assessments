package com.redhat.services.ae.plugins;

import static io.restassured.RestAssured.given;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.redhat.services.ae.model.Survey;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import mjson.Json;

public class Eloqua2Plugin extends EnrichAnswersPluginBase{
	public static final Logger log=LoggerFactory.getLogger(Eloqua2Plugin.class);
	private String url;
	private Map<String,String> mapping;
	private Map<String,String> values;
	boolean disabled=false;
	
	
	@SuppressWarnings({"unchecked"})
	@Override
	public void setConfig(Map<String, Object> cfg){
		url=(String)cfg.get("url");
		Map<String,Object> config=(Map<String,Object>)cfg.get("config");
		mapping=(Map<String,String>)config.get("mapping");
		values=(Map<String,String>)config.get("values");
		disabled=cfg.containsKey("disabled")?"true".equalsIgnoreCase((String)cfg.get("disabled")):false;
		
		if (null==url || null==mapping)
			throw new RuntimeException(String.format("Config not set correctly. Url is %s, and config==null is %s", url, (config==null)));
	}

	private Map<String,String> eloquaFields=new LinkedHashMap<String, String>();
	private Map<String,String> extractedAnswers=new HashMap<String, String>();
	
	
	private String getTitle(String questionId, Map<String, mjson.Json> questionConfig){
		Json cfg=questionConfig.get(questionId);
		if (null!=cfg){
			return cfg.at(cfg.has("title")?"title":"name").asString();
		}else{
			return null;
		}
	}
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
//		super.execute(surveyId, visitorId, surveyResults);
		
		// Extract the question config
		Map<String, mjson.Json> questionsMapping=buildQuestionMap(surveyId);
		
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			
			mjson.Json questionConfig=questionsMapping.get(questionId);
			if (null!=questionConfig){
				System.out.println("found questionConfig for: "+questionId);
				
				if (null!=questionConfig){
					if (Map.class.isAssignableFrom(e.getValue().getClass())){
						Map<String,Object> value=(Map<String,Object>)e.getValue();
						if (value.containsKey("answer")){
							extractedAnswers.put(questionId, (String)value.get("answer"));
						}else if (value.containsKey("answers")){
							extractedAnswers.put(questionId, Joiner.on(",").join((Iterable)value.get("answers")));
						}
					}
					
				}
				
			}
			
		}

		for(Entry<String, String> e:mapping.entrySet()){
			if (extractedAnswers.containsKey(e.getKey())){
				
				if (e.getValue().startsWith("UDF_")){
					eloquaFields.put(e.getValue()+"_Question", getTitle(e.getKey(), questionsMapping));
					eloquaFields.put(e.getValue()+"_Answer", (String)extractedAnswers.get(e.getKey()));
				}else{
					eloquaFields.put(e.getValue(), (String)extractedAnswers.get(e.getKey()));
				}
				
			}else{
				System.err.println("Eloqua mapped field '"+e.getKey()+"' was not found in survey results. Check your config");
			}
		}
		
		for(Entry<String, String> e:values.entrySet()){
			eloquaFields.put(e.getKey(), e.getValue());
		}
		
		sendToEloqua(url, disabled, eloquaFields);
		
		return surveyResults;
	}
	
	public void sendToEloqua(String url, boolean disabled, Map<String,String> eloquaData){
		RequestSpecification rs=given()
				.contentType(ContentType.JSON)
				.header("Accept", ContentType.JSON.getAcceptHeader())
				;
			
			for (Entry<String, String> e:eloquaFields.entrySet()){
				log.debug(String.format("Sending Eloqua as querystring:: field=%s, value=%s", e.getKey(), e.getValue()));
				System.out.println(String.format("->Eloqua:: field=%-20s ,value=%s", e.getKey(), e.getValue()));
				rs.queryParam(e.getKey(), e.getValue());
			}

			
			if (!disabled){
				Response response=rs.post(url).andReturn();
				log.debug("Eloqua response statusCode="+response.statusCode());
			}else{
				log.info("Plugin disabled - dummy call, not sent to Eloqua");
			}
	}
	
	@Override
	public Map<String, Object> OnSingleStringAnswer(String questionId, String answer, Json question){
		
		if (questionId.toLowerCase().contains("email")){
			log.info("Skipping Eloqua plugin because email is an @redhat.com email");
			disabled=true;
		}
		
		extractedAnswers.put(questionId, "");
		
		return null;
	}

	@Override
	public Map<String, Object> OnMultipleStringAnswers(String questionId, List<String> answers, Json question){
		return null;
	}

}
