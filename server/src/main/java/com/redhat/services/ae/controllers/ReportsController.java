package com.redhat.services.ae.controllers;

import java.util.Date;
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

import com.fasterxml.jackson.core.JsonProcessingException;
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
	@Path("/{surveyId}/metrics/{pageId}")
	public Response gatherMetrics(@PathParam("surveyId") String surveyId, 
			@PathParam("pageId") String pageId, 
			@QueryParam("time") String time, 
			@QueryParam("visitorId") String visitorId,
			@QueryParam("country") String countryCode,
			@QueryParam("regionCode") String regionCode){
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		
		String YYMMM=FluentCalendar.get(new Date()).getString("yy-MMM");
		
//		if (!Database.get().getVisitors(YYMMM).contains(visitorId)){
			o.getMetrics().incrementSurvey(YYMMM);
//		}
		o.getMetrics().incrementPage(YYMMM, pageId);
		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}
	
	
	@GET
	@Path("/{surveyId}/reports/surveyCount")
	public Response getSurveyCount(@PathParam("surveyId") String surveyId, @QueryParam("daterange") String daterange) throws JsonProcessingException{
		Database.reset(); // force a reload
		Survey o=Survey.findById(surveyId);
		
		ChartJson c=new ChartJson();
		DataSet ds=c.addNewDataSet();
		for (Entry<String, Integer> e:o.getMetrics().getSurveyCountByMonth().entrySet()){
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
		for (Entry<String, Map<String, Integer>> e:o.getMetrics().getPageCountByMonth().entrySet()){
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
