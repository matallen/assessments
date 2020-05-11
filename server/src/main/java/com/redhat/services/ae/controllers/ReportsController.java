package com.redhat.services.ae.controllers;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.model.Survey;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportsController{
	public static final Logger log=LoggerFactory.getLogger(ReportsController.class);
	
	@POST
	@Path("/{surveyId}/metrics/{pageId}")
	public Response gatherMetrics(@PathParam("surveyId") String surveyId, @PathParam("pageId") String pageId, @QueryParam("time") String time){
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		o.getMetrics().increment(pageId);
		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}
	
}
