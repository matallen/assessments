package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.Results;
import com.redhat.services.ae.model.MetricsDecorator;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.model.storage.Surveys;
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.plugins.RemovePIIAnswersPlugin;
import com.redhat.services.ae.utils.CacheHelper;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
public class SurveyController{
	public static final Logger log=LoggerFactory.getLogger(SurveyController.class);

	@GET
	@PermitAll
	@Path("/{surveyId}/survey-config.js")
	public Response getSurveyJavascript(@PathParam("surveyId") String surveyId, 
			@DefaultValue("application/json") @QueryParam("responseContentType") String responseContentType,
			@DefaultValue("false") @QueryParam("questionsOnly") String questionsOnly) throws IOException{
		String surveyName=surveyId+".json";
		
		log.debug("Loading questions: "+surveyName);
		
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
	
	
	@GET
	@PermitAll
	@Path("/{surveyId}/results/{resultId}")
	public Response getSurveyResults2(
			@PathParam("surveyId") String surveyId,
			@PathParam("resultId") String resultId
			) throws IOException{
		log.debug("results page loading from storage: "+resultId);
		return Response.ok().entity(Survey.findById(surveyId).getResults().get(resultId)).build();
//		return Response.ok().entity(Results.get().getResults().get(resultId)).build();
	}
	
	
//	@GET
//	@PermitAll
//	@Path("/{surveyId}/results/{rId}")
//	public Response getSurveyResults(
//			@PathParam("surveyId") String surveyId,
//			@PathParam("rId") String rId
//			) throws IOException{
//		
////		System.out.println("/results/::");
////		String xxx=CacheHelper.cache.get(surveyId+"_"+rId);
//		String xxx=Results.get().getResults().get(surveyId+"_"+rId);
//		
//		
////		Cache<String, String> cache=new CacheHelper<String>().getCache("resultDataTransfer", 10, 100, 300);
////		String xxx=cache.getIfPresent(surveyId+"_"+rId);
////		System.out.println("cache(resultDataTransfer).size="+CacheHelper.cache.size());
////		System.out.println("cache(resultDataTransfer).get("+(surveyId+"_"+rId)+") = "+xxx);
//		
//		return Response.ok().entity(xxx).build();
//	}
	
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
		
//		Map<String,Map<String,Object>> payloadObj=Json.toObject(payload, new TypeReference<HashMap<String,Map<String,Object>>>(){});
//		Map<String, Object> info=payloadObj.get("_page");
		
		// TODO: log the time window spent on page
		
		// TODO: Metrics: Increment the page count by month (TODO: this would better be implemented using a cache that lasts 24 hours rather than poluting our DB with visitor IDs)
//		long moreThanAMonthInSeconds=60*60*24*32;
//		Cache<String, String> visitorsThisMonth=new CacheHelper<String>().getCache("visitors", 10, 1000, moreThanAMonthInSeconds);
		
		// Metrics:: update "page transition" metrics
		updatePageTransitionMetrics(o, visitorId, YYMMM, pageId);
		
		o.saveMetrics();
		o.persist();
		return Response.ok().build();
	}
	

	
	
