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
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.Pair;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportsController{
	public static final Logger log=LoggerFactory.getLogger(ReportsController.class);
	
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
						String answerText=option.at("text").isString()?option.at("text").asString():option.at("text").at("default").asString(); // non, or multilingual answer format
						
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
					
					result.add(c);
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return Response.ok(Json.toJson(result)).build();
	}
	
	public static int getRandomInteger(int maximum, int minimum){ return ((int) (Math.random()*(maximum - minimum))) + minimum; }
	
	
	/* ********** UTILITY FUNCTIONS ************ */
	
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
	

}
