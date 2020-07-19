package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.utils.CacheHelper;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;

@Path("/api/surveys")
//@Produces(MediaType.APPLICATION_JSON)
public class CustomFieldController{
	public static final Logger log=LoggerFactory.getLogger(CustomFieldController.class);

	@GET
	@PermitAll
	@Path("/{surveyId}/countryList")
	public Response getCountries(@PathParam("surveyId") String surveyId) throws IOException{
		String surveyName=surveyId+".json";
		
		System.out.println("Loading countries: "+surveyName);
		List<Map<String,String>> countries=new ArrayList<>();
		countries.add(new MapBuilder<String,String>().put("name", "United States").put("value", "USA").build());
		countries.add(new MapBuilder<String,String>().put("name", "United Kingdom").put("value", "GBP").build());
		countries.add(new MapBuilder<String,String>().put("name", "Italy").put("value", "ITA").build());
		
		return Response.ok(Json.toJson(countries)).type(MediaType.APPLICATION_JSON).build();
	}
	
	@GET
	@PermitAll
	@Path("/{surveyId}/departments")
	public Response getDepartments(@PathParam("surveyId") String surveyId) throws IOException{
		String surveyName=surveyId+".json";
		
		System.out.println("Loading departments: "+surveyName);
		
		List<String> list=new ArrayList<>();
		list.add("Dept 1");
		list.add("Dept 2");
		list.add("Dept 3");
		
		return Response.ok(Json.toJson(list)).build();
	}
	
	@GET
	@PermitAll
	@Path("/{surveyId}/jobroles")
	public Response getJobRoles(@PathParam("surveyId") String surveyId) throws IOException{
		String surveyName=surveyId+".json";
		
		System.out.println("Loading jobroles: "+surveyName);
		
		List<String> list=new ArrayList<>();
		list.add("Job 1");
		list.add("Job 2");
		list.add("Job 3");
		
		return Response.ok(Json.toJson(list)).build();
	}
	
	
	
	
	
}
