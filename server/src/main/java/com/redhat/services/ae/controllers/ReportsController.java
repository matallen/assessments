package com.redhat.services.ae.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.charts.ChartJson;
import com.redhat.services.ae.charts.DataSet;
import com.redhat.services.ae.charts.PieChartJson;
import com.redhat.services.ae.charts.PieData;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.Pair;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportsController{
	public static final Logger log=LoggerFactory.getLogger(ReportsController.class);

	
	@POST
	@Path("/{surveyId}/metrics/{pageId}/onPageChange")
	public Response onPageChange(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		System.out.println("onPageChange:: data="+Json.toJson(data));
		// TODO: log the time window spent on page
		
		if (!Database.get().getVisitors(YYMMM).contains(visitorId+pageId))
			o.getMetrics().getByMonth("page", YYMMM).put(pageId, o.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?o.getMetrics().getByMonth("page", YYMMM).get(pageId)+1:1);

		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}

	@POST
	@Path("/{surveyId}/metrics/{pageId}/onComplete")
	public Response onComplete(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		
		onPageChange(surveyId, pageId, visitorId, payload);
		
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		
		System.out.println("onComplete:: data="+Json.toJson(data));
		
		String geo=data.get("info.geo");
		//String countryCode=data.get("info.countryCode");
		if (!Database.get().getVisitors(YYMMM).contains(visitorId)){
			o.getMetrics().getCompletedByMonth().put(YYMMM, o.getMetrics().getCompletedByMonth().containsKey(YYMMM)?o.getMetrics().getCompletedByMonth().get(YYMMM)+1:1);
//			o.getMetrics().getByMonth("page", YYMMM).put(pageId, o.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?o.getMetrics().getByMonth("page", YYMMM).get(pageId)+1:1);
			o.getMetrics().getByMonth("geo", YYMMM).put(geo, o.getMetrics().getByMonth("geo", YYMMM).containsKey(geo)?o.getMetrics().getByMonth("geo", YYMMM).get(geo)+1:1);
			//o.getMetrics().getByMonth("country", YYMMM).put(countryCode, o.getMetrics().getByMonth("country", YYMMM).containsKey(countryCode)?o.getMetrics().getByMonth("country", YYMMM).get(countryCode)+1:1);
		}
		
		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}

	@POST
	@Path("/{surveyId}/metrics/onResults")
	public Response onResults(@PathParam("surveyId") String surveyId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		
		System.out.println("resultsGathering:: data="+Json.toJson(data));
		
		for (Entry<String, String> e:data.entrySet()){
			String questionId=e.getKey();
			String answerId=e.getValue();
			
			Map<String, Map<String,Integer>> answers=o.getMetrics().getAnswersByMonth("answers", YYMMM);
			if (!answers.containsKey(questionId)) answers.put(questionId, new HashMap<>());
			answers.get(questionId).put(answerId, answers.get(questionId).containsKey(answerId)?answers.get(questionId).get(answerId)+1:1);
			
			// log how many times a specific answer was provided to a question, for reporting % of answers per question
//			YOU ARE HERE
//			o.getMetrics().getByMonth("questions", YYMMM).get(questionId).put(answerId, o.getMetrics().getByMonth("questions", YYMMM).containsKey(answerId)?o.getMetrics().getByMonth("questions", YYMMM).get(answerId):1);
			
			
//			o.getMetrics().getByMonth("answers", YYMMM).put(questionId, o.getMetrics().getByMonth("answers", YYMMM).containsKey(questionId)?o.getMetrics().getByMonth("answers", YYMMM).get(questionId)+1:1);
			
		}
		o.persist();
		
		return Response.ok(Survey.findById(o.id)).build();
	}
	
//	@POST
//	@Path("/{surveyId}/metrics/{pageId}")
//	public Response gatherMetrics(
//			@PathParam("surveyId") String surveyId, 
//			@PathParam("pageId") String pageId, 
//			@QueryParam("visitorId") String visitorId
//			,String payload
////			,@QueryParam("geo") String geo
////			,@QueryParam("countryCode") String countryCode
////			,@QueryParam("regionCode") String regionCode
//			) throws JsonParseException, JsonMappingException, IOException{
//		Survey o=Survey.findById(surveyId);
//		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
//		
//		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
//		
////		if (!Database.get().getVisitors(YYMMM).contains(visitorId))
////			o.getMetrics().incrementSurvey(YYMMM);
//		
//		
////		o.getMetrics().getGeoByMonth()
//		
//		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
//		String geo=data.get("geo");
//		String countryCode=data.get("countryCode");
//		String region=data.get("region");
//		
//		if (!Database.get().getVisitors(YYMMM).contains(visitorId)){
//			o.getMetrics().getSurveyByMonth().put(YYMMM, o.getMetrics().getSurveyByMonth().containsKey(YYMMM)?o.getMetrics().getSurveyByMonth().get(YYMMM)+1:1);
//			o.getMetrics().getByMonth("geoByMonth", YYMMM).put(geo, o.getMetrics().getByMonth("geoByMonth", YYMMM).containsKey(geo)?o.getMetrics().getByMonth("geoByMonth", YYMMM).get(geo)+1:1);
//			o.getMetrics().getByMonth("countryByMonth", YYMMM).put(countryCode, o.getMetrics().getByMonth("countryByMonth", YYMMM).containsKey(countryCode)?o.getMetrics().getByMonth("countryByMonth", YYMMM).get(countryCode)+1:1);
//		}
//		
//		if (!Database.get().getVisitors(YYMMM).contains(visitorId+pageId))
//			o.getMetrics().getPageByMonth(YYMMM).put(pageId, o.getMetrics().getPageByMonth(YYMMM).containsKey(pageId)?o.getMetrics().getPageByMonth(YYMMM).get(pageId)+1:1);
//		
//		
//		
//		
////		o.getMetrics().incrementPage(YYMMM, pageId);
//		o.persist();
//		return Response.ok(Survey.findById(o.id)).build();
//	}
	
	
	private Pair<Calendar,Calendar> buildDateRange(String start, String end) throws ParseException{
		// SDF is not threadsafe, must have this inside method
		SimpleDateFormat YYYYMMM=new SimpleDateFormat("yyyy-MMM");
		SimpleDateFormat YYMMM=new SimpleDateFormat("yy-MMM");
		SimpleDateFormat sdf=start.matches("\\d{4}-\\s{3}")?YYYYMMM:YYMMM;
		
		try{
			Calendar startDate=FluentCalendar.get(sdf.parse(start)).firstDayOfMonth().startOfDay().build();
			Calendar endDate=FluentCalendar.get(sdf.parse(end)).lastDayOfMonth().endOfDay().build();
			System.out.println("Filtering data between: "+sdf.format(startDate.getTime())+" and "+sdf.format(endDate.getTime()));
			return new Pair<Calendar,Calendar>(startDate, endDate);
		}catch(NumberFormatException e){
			throw e;
		}
	}
	
	private boolean isOnOrWithinRange(String dateStr, Pair<Calendar,Calendar> range) throws ParseException{
		// SDF is not threadsafe, must have this inside method
		SimpleDateFormat YYYYMMM=new SimpleDateFormat("yyyy-MMM");
		SimpleDateFormat YYMMM=new SimpleDateFormat("yy-MMM");

		Calendar testDate;
		try{
			SimpleDateFormat sdf=dateStr.matches("\\d{4}-\\s{3}")?YYYYMMM:YYMMM;
			testDate=FluentCalendar.get(sdf.parse(dateStr)).firstDayOfMonth().startOfDay().build();
			return testDate.getTime().getTime() >= range.getFirst().getTime().getTime() &&
					testDate.getTime().getTime() <= range.getSecond().getTime().getTime();
		}catch(ParseException e){
			log.error("date format unreadable: "+dateStr);
			throw e;
		}
	}
	
	@GET
	@Path("/{surveyId}/reports/surveyCount")
	public Response getSurveyCount(@PathParam("surveyId") String surveyId, @QueryParam("start") String dateRangeStart, @QueryParam("end") String dateRangeEnd) throws JsonProcessingException, ParseException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		Pair<Calendar, Calendar> range=buildDateRange(dateRangeStart, dateRangeEnd);
		SimpleDateFormat YYMMM=new SimpleDateFormat("yy-MMM");
		
		ChartJson c=new ChartJson();
		DataSet ds=c.addNewDataSet();
		ds.setLabel("Total Surveys Completed");
		
		Calendar start=range.getFirst();
		while (start.before(range.getSecond())) {
			String month=YYMMM.format(start.getTime());
			c.getLabels().add(month);
			ds.getData().add(o.getMetrics().getCompletedByMonth().containsKey(month)?o.getMetrics().getCompletedByMonth().get(month):0);
			start.add(Calendar.MONTH, 1);
		}
		
//		for (Entry<String, Integer> e:o.getMetrics().getCompletedByMonth().entrySet()){
//			if (isOnOrWithinRange(e.getKey(), range)){
//				c.getLabels().add(e.getKey());
//				ds.getData().add(e.getValue());
//			}
//		}
		
		return Response.ok(Json.toJson(c)).build();
	}

	@GET
	@Path("/{surveyId}/reports/pageCount")
	public Response getPageCount(@PathParam("surveyId") String surveyId, @QueryParam("start") String dateRangeStart, @QueryParam("end") String dateRangeEnd) throws JsonProcessingException, ParseException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		Pair<Calendar, Calendar> range=buildDateRange(dateRangeStart, dateRangeEnd);
		
