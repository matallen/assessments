package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;
//import org.apache.commons.lang3.StringUtils;
//import org.bson.types.ObjectId;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.Utils;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.StringUtils;

//import io.quarkus.mongodb.panache.PanacheMongoEntity;
//import io.quarkus.mongodb.panache.PanacheMongoEntityBase;

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@RolesAllowed({"Admin"})
public class SurveyAdminController{
	public static final Logger log=LoggerFactory.getLogger(SurveyAdminController.class);
  @Inject JsonWebToken jwt;
  @Context SecurityContext ctx;
  
	/** #### SURVEY HANDLERS ####  */
  
	@GET
	public Response list(){
		return Response.ok(Survey.findAll()).build();
	}
	
	@GET
	@Path("/{surveyId}")
	public Response get(@PathParam("surveyId") String surveyId){
		return Response.ok(Survey.findById(surveyId)).build();
	}
	@POST
	@RolesAllowed({"Admin"})
	public Response create(String payload) throws IOException{
		Survey o=Json.toObject(payload, Survey.class);
		if (StringUtils.isBlank(o.id)) o.id=Utils.generateId();
		if (Database.get().getSurveys().containsKey(o.id))
			throw new RuntimeException("Survey ID already exists");
		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}
	@PUT
	@Path("/{surveyId}")
	public Response update(@PathParam("surveyId") String surveyId, String payload) throws IOException{
//		System.out.println("PUT detected - payload = "+payload);
		Survey o=Json.toObject(payload, Survey.class);
		Survey entity=Survey.findById(surveyId);
		if (null==entity) throw new WebApplicationException("Unable to find "+Survey.class.getSimpleName()+" with id "+surveyId);
		entity=Survey.builder().populate(o, entity);
		entity.update();
		return Response.ok(entity).build();
	}
	@DELETE
	@Path("/{id}")
	public Response deleteSingle(@PathParam String id) throws IOException{
		Survey entity=Survey.findById(id);
		if (null==entity) throw new WebApplicationException("Unable to find "+Survey.class.getClass().getSimpleName()+" with id "+id);
		entity.delete();
		return Response.status(204).build();
	}
	@DELETE
	public Response deleteMany(String ids) throws IOException{
//		System.out.println("ids="+ids);
		List<String> l=Json.newObjectMapper(true).readValue(ids, new TypeReference<List<String>>(){});
		for (String id:l)
			deleteSingle(id);
		return Response.status(204).build();
	}
	@POST
	@Path("/{surveyId}/copy")
	public Response copy(@PathParam("surveyId") String surveyId) throws IOException{
		Survey o=Survey.findById(surveyId);
		Survey copy=o.copy();
		return Response.ok(copy).build();
	}
	
	/** ### Database Backup endpoint ### */
	
	@GET
	@PermitAll
	@Path("/database")
	public Response getDatabase(@PathParam("surveyId") String surveyId) throws FileNotFoundException, IOException{
		return Response.ok(Json.toJson(Database.get())).build();
		
	}	
	
	/** #### QUESTION HANDLERS #### */
	
	@PUT
	@Path("/{surveyId}/questions")
	public Response saveQuestions(@PathParam("surveyId") String surveyId, String questionsJson) throws FileNotFoundException, IOException{
		Survey survey=Survey.findById(surveyId);
		log.debug("Saving Questions:/n"+questionsJson);
		survey.setQuestions(questionsJson);
		survey.update();
		return Response.ok(Survey.findById(surveyId).getQuestions()).build();
	}
	
	@GET
	@PermitAll
	@Path("/{surveyId}/questions")
	public Response getQuestions(@PathParam("surveyId") String surveyId) throws FileNotFoundException, IOException{
		String surveyName=surveyId+".json";
		log.debug("Loading questions for: "+surveyName);
		return Response.ok(Survey.findById(surveyId).getQuestions()).build();
	}
	
	/** #### PLUGINS HANDLER #### */
	@GET
	@Path("/{surveyId}/plugins/{pluginName}")
	public Response getPlugin(@PathParam("surveyId") String surveyId, @PathParam("pluginName") String pluginName) throws FileNotFoundException, IOException{
		Survey o=Survey.findById(surveyId);
		System.out.println("survey="+o);
		Map<String, Object> config=o.getActivePlugins().get(pluginName);
		System.out.println("plugins="+o.getPlugins());
		System.out.println("activePlugins="+o.getActivePlugins());
		System.out.println("config="+Json.toJson(config));
		return Response.ok(Json.toJson(config)).build();
	}
	
}
