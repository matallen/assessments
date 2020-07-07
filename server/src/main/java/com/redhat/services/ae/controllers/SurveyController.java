package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
public class SurveyController{
	public static final Logger log=LoggerFactory.getLogger(SurveyController.class);

	@GET
	@PermitAll
	@Path("/{surveyId}/run")
	public Response getSurveyJavascript(@PathParam("surveyId") String surveyId, 
			@DefaultValue("application/json") @QueryParam("responseContentType") String responseContentType,
			@DefaultValue("false") @QueryParam("questionsOnly") String questionsOnly) throws IOException{
		String surveyName=surveyId+".json";
		
		System.out.println("Loading questions: "+surveyName);
		
		String templateName="survey-template.js";
		String template=IOUtils.toString(new File("target/classes", templateName).exists()?new FileInputStream(new File("target/classes", templateName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(templateName), "UTF-8");
		
		Survey survey=Survey.findById(surveyId);
		String questions=survey.getQuestions();
		
		String result;
		if ("true".equalsIgnoreCase(questionsOnly)){
			result=questions;
		}else{
			result=template.toString();
			result=findReplace(result, "SURVEY_CONFIG", String.format("{theme: %s}", "\""+survey.theme+"\""));
			result=findReplace(result, "SURVEY_CONTENT", questions);
		}
		return Response.ok(result, null==responseContentType?"text/html; charset=UTF-8":responseContentType).build();
	}
	
	private String findReplace(String allContent, String find, String replace){
		int i=allContent.indexOf(find);
		if (i>=0){
			allContent=new StringBuffer(allContent).delete(i, i+find.length()).toString();
			allContent=new StringBuffer(allContent).insert(i, replace).toString();
		}
		return allContent;
	}
	
	@POST
	@Path("/{surveyId}/metrics/{pageId}/onPageChange")
	public Response onPageChange(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		
		// TODO: i don't like separating the data and info as it makes parsing less clean, but data may not be necessary anyway
		System.out.println("onPageChange: payload= "+payload);
		Map<String,String> info=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
//		Map<String,String> data=(Map<String,String>)pageData.get("data");
//		Map<String,String> info=(Map<String,String>)pageData.get("info");
		
		
		System.out.println("onPageChange:: info="+Json.toJson(info));
//		System.out.println("onPageChange:: data="+Json.toJson(data));
		
		// TODO: log the time window spent on page
		
		if (!Database.get().getVisitors(YYMMM).contains(visitorId+pageId))
			o.getMetrics().getByMonth("page", YYMMM).put(pageId, o.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?o.getMetrics().getByMonth("page", YYMMM).get(pageId)+1:1);

		o.persist();
		return Response.ok().build();
	}
	
	
//	// all because I cant type-strong parse out a map of maps...
//	private Map<String,Map<String,String>> getInfoAndData(String payload) throws JsonParseException, JsonMappingException, IOException{
//		Map<String,Map<String,String>> result=new HashMap<String, Map<String,String>>();
//		Map<String,Object> pageData=Json.toObject(payload, new TypeReference<HashMap<String,Object>>(){});
//		Map<String,String> data=(Map<String,String>)pageData.get("data");
//		Map<String,String> info=(Map<String,String>)pageData.get("info");
//		result.put("info", info);
//		result.put("data", data);
//		return result;
//	}

	@POST
	@Path("/{surveyId}/metrics/{pageId}/onComplete")
	public Response onComplete(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		
		onPageChange(surveyId, pageId, visitorId, payload);
		
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
//		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		Map<String,String> info=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
//		Map<String,String> data=(Map<String,String>)pageData.get("data");
//		Map<String,String> info=(Map<String,String>)pageData.get("info");
		
//		log.debug("onComplete:: data="+Json.toJson(data));
		
		String geo=info.get("geo");
		if (!Database.get().getVisitors(YYMMM).contains(visitorId)){
			o.getMetrics().getCompletedByMonth().put(YYMMM, o.getMetrics().getCompletedByMonth().containsKey(YYMMM)?o.getMetrics().getCompletedByMonth().get(YYMMM)+1:1);
//			o.getMetrics().getByMonth("page", YYMMM).put(pageId, o.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?o.getMetrics().getByMonth("page", YYMMM).get(pageId)+1:1);
			o.getMetrics().getByMonth("geo", YYMMM).put(geo, o.getMetrics().getByMonth("geo", YYMMM).containsKey(geo)?o.getMetrics().getByMonth("geo", YYMMM).get(geo)+1:1);
			//o.getMetrics().getByMonth("country", YYMMM).put(countryCode, o.getMetrics().getByMonth("country", YYMMM).containsKey(countryCode)?o.getMetrics().getByMonth("country", YYMMM).get(countryCode)+1:1);
		}
		
		o.persist();
		return Response.ok().build();
	}

	@POST
	@Path("/{surveyId}/metrics/onResults")
	public Response onResults(@PathParam("surveyId") String surveyId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		Map<String,Object> data=Json.toObject(payload, new TypeReference<HashMap<String,Object>>(){});
		
		System.out.println("resultsGathering:: data="+Json.toJson(data));
		
		// log how many times a specific answer was provided to a question, for reporting % of answers per question
		for (Entry<String, Object> e:data.entrySet()){
			String questionId=e.getKey();
			
			
			if (e.getValue() instanceof String){
				String answerId=(String)e.getValue();
				Map<String, Map<String,Integer>> answers=o.getMetrics().getAnswersByMonth("answers", YYMMM);
				if (!answers.containsKey(questionId)) answers.put(questionId, new HashMap<>());
				answers.get(questionId).put(answerId, answers.get(questionId).containsKey(answerId)?answers.get(questionId).get(answerId)+1:1);
			}else{
				System.out.println("erm, what if the answer is not a string????");
			}
			
		}
		o.persist();
		
		// check for post-survey plugins and execute
		Map<String, Map<String, Object>> plugins=o.getActivePlugins();
		for(Entry<String, Map<String, Object>> pl:plugins.entrySet()){
			String pluginName=pl.getKey();
			System.out.println("Executing Plugin: "+pluginName);
			String clazz=(String)pl.getValue().get("className");
			try{
				Plugin plugin=(Plugin)Class.forName(clazz).newInstance();
				plugin.setConfig(pl.getValue());
				plugin.execute(data);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return Response.ok(Survey.findById(o.id)).build();
	}
}