//		System.out.println("range="+range);
		
		ChartJson c=new ChartJson();
		DataSet ds=c.addNewDataSet();
		ds.setLabel("Total Pages Completed");
//		// get absolute list of pages, then build the chart data from those
		Set<String> pages=new TreeSet<>();
		
		boolean pagesFromQuestions=true;
		if (!pagesFromQuestions) // then they come from metrics history 
			for (Entry<String, Map<String, Integer>> e:o.getMetrics().getByMonth("page").entrySet())
				pages.addAll(e.getValue().keySet());
		
		// sort the pages in the same order the questions were asked
		if (pagesFromQuestions){
			try{
				List<mjson.Json> pagesx=mjson.Json.read(o.getQuestions()).at("pages").asJsonList();
				for(mjson.Json page:pagesx){
					String pageName=page.at("name").asString();
					pages.add(pageName);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		for (String page:pages){
			c.getLabels().add(page);
			int total=0;
			for (Entry<String, Map<String, Integer>> e:o.getMetrics().getByMonth("page").entrySet()){
				if (isOnOrWithinRange(e.getKey(), range)){
					total+= e.getValue()!=null && e.getValue().get(page)!=null?e.getValue().get(page):0;
				}
			}
			ds.getData().add(total);
		}
		
		return Response.ok(Json.toJson(c)).build();
	}
	
	@GET
	@Path("/{surveyId}/reports/surveyCountByGeo")
	public Response getSurveyCountByGeo(@PathParam("surveyId") String surveyId, @QueryParam("start") String dateRangeStart, @QueryParam("end") String dateRangeEnd) throws JsonProcessingException, ParseException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		Pair<Calendar, Calendar> range=buildDateRange(dateRangeStart, dateRangeEnd);
		
		PieChartJson c=new PieChartJson();
		Set<String> keys=new HashSet<>(); // geos
		for (Entry<String, Map<String, Integer>> x:o.getMetrics().getByMonth("geo").entrySet())
			keys.addAll(x.getValue().keySet());
		
		PieData pd2=new PieData();
		for(String geo:keys){
			c.getLabels().add(geo);
			int sum=0;
			for (Entry<String, Map<String, Integer>> e:o.getMetrics().getByMonth("geo").entrySet())
				if (isOnOrWithinRange(e.getKey(), range))
					sum+=e.getValue().containsKey(geo)?e.getValue().get(geo):0;
			pd2.getData().add(sum);
		}
		c.getDatasets().add(pd2);
		
		return Response.ok(Json.toJson(c)).build();
	}

	
//	@GET
//	@Path("/{surveyId}/reports/answers")
//	public Response getQuestions(@PathParam("surveyId") String surveyId) throws ParseException, FileNotFoundException, IOException{
//		Database.reset(); // force a reload
//		Survey o=Survey.findById(surveyId);
//		List<Map<String,String>> questions=new ArrayList<>();
//		List<mjson.Json> pages=mjson.Json.read(o.getQuestions()).at("pages").asJsonList();
//		for(mjson.Json page:pages){
//			String pageName=page.at("name").asString();
//			
//			for (mjson.Json question:page.at("elements").asJsonList()){
//				String questionName=question.at("name").asString();
//				String url="/api/surveys/{surveyId}/reports/answers/"+questionName;
//				questions.add(new MapBuilder<String,String>().put("name", questionName).put("url", url).put("type", "horizontalBar").build());
//			}
//		}
//		return Response.ok(Json.toJson(questions)).build();
//	}
//	@GET
//	@Path("/{surveyId}/reports/answers/{questionName}")
//	public Response getQuestion(@PathParam("surveyId") String surveyId, @PathParam("questionName") String questionName, @QueryParam("start") String dateRangeStart, @QueryParam("end") String dateRangeEnd) throws ParseException, FileNotFoundException, IOException{
//		Database.reset(); // force a reload
//		Survey o=Survey.findById(surveyId);
//		Pair<Calendar, Calendar> range=buildDateRange(dateRangeStart, dateRangeEnd);
//		
//		ChartJson c=new ChartJson();
//		try{
//			List<mjson.Json> pagesx=mjson.Json.read(o.getQuestions()).at("pages").asJsonList();
//			for(mjson.Json page:pagesx){
//				String pageName=page.at("name").asString();
//				
//				for (mjson.Json question:page.at("elements").asJsonList()){
////					ChartJson c=new ChartJson();
//					
//					if (questionName.equals(question.at("name").asString())){
////						c.setName(questionName);
//						if (!question.has("choices")) continue;
//						DataSet ds=c.addNewDataSet();
//						for (mjson.Json option:question.at("choices").asJsonList()){
//							String value=option.at("value").asString();
//							String text=option.at("text").asString();
//							c.getLabels().add(text);
//							ds.setLabel("Count");
//							ds.getData().add(getRandomInteger(1,10));
//						}
//						
//					}
//					
//				}
//			}
//			
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//		
//		return Response.ok(Json.toJson(c)).build();
//	}
	
	@GET
	@Path("/{surveyId}/reports/answerPercentages")
	public Response answerPercentages(@PathParam("surveyId") String surveyId, @QueryParam("start") String dateRangeStart, @QueryParam("end") String dateRangeEnd) throws JsonProcessingException, ParseException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		Pair<Calendar, Calendar> range=buildDateRange(dateRangeStart, dateRangeEnd);
		
		List<ChartJson> result=new ArrayList<>();
		
		try{
			List<mjson.Json> pagesx=mjson.Json.read(o.getQuestions()).at("pages").asJsonList();
			for(mjson.Json page:pagesx){
				String pageName=page.at("name").asString();
				
				for (mjson.Json question:page.at("elements").asJsonList()){
					ChartJson c=new ChartJson();
					String questionId=question.at("name").asString();
					c.setName(questionId);
//					c.getLabels().add(questionName);
					
					if (!question.has("choices")) continue;
					DataSet ds=c.addNewDataSet();
					for (mjson.Json option:question.at("choices").asJsonList()){
						String answerId=option.at("value").asString();
						String answerText=option.at("text").asString();
						c.getLabels().add(answerText);
//						ds.setLabel("Count");
//						ds.getData().add(getRandomInteger(1,10));
						
						// go through the metrics per month to find the question/answer combo cardinality
						int count=0;
						for (Entry<String, Map<String, Map<String, Integer>>> e:o.getMetrics().getAnswersByMonth("answers").entrySet()){			
							if (isOnOrWithinRange(e.getKey(), range)){
								if (e.getValue().containsKey(questionId)){
									count+=(e.getValue().get(questionId).containsKey(answerId)?e.getValue().get(questionId).get(answerId):0); 
								}
							}
						}
						ds.getData().add(count);
					}
					
//					result.put(questionName, c);
					result.add(c);
//					 FOR NOW, ONE QUESTION ONLY
//					throw new IOException("xxx");
				}
			}
			

			for (Entry<String, Map<String, Map<String, Integer>>> e:o.getMetrics().getAnswersByMonth("answers").entrySet()){
				if (isOnOrWithinRange(e.getKey(), range)){
					Map<String, Map<String, Integer>> answer=e.getValue();
					
					
					
					int count=0;
				}
				
			}

//			
//					
//					if (isOnOrWithinRange(e.getKey(), range)){
//						
//					}
//					o.getMetrics().getAnswersByMonth("answers", YYMMM)
//					
//				}
//				
//				DataSet ds=c.addNewDataSet();
//				ds.setLabel("Question 1: Count (), Percentage (%)");
//				ds.getData().addAll(Arrays.asList(1, 2, 3)); // for each answer
//				
//			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
//		
//		DataSet ds=c.addNewDataSet();
//		ds.setLabel("Question 1: Count (), Percentage (%)");
//		ds.getData().addAll(Arrays.asList(1, 2, 3)); // for each answer
//		
		
		
		return Response.ok(Json.toJson(result)).build();
	}
	
	public static int getRandomInteger(int maximum, int minimum){ return ((int) (Math.random()*(maximum - minimum))) + minimum; }
		

}
