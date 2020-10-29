package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.MetricsDecorator;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.plugins.UpdateAnswerMetricsPlugin;
import com.redhat.services.ae.plugins.utils.RemovePIIAnswersPlugin;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;

import org.checkerframework.checker.nullness.qual.Nullable;

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
		log.debug("serving survey-config.js : "+surveyId);
		
		String templateName="survey-template.js";
		String template=IOUtils.toString(new File("target/classes", templateName).exists()?new FileInputStream(new File("target/classes", templateName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(templateName), "UTF-8");
		
		Survey survey=Survey.findById(surveyId);
		String questions=survey.getQuestionsAsString();
		
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
	@Path("/{surveyId}/config")
	public Response getConfig(@PathParam("surveyId") String surveyId) throws IOException{
		return Response.ok(Json.toJson(Survey.findById(surveyId).getConfig())).build();
	}
  
	@PUT
	@RolesAllowed({"Admin"})
	@Path("/{surveyId}/config")
	public Response updateConfig(@PathParam("surveyId") String surveyId, String payload) throws IOException{
		System.out.println("payload="+payload);
		Survey s=Json.toObject(payload, Survey.class);
		System.out.println("s="+s);
		Survey entity=Survey.findById(surveyId);
		if (null==entity) throw new WebApplicationException("Unable to find "+Survey.class.getSimpleName()+" with id "+surveyId);
		entity=Survey.builder().populate(s, entity);
		entity.update();
		return Response.ok(entity).build();
	}
	
	@GET
	@PermitAll
	@Path("/{surveyId}/results/{resultId}")
	public Response getSurveyResults(
			@PathParam("surveyId") String surveyId,
			@PathParam("resultId") String resultId
			) throws IOException{
		log.debug("results page loading from storage: "+resultId);
		return Response.ok().entity(Survey.findById(surveyId).getResults().get(resultId)).build();
//		return Response.ok().entity(Results.get().getResults().get(resultId)).build();
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
	@PermitAll
	@Path("/{surveyId}/metrics/{pageId}/onPageChange")
	public Response onPageChange(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		log.debug("onPageChange:: "+surveyId);
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
	@PermitAll
	@Path("/{surveyId}/generateReport")
	public Response generateReport(@PathParam("surveyId") String surveyId, @QueryParam("visitorId") String visitorId, @QueryParam("pageId") String pageId, String payload) throws JsonParseException, JsonMappingException, IOException{
		log.debug("generateReport:: "+surveyId);
		
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
		
		// TODO: Move this to plugins so we can configure under what conditions it fires
//		updateAnswerMetrics(o, YYMMM, surveyData);
		
		// Save Survey & its metrics
		o.saveMetrics();
		o.persist();
		
		
		// Plugins:: Automatically add "Remove PII plugin" if it isn't configured
//		if (!Iterables.any(plugins.values(), new Predicate<Map<String,Object>>(){public boolean apply(@Nullable Map<String,Object> pluginCfg){
//					return pluginCfg.get("className").equals(RemovePIIAnswersPlugin.class.getName());
//				}})){
//			plugins.put("RemovePII", new MapBuilder<String,Object>().put("active", true).put("className",RemovePIIAnswersPlugin.class.getName()).build());
//		}
		
		
		// Plugins:: Add/Insert any mandatory plugins, prior to executing the plugin chain
//		List<Plugin> activePlugins=new LinkedList<Plugin>();
//		Iterables.any(o.getActivePlugins().values(), new Predicate<Map<String,Object>>(){
//			@Override
//			public boolean apply(@org.checkerframework.checker.nullness.qual.Nullable Map<String, Object> input){
//				return false;
//			}});
		
		Map<String, Map<String, Object>> activePluginMap=new LinkedHashMap<String, Map<String,Object>>();
		// Plugins:: Add any mandatory plugins at the start of the plugin chain
		if (!Iterables.any(o.getActivePlugins().values(), new Predicate<Map<String,Object>>(){public boolean apply(@Nullable Map<String,Object> pluginCfg){
			return pluginCfg.get("className").equals(UpdateAnswerMetricsPlugin.class.getName());}})){
			log.info("Adding default 'UpdateAnswerMetrics' since it was not found in plugin chain");
			activePluginMap.put("UpdateAnswerMetrics", new MapBuilder<String,Object>().put("active", true).put("className",UpdateAnswerMetricsPlugin.class.getName()).build());
		}
		activePluginMap.putAll(o.getActivePlugins());
		// Plugins:: Add any mandatory plugins at the end of the plugin chain
//		if (!Iterables.any(activePluginMap.values(), new Predicate<Map<String,Object>>(){public boolean apply(@Nullable Map<String,Object> pluginCfg){
//			return pluginCfg.get("className").equals(RemovePIIAnswersPlugin.class.getName());}})){
//			activePluginMap.put("RemovePIIAnswers", new MapBuilder<String,Object>().put("active", true).put("className",RemovePIIAnswersPlugin.class.getName()).build());
//		}
		

		// Plugins:: Execute post-survey plugins
		List<Plugin> activePlugins=new LinkedList<Plugin>();
		Map<String,Object> originalSurveyData=new LinkedHashMap<>(surveyData);
		// Plugins:: Create Plugin list
		for(Entry<String, Map<String, Object>> pl:activePluginMap.entrySet()){
			try{
				Plugin plugin=(Plugin)Class.forName((String)pl.getValue().get("className")).newInstance();
				plugin._setConfig(pl.getValue());
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
//				System.out.println("_reportId="+surveyData.get("_reportId"));
//				System.out.println("After '"+plugin.getClass().getSimpleName()+"': "+Json.toJson(surveyData));
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		// Plugins:: Post-execution, cleanup
		for(Plugin plugin:activePlugins){
			plugin.onDestroy(surveyId, visitorId, surveyData);
		}
		
		// Add a timestamp if it doesnt exist
		if (!surveyData.containsKey("_timestamp"))
			surveyData.put("_timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date()));
		
		// A Results plugin should ALWAYS have fired and created a result ID, so it should always exist
		String uniqueReportId=(String)surveyData.get("_reportId");
		if (null==uniqueReportId){
			log.warn("No _reportId was found in the survey data - if Eloqua has been sent a result link in an earlier plugin then it's likely it will not work. Generating a new _resultId now");
			uniqueReportId=UUID.randomUUID().toString().replaceAll("-", "");
		}
			
		// store the enriched/processed results for the results page to use
		log.debug("putting answers into cache, ready for the results page to render it");
		log.debug("cache.put("+(uniqueReportId)+") = "+Json.toJson(surveyData));
		
		o.getResults().put(uniqueReportId, Json.toJson(surveyData));
		o.saveResults();
		
		return Response.ok(uniqueReportId).build();
	}
	
	
	
  /** METRICS UPDATE METHODS */

	private void updatePageTransitionMetrics(Survey s, String visitorId, String YYMMM, String pageId){
		// Removing the visitor ID for the time being because, the cookie lasts 30 days, but if someone took the assessment mid month1, then it wouldnt register until after mid month2 if we had a cookie/page check
//		log.debug("onPageChange:: incrementing monthly page counter for [visitorId="+visitorId+", pageId="+pageId+"]");
		new MetricsDecorator(s.getMetrics()).increment(1, "pageTransitionsByMonth", YYMMM, pageId);
	}
	
	private void updateSurveyCompleteMetrics(Survey s, String visitorId, String YYMMM, Map<String,String> info){
		String geo=(String)info.get("geo");
		MetricsDecorator m=new MetricsDecorator(s.getMetrics());
		m.increment(1, "completedSurveysByMonth", YYMMM);
		m.increment(1, "geoByMonth", YYMMM, geo);
	}
	
	private void updateAnswerMetrics(Survey s, String YYMMM, Map<String,Object> surveyData){
	  // Metrics: log how many times a specific answer was provided to a question, for reporting % of answers per question
		MetricsDecorator m=new MetricsDecorator(s.getMetrics());
		new AnswerProcessor(false){
			@Override public void onStringAnswer(String questionId, String answerId, Integer score){ // radiobuttons
				log.debug("Updating answer distrib metrics for question '"+questionId+"'");
				m.increment(1, "answerDistribution", YYMMM, questionId, answerId);
			}
			@Override
			public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){ // multi-checkboxes
				log.debug("Updating answer distrib metrics for question '"+questionId+"'");
				for (Answer answer:answerList){
					// Increment the metrics for each item selected
					m.increment(1, "answerDistribution", YYMMM, questionId, answer.id);
				}
			}
			@Override
			public void onMapAnswer(String question, Answer answer){ // only seen this as a panel in surveyjs?
				// ignore for the purpose of metrics because it's most likely a contact form
			}
		}.process(surveyData);
	}
	
	
}