	@POST
	@Path("/{surveyId}/generateReport")
	public Response generateReport(@PathParam("surveyId") String surveyId, @QueryParam("visitorId") String visitorId, @QueryParam("pageId") String pageId, String payload) throws JsonParseException, JsonMappingException, IOException{
		log.info("generateReport::");
		
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		
		Map<String,Map<String,Object>> payloadObj=Json.toObject(payload, new TypeReference<HashMap<String,Map<String,Object>>>(){});
		String YYMMM=FluentCalendar.now().getString("yy-MMM");
		
		// Metrics:: Update "page transition" metrics
		updatePageTransitionMetrics(o, visitorId, YYMMM, pageId);
		
		// Metrics:: Update "survey complete" metrics
		Map<String, String> pageInfo=(Map<String,String>)(Object)payloadObj.get("_page"); // wow, this is a super hack to convert String/Object to String/String
		if (null!=pageInfo)
			updateSurveyCompleteMetrics(o, visitorId, YYMMM, pageInfo);
		
		// Metrics:: Update "cardinality of which answers selected" metrics
		Map<String, Object> surveyData=payloadObj.get("_data");
		log.debug("generateReport:: data="+Json.toJson(surveyData));
		updateAnswerMetrics(o, YYMMM, surveyData);
		
		// Save Survey & its metrics
		o.saveMetrics();
		o.persist();
		
		
		// Plugins:: Automatically add "Remove PII plugin" if it isn't configured
//		if (!Iterables.any(plugins.values(), new Predicate<Map<String,Object>>(){public boolean apply(@Nullable Map<String,Object> pluginCfg){
//					return pluginCfg.get("className").equals(RemovePIIAnswersPlugin.class.getName());
//				}})){
//			plugins.put("RemovePII", new MapBuilder<String,Object>().put("active", true).put("className",RemovePIIAnswersPlugin.class.getName()).build());
//		}
		
		
		// Plugins:: Execute post-survey plugins
		List<Plugin> activePlugins=new LinkedList<Plugin>();
		Map<String,Object> originalSurveyData=new LinkedHashMap<>(surveyData);
		// Plugins:: Create Plugin list
		for(Entry<String, Map<String, Object>> pl:o.getActivePlugins().entrySet()){
			try{
				Plugin plugin=(Plugin)Class.forName((String)pl.getValue().get("className")).newInstance();
				plugin.setConfig(pl.getValue());
				plugin.setOriginalSurveyResults(originalSurveyData);
				activePlugins.add(plugin);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		// Plugins:: Execute post-survey plugins
		for(Plugin plugin:activePlugins){
			try{
				surveyData=plugin.execute(surveyId, visitorId, surveyData); // after each plugin, keep the changes to the data (similar to the concept of Tomcat filters)
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		// Plugins:: Post-execution, cleanup
		for(Plugin plugin:activePlugins){
			plugin.onDestroy(surveyId, visitorId, surveyData);
		}
		
		
		//MAT - TODO: generate a report ID, and store the results (minus any personal info), return the ID so the index.html can redirect to report.html passing the unique ID
//		String uniqueReportId=surveyId+"_"+visitorId;
		String uniqueReportId=surveyData.containsKey("_reportId")?(String)surveyData.get("_reportId"):UUID.randomUUID().toString().replaceAll("-", ""); // reportId generated by the results plugin
		
		surveyData.put("_timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date()));
		
		// store the enriched/processed results for the results page to use
		log.debug("putting answers into cache, ready for the results page to render it");
		log.debug("cache.put("+(uniqueReportId)+") = "+Json.toJson(surveyData));
		
		o.getResults().put(uniqueReportId, Json.toJson(surveyData));
		o.saveResults();
//		Results results=Results.get();
//		results.getResults().put(uniqueReportId, Json.toJson(surveyData));
//		results.save();
		
		return Response.ok(uniqueReportId).build();
	}
	
	
	
  /** METRICS UPDATE METHODS */

	private void updatePageTransitionMetrics(Survey s, String visitorId, String YYMMM, String pageId){
		// Removing the visitor ID for the time being because, the cookie lasts 30 days, but if someone took the assessment mid month1, then it wouldnt register until after mid month2 if we had a cookie/page check
//		if (!Database.get().getVisitors(YYMMM).contains(visitorId+pageId)){
			log.debug("onPageChange:: incrementing monthly page counter for [visitorId="+visitorId+", pageId="+pageId+"]");
			
			new MetricsDecorator(s.getMetrics()).increment(1, "byMonth", "page", YYMMM, pageId);
//			s.getMetrics().getByMonth("page", YYMMM).put(pageId, s.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?s.getMetrics().getByMonth("page", YYMMM).load(pageId)+1:1);
//		}
	}
	private void updateSurveyCompleteMetrics(Survey s, String visitorId, String YYMMM, Map<String,String> info){
//		Map<String,String> info=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		String geo=(String)info.get("geo");
		MetricsDecorator m=new MetricsDecorator(s.getMetrics());
//		if (!Database.get().getVisitors(YYMMM).contains(visitorId)){
			m.increment(1, "completedByMonth", YYMMM);
//			s.getMetrics().getCompletedByMonth().put(YYMMM, s.getMetrics().getCompletedByMonth().containsKey(YYMMM)?s.getMetrics().getCompletedByMonth().load(YYMMM)+1:1);
			m.increment(1, "byMonth", "geo", YYMMM, geo);
//			s.getMetrics().getByMonth("geo", YYMMM).put(geo, s.getMetrics().getByMonth("geo", YYMMM).containsKey(geo)?s.getMetrics().getByMonth("geo", YYMMM).load(geo)+1:1);
//		}
	}
	
	private void updateAnswerMetrics(Survey s, String YYMMM, Map<String,Object> surveyData){
	  // Metrics: log how many times a specific answer was provided to a question, for reporting % of answers per question
		MetricsDecorator m=new MetricsDecorator(s.getMetrics());
		new AnswerProcessor(false){
			@Override public void onStringAnswer(String questionId, String answerId, Integer score){ // radiobuttons
				log.debug("Reports: Adding answers for question '"+questionId+"' to metrics");
//				Map<String, Map<String,Integer>> answers=s.getMetrics().getAnswersByMonth("answers", YYMMM);
//				if (!answers.containsKey(questionId)) answers.put(questionId, new HashMap<>());
//				answers.get(questionId).put(answerId, answers.get(questionId).containsKey(answerId)?answers.get(questionId).get(answerId)+1:1);
				m.increment(1, "answersByMonth", "answers", YYMMM, questionId, answerId);
			}
			@Override
			public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){ // multi-checkboxes
				log.debug("Reports: Adding answers for question '"+questionId+"' to metrics");
				for (Answer answer:answerList){
					// Increment the metrics for each item selected
//					Map<String, Map<String,Integer>> answers=s.getMetrics().getAnswersByMonth("answers", YYMMM);
//					if (!answers.containsKey(questionId)) answers.put(questionId, new HashMap<>());
//					answers.get(questionId).put(answer.id, answers.get(questionId).containsKey(answer.id)?answers.get(questionId).get(answer.id)+1:1);
					m.increment(1, "answersByMonth", "answers", YYMMM, questionId, answer.id);
				}
			}
			@Override
			public void onMapAnswer(String question, Answer answer){ // only seen this as a panel in surveyjs?
				// ignore for the purpose of metrics because it's most likely a contact form
			}
		}.process(surveyData);
	}
	
	
}
