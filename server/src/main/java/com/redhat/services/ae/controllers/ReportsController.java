package com.redhat.services.ae.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportsController{
	public static final Logger log=LoggerFactory.getLogger(ReportsController.class);

	
	@POST
	@Path("/{surveyId}/metrics/{pageId}/pageChange")
	public Response gatherMetricsPage(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		
		// TODO: log the time window spent on page
		
		if (!Database.get().getVisitors(YYMMM).contains(visitorId+pageId))
			o.getMetrics().getByMonth("page", YYMMM).put(pageId, o.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?o.getMetrics().getByMonth("page", YYMMM).get(pageId)+1:1);

		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}

	@POST
	@Path("/{surveyId}/metrics/{pageId}/complete")
	public Response gatherMetricsComplete(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("visitorId") String visitorId, String payload) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		Map<String,String> data=Json.toObject(payload, new TypeReference<HashMap<String,String>>(){});
		
		String geo=data.get("geo");
		String countryCode=data.get("countryCode");
		if (!Database.get().getVisitors(YYMMM).contains(visitorId)){
			o.getMetrics().getCompletedByMonth().put(YYMMM, o.getMetrics().getCompletedByMonth().containsKey(YYMMM)?o.getMetrics().getCompletedByMonth().get(YYMMM)+1:1);
			o.getMetrics().getByMonth("page", YYMMM).put(pageId, o.getMetrics().getByMonth("page", YYMMM).containsKey(pageId)?o.getMetrics().getByMonth("page", YYMMM).get(pageId)+1:1);
			o.getMetrics().getByMonth("geo", YYMMM).put(geo, o.getMetrics().getByMonth("geo", YYMMM).containsKey(geo)?o.getMetrics().getByMonth("geo", YYMMM).get(geo)+1:1);
			o.getMetrics().getByMonth("country", YYMMM).put(countryCode, o.getMetrics().getByMonth("country", YYMMM).containsKey(countryCode)?o.getMetrics().getByMonth("country", YYMMM).get(countryCode)+1:1);
		}
		
		o.persist();
		return Response.ok(Survey.findById(o.id)).build();	}

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
	
	
	@GET
	@Path("/{surveyId}/reports/surveyCount")
	public Response getSurveyCount(@PathParam("surveyId") String surveyId, @QueryParam("daterange") String daterange) throws JsonProcessingException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		
		ChartJson c=new ChartJson();
		DataSet ds=c.addNewDataSet();
		for (Entry<String, Integer> e:o.getMetrics().getCompletedByMonth().entrySet()){
			c.getLabels().add(e.getKey());
			ds.getData().add(e.getValue());
		}
		
		return Response.ok(Json.toJson(c)).build();
	}

	@GET
	@Path("/{surveyId}/reports/pageCount")
	public Response getPageCount(@PathParam("surveyId") String surveyId, @QueryParam("daterange") String daterange) throws JsonProcessingException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		
		ChartJson c=new ChartJson();
		for (Entry<String, Map<String, Integer>> e:o.getMetrics().getByMonth("page").entrySet()){
			DataSet ds=c.addNewDataSet();
			for (Entry<String, Integer> f:e.getValue().entrySet()){
				c.getLabels().add(f.getKey());
				ds.getData().add(f.getValue());
				ds.setLabel(e.getKey());
			}
		}
		
		return Response.ok(Json.toJson(c)).build();
	}

}
