package com.redhat.services.ae.plugins;

import static io.restassured.RestAssured.given;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import mjson.Json;

public class Eloqua2Plugin extends EnrichAnswersPluginBase{
	public static final Logger log=LoggerFactory.getLogger(Eloqua2Plugin.class);
	private String url;
	private Map<String,String> mapping;
	private Map<String,String> values;
	private Map<String,String> expressions;
	boolean disabled=false;
	private String disabledIfExpression;
	private boolean disabledIfResult=false;
	
	
	@SuppressWarnings({"unchecked"})
	@Override
	public Plugin setConfig(Map<String, Object> cfg){
		url=(String)cfg.get("url");
		Map<String,Object> config=(Map<String,Object>)cfg.get("config");
		mapping=(Map<String,String>)config.get("mapping");
		values=(Map<String,String>)config.get("values");
		expressions=(Map<String,String>)config.get("expressions");
//		disabled=cfg.containsKey("disabled")?"true".equalsIgnoreCase((String)cfg.get("disabled")):false;
		disabledIfExpression=(String)cfg.get("disabledIf");
		
		if (cfg.containsKey("disabled")){
			if (String.class.isAssignableFrom(cfg.get("disabled").getClass())){
				disabled="true".equalsIgnoreCase((String)cfg.get("disabled"));
			}else if (Boolean.class.isAssignableFrom(cfg.get("disabled").getClass())){
				disabled=(Boolean)cfg.get("disabled");
			}
		}else
			disabled=false; // enabled by default
		
		
		
		if (null==url || null==mapping)
			throw new RuntimeException(String.format("Config not set correctly. Url is %s, and config==null is %s", url, (config==null)));
		
		return this;
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
		
//		System.out.println("ELOQUA: surveyResults="+com.redhat.services.ae.utils.Json.toJson(surveyResults));
		
		// Extract the question config
		Map<String, mjson.Json> questionsMapping=buildQuestionMap(surveyId);
		
		Map<String,Object> results=new LinkedHashMap<>();
		results.putAll(surveyResults);
		results.put("visitorId", visitorId);
		
		for (Entry<String, Object> e:results.entrySet()){
			String questionId=e.getKey();
			
			mjson.Json questionConfig=questionsMapping.get(questionId);
			if (null!=questionConfig){
//				System.out.println("found questionConfig for: "+questionId);
				
				if (null!=questionConfig){
					if (Map.class.isAssignableFrom(e.getValue().getClass())){
						Map<String,Object> value=(Map<String,Object>)e.getValue();
						if (value.containsKey("answer")){
							String answer=(String)value.get("answer");
							extractedAnswers.put(questionId, answer);
							
						}else if (value.containsKey("answers")){
							extractedAnswers.put(questionId, Joiner.on(",").join((Iterable)value.get("answers")));
						}
					}
					
				}
				
			}
			
		}
		
		List<String> errorExpectedFieldsNotFoundInSurveyResults=new ArrayList<String>();
		
		for(Entry<String, String> e:mapping.entrySet()){
			if (extractedAnswers.containsKey(e.getKey())){
				
				if (e.getValue().startsWith("UDF_")){
					eloquaFields.put(e.getValue()+"_Question", getTitle(e.getKey(), questionsMapping));
					eloquaFields.put(e.getValue()+"_Answer", (String)extractedAnswers.get(e.getKey()));
				}else{
					eloquaFields.put(e.getValue(), (String)extractedAnswers.get(e.getKey()));
				}
				
			}else{
				errorExpectedFieldsNotFoundInSurveyResults.add(e.getKey());
//				System.err.println("Eloqua mapped field '"+e.getKey()+"' was not found in survey results. Check your config");
			}
		}
		if (errorExpectedFieldsNotFoundInSurveyResults.size()>0){
			log.error("EloquaPlugin:: Following fields were configured to send to Eloqua, but were not found in the survey results:");
			for(String field:errorExpectedFieldsNotFoundInSurveyResults)
				log.error("   - "+field);
		}
		
		// Mat - TODO: review putting this here, it would be nice to have surveyId etc... available to all plugins
		
		// Mat! TODO: FIX! this breaks got OCP migration!
//		Map<String,String> answers=surveyResults.entrySet().stream()
//				.filter(map->map.getKey().startsWith("_"))
//				.collect(Collectors.toMap(map->map.getKey(),map->(String)map.getValue()));
		
		Map<String,String> answers=new HashMap<>();
		try{
//			answers.putAll(surveyResults.entrySet().stream()
//					.filter(map->map.getKey().startsWith("_"))
//					.collect(Collectors.toMap(map->map.getKey(),map->(String)map.getValue()))
//						);
			for (Entry<String, Object> e:surveyResults.entrySet()){
				if (e.getKey().startsWith("_")){
					if (String.class.isAssignableFrom(e.getValue().getClass())){
						answers.put(e.getKey(), (String)e.getValue());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		answers.putAll(extractedAnswers);
		answers.put("_surveyId", surveyId);
		answers.put("_reportId", (String)surveyResults.get("_reportId"));
		answers.put("_timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date()));
		answers.put("_visitorId", visitorId);
		
//		for(Entry<String, String> a:answers.entrySet())
//			System.out.println("Eloqua: substitute value - "+a.getKey()+"="+a.getValue());
		
		
		// Evaluate expressions prior to processing values in case an expression creates a new value
		if (null!=expressions){
			for (Entry<String, String> e:expressions.entrySet()){
				try{
					Object eval=MVEL.eval(e.getValue(), answers);
					eloquaFields.put(e.getKey(), eval.toString());
				}catch(PropertyAccessException ex){
					// sink, it's because the property doesnt exist... and that's ok
				}catch(Exception ex){
					log.warn("expression error with '"+e.getValue()+"': "+ex.getMessage());
//					ex.printStackTrace();
				}
			}
		}
		
		// Eloqua:: send literal values (+ replacement variables where configured)
		StringSubstitutor substitutor=new StringSubstitutor(answers); // replaces ${name} placeholders
		for(Entry<String, String> e:values.entrySet()){
			String value=substitutor.replace(e.getValue());
				eloquaFields.put(e.getKey(), value);
		}
		
		// execute mvel expression (ie. WorkEmail contains @redhat.com) then disabledIfResult becomes True
		try{
			Object eval=MVEL.eval(disabledIfExpression, answers);
			disabledIfResult=disabledIfResult || (eval instanceof Boolean && (boolean)eval);
		}catch(Exception ex){
			log.warn("Not disabling eloqua plugin, however an expression error occured: "+ex.getMessage());
			ex.printStackTrace();
		}
		if (disabledIfResult){
			log.warn("Skipping Eloqua plugin because disabledIf expression '"+disabledIfExpression+"' evaluated to true");
			for (Entry<String, String> e:eloquaFields.entrySet()){
				log.debug(String.format("EloquaPlugin[DISABLED]:: Adding param:: %s = %s", e.getKey(), e.getValue()));
			}
		}
		
		sendToEloqua(url, disabled || disabledIfResult, eloquaFields);
		
		return surveyResults;
	}
	
	public void sendToEloqua(String url, boolean disabled, Map<String,String> eloquaData){
		if (!disabled){
			RequestSpecification rs=given()
					.contentType(ContentType.JSON)
					.header("Accept", ContentType.JSON.getAcceptHeader())
					;
			for (Entry<String, String> e:eloquaFields.entrySet()){
				log.debug(String.format("EloquaPlugin:: Adding queryParam:: %s = %s", e.getKey(), e.getValue()));
				rs.queryParam(e.getKey(), e.getValue());
//				rs.formParam(e.getKey(), e.getValue());
			}

		
			log.debug("EloquaPlugin:: Sending request to Eloqua: "+url);
			Response response=rs.post(url).andReturn();
			
			if (response.getStatusCode()!=200){
				log.error("EloquaPlugin:: response statusCode="+response.statusCode());
			}else{
				log.debug("EloquaPlugin:: response statusCode="+response.statusCode());
			}
			
		}else{
			log.info("EloquaPlugin:: Plugin disabled - dummy call, not sent to Eloqua");
		}
	}
	
	@Override
	public Map<String, Object> OnSingleStringAnswer(String questionId, String answer, Json question){
		
//		extractedAnswers.put(questionId, "");
		
		return null;
	}

	@Override
	public Map<String, Object> OnMultipleStringAnswers(String questionId, List<String> answers, Json question){
		return null;
	}

}
