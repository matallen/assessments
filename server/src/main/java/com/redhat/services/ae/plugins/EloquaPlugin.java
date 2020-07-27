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
import com.redhat.services.ae.controllers.AnswerProcessor;
import com.redhat.services.ae.model.Survey;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

// https://integrate.com/static/Home2/1.6.78.2/documents/marketing/Integrate_Eloqua_Implementation_Guide.pdf

public class EloquaPlugin implements Plugin{
	public static final Logger log=LoggerFactory.getLogger(EloquaPlugin.class);
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
		disabled=config.containsKey("disabled")?"true".equalsIgnoreCase((String)config.get("disabled")):false;
		
		if (null==url || null==mapping)
			throw new RuntimeException(String.format("Config not set correctly. Url is %s, and config==null is %s", url, (config==null)));
	}

	
	// gather the data from surveyResults, map it for the Eloqua http post request, build and send it
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults){
		Map<String,String> eloquaFields=new HashMap<String, String>();
		Map<String,String> flattenedSurveyResults=new HashMap<>();
		
		// exclude sending data to eloqua if your email is @redhat.com
		for (Entry<String, Object> e:surveyResults.entrySet()){
			String questionId=e.getKey();
			if (questionId.toLowerCase().contains("email")){
				if (String.class.isAssignableFrom(e.getValue().getClass())){
					String value=(String)e.getValue();
					if (value.toLowerCase().contains("@redhat.com")){
						log.info("Skipping Eloqua plugin because email is an @redhat.com email");
						return surveyResults;
					}
				}
			}
		}
		
		// Flatten the answers so they can be parsed more easily to send to Eloqua
		new AnswerProcessor(){
			@Override public void onStringAnswer(String questionId, String answerId, Integer score){
				System.out.println("Eloqua:: flattening single-string answers for question '"+questionId+"'");
				flattenedSurveyResults.put(questionId, answerId);
			}
			@Override public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){
				System.out.println("Eloqua:: flattening multi-select answers for question '"+questionId+"'");
				List<String> sList=new ArrayList<>();
				for (Answer answer:answerList) sList.add(answer.id);
				flattenedSurveyResults.put(questionId, Joiner.on(",").join(sList));
			}
			@Override public void onMapAnswer(String questionId, Answer answer){
				System.out.println("Eloqua:: flattening map-based answers for question '"+questionId+"'");
				flattenedSurveyResults.put(questionId, answer.id);
			}
		}.process(surveyResults);
		
		// map answers and add to the list of fields to send to eloqua
		for(Entry<String, String> e:mapping.entrySet()){
			if (flattenedSurveyResults.containsKey(e.getKey())){
				if (String.class.isAssignableFrom(flattenedSurveyResults.get(e.getKey()).getClass())){
					
					if (e.getValue().startsWith("UDF_")){
						eloquaFields.put(e.getValue()+"_Question", e.getKey());
						eloquaFields.put(e.getValue()+"_Answer", (String)flattenedSurveyResults.get(e.getKey()));
					}else{
						eloquaFields.put(e.getValue(), (String)flattenedSurveyResults.get(e.getKey()));
					}
				}else{
					System.err.println("error: what if the answer is not a string? do we flatten it into a comma separated string?");
					throw new RuntimeException("Encountered a non-String answer, don't know how to handle that yet");
				}
				
				//TODO: handle other answer structures, such as lists or sub-maps of answers
			}else{
				System.err.println("Eloqua mapped field '"+e.getKey()+"' was not found in survey results. Check your config");
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
			System.out.println(String.format("->Eloqua:: field=%-20s ,value=%s", e.getKey(), e.getValue()));
			rs.queryParam(e.getKey(), e.getValue());
		}
//		url="https://s1795.t.eloqua.com/e/f2?elqSiteID=8091&elqFormName=consulting-assessment-integration-sandbox";
//		url="https://s1795.t.eloqua.com/e/f2";
		
		if (!disabled){
			Response response=rs.post(url).andReturn();
			log.debug("Eloqua response statusCode="+response.statusCode());
		}else{
			log.info("Plugin disabled - dummy call, not sent to Eloqua");
		}
		
		
//		TODO: What happens if we get a statusCode != 200 ??? notify chat? log error?
		
		
		return surveyResults;
	}
}
