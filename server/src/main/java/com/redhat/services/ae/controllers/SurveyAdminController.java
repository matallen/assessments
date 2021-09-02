package com.redhat.services.ae.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;
//import org.apache.commons.lang3.StringUtils;
//import org.bson.types.ObjectId;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.kie.api.runtime.KieSession;
//import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Utils;
import com.redhat.services.ae.model.MultipartBody;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.model.storage.Surveys;
import com.redhat.services.ae.plugins.DroolsCompilationException;
import com.redhat.services.ae.plugins.DroolsScoreRecommendationsPlugin;
import com.redhat.services.ae.plugins.utils.MultipartReader;
import com.redhat.services.ae.plugins.utils.MultipartReader.MultipartReaderResult;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.StringUtils;

import io.netty.handler.codec.base64.Base64Decoder;

//import io.quarkus.mongodb.panache.PanacheMongoEntity;
//import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
//@Consumes(MediaType.APPLICATION_JSON)

@Path("/api/surveys")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
@RolesAllowed({"Admin"})
public class SurveyAdminController{
	public static final Logger log=LoggerFactory.getLogger(SurveyAdminController.class);
  @Inject JsonWebToken jwt;
  @Context SecurityContext ctx;

	/** ### Database Backup endpoint ### */
	
  /* temporary until backup solution is deployed with authentication */
  /* not used */
	@GET
	@PermitAll
	@Path("/database")
	public Response getDatabase() throws FileNotFoundException, IOException{
		return Response.ok(Json.toJson(Surveys.get())).build();
	}
	
  /* temporary until backup solution is deployed with authentication */
	@GET
	@PermitAll
	@Path("/{surveyId}/survey")
	public Response getSurveyBackup(@PathParam("surveyId") String surveyId) throws FileNotFoundException, IOException{
		return Response.ok(Json.toJson(Survey.findById(surveyId))).build();
	}
	
	/* temporary until backup solution is deployed with authentication */
	@GET
	@PermitAll
	@Path("/{surveyId}/results")
	public Response getSurveyResults(@PathParam("surveyId") String surveyId) throws IOException{
		return Response.ok(Json.toJson(Survey.findById(surveyId).getResults())).build();
	}
	
	/* temporary until backup solution is deployed with authentication */
	@GET
	@PermitAll
	@Path("/{surveyId}/metrics")
	public Response getSurveyMetrics(@PathParam("surveyId") String surveyId) throws IOException{
		return Response.ok(Json.toJson(Survey.findById(surveyId).getMetrics())).build();
	}
	
	/* temporary until backup solution is deployed with authentication */
	@GET
	@PermitAll
	@Path("/{surveyId}/backup")
	public Response getSurveyAll(@PathParam("surveyId") String surveyId) throws IOException{
		Survey s=Survey.findById(surveyId);
		Map<String,Object> all=new HashMap<String, Object>();
		all.put("results", s.getResults());
		all.put("metrics", s.getMetrics());
		all.put("questions", s.getQuestions());
		return Response.ok(Json.toJson(all)).build();
	}
	

  
	/** #### SURVEY HANDLERS ####  */
  
	@GET
	public Response listSurveys(){
		Surveys.reset(); // ensure to reload what's actually in the database
		return Response.ok(Survey.findAll()).build();
	}
	
	@GET
	@Path("/{surveyId}")
	public Response getSurvey(@PathParam("surveyId") String surveyId){
		return Response.ok(Survey.findById(surveyId)).build();
	}
	@POST
	public Response createSurvey(String payload) throws IOException{
		Survey o=Json.toObject(payload, Survey.class);
		if (StringUtils.isBlank(o.id)) o.id=Utils.generateId();
		if (Surveys.get().getSurveys().containsKey(o.id))
			throw new RuntimeException("Survey ID already exists");
		o.persist();
		return Response.ok(Survey.findById(o.id)).build();
	}
	@PUT
	@Path("/{surveyId}")
	public Response updateSurvey(@PathParam("surveyId") String surveyId, String payload) throws IOException{
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
	public Response deleteSingleSurvey(@PathParam String id) throws IOException{
		Survey entity=Survey.findById(id);
		if (null==entity) throw new WebApplicationException("Unable to find "+Survey.class.getClass().getSimpleName()+" with id "+id);
		entity.delete();
		return Response.status(204).build();
	}
	@DELETE
	public Response deleteManySurvey(String ids) throws IOException{
//		System.out.println("ids="+ids);
		List<String> l=Json.newObjectMapper(true).readValue(ids, new TypeReference<List<String>>(){});
		for (String id:l)
			deleteSingleSurvey(id);
		return Response.status(204).build();
	}
	@POST
	@Path("/{surveyId}/copy")
	public Response copySurvey(@PathParam("surveyId") String surveyId) throws IOException{
		Survey o=Survey.findById(surveyId);
		Survey copy=o.copy();
		return Response.ok(copy).build();
	}
	
	
	/** #### SURVEY RESOURCE HANDLERS ####  */
	
	@GET
	@PermitAll
	@Path("/{surveyId}/resources")
	public Response listResources(@PathParam("surveyId") String surveyId) throws IOException{
		return Response.ok(Survey.findById(surveyId).getResources()).build();
	}
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/{surveyId}/resources")
	public Response createResource(@PathParam("surveyId") String surveyId, MultipartFormDataInput data) throws IOException{
		MultipartReaderResult multipartData=new MultipartReader("file").process(data);
		log.info("CreateResource:: surveyId="+surveyId+", file="+multipartData.getFilename());
		Survey s=Survey.findById(surveyId);
		s.addResource(multipartData.getFilename(), multipartData.getFileStream());
		return listResources(surveyId);
	}
	@GET
	@PermitAll
	@Path("/{surveyId}/resources/{name}")
	public Response getResource(@PathParam("surveyId") String surveyId, @PathParam("name") String name) throws IOException{
		Survey survey=Survey.findById(surveyId);
		if (null==survey) return Response.status(Status.NOT_FOUND).build();
		
		File resource=survey.getResource(name);
		if (!resource.exists()) return Response.status(Status.NOT_FOUND).build();
		
		return Response.ok(new FileInputStream(resource)).build();
	}
	@DELETE
	@Path("/{surveyId}/resources/{name}")
	public Response deleteResource(@PathParam("surveyId") String surveyId, @PathParam("name") String name) throws IOException{
		Survey.findById(surveyId).deleteResource(name);
		return listResources(surveyId);
	}
	@DELETE
	@Path("/{surveyId}/resources")
	public Response deleteMultipleResources(@PathParam("surveyId") String surveyId, String listOfResourceNames) throws IOException{
		return Response.ok("TODO").build();
	}
	
	
	
	/** #### PLUGIN HANDLERS #### */
	
	@PUT
	@Path("/{surveyId}/plugins")
	public Response savePlugins(@PathParam("surveyId") String surveyId, String json) throws FileNotFoundException, IOException{
		Survey survey=Survey.findById(surveyId);
		log.debug("Saving Plugins: "+json);
		Map<String, Map<String, Object>> oJson=Json.newObjectMapper(true).readValue(json, new TypeReference<Map<String,Map<String,Object>>>(){});
		survey.getPlugins().clear();
		survey.getPlugins().putAll(oJson);
		survey.persist();
		return Response.ok(Json.toJson(survey.getPlugins())).build();
	}
	
	@GET
	@PermitAll
	@Path("/{surveyId}/plugins")
	public Response getPlugins(@PathParam("surveyId") String surveyId) throws FileNotFoundException, IOException{
		String surveyName=surveyId+".json";
		Survey survey=Survey.findById(surveyId);
		log.debug("Loading plugins for: "+surveyName);
		return Response.ok(Json.toJson(survey.getPlugins())).build();
	}
	
	/** #### GENERIC DRL RESULT RULE HANDLERS #### */
	
	@PUT
	@Path("/{surveyId}/result/rules")
	public Response saveTechnicalDRLResultRules(@PathParam("surveyId") String surveyId, Object rules) throws FileNotFoundException, IOException{
		Survey survey=Survey.findById(surveyId);
		String drl=new String(com.google.api.client.util.Base64.decodeBase64((String)rules));
		log.debug("Saving Technical DRL Result Rules: "+drl);
		survey.setRules(drl);
		survey.saveRules();
		survey.persist();
		return Response.ok(survey.getRules()).build();
	}
	
	@PUT
	@Path("/{surveyId}/result/rules/validate")
	public Response validateTechnicalDRLResultRules(@PathParam("surveyId") String surveyId, Object/*because String will give you a string with quotes in it!*/ rules) throws FileNotFoundException, IOException{
		DroolsScoreRecommendationsPlugin p=new DroolsScoreRecommendationsPlugin();
		try{
			String drl=new String(com.google.api.client.util.Base64.decodeBase64((String)rules));
			p.newKieSession(drl);
			return Response.ok().entity("Rules are valid.").build();
		}catch(DroolsCompilationException e){
			return Response.status(200).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@PermitAll
	@Path("/{surveyId}/result/rules")
	public Response getGenericResultRules(@PathParam("surveyId") String surveyId) throws FileNotFoundException, IOException{
		Survey survey=Survey.findById(surveyId);
		log.debug("Loading Result Rules for: "+surveyId);
		return Response.ok(survey.getRules()).build();
	}
	
	
	@DELETE
	@Path("/{surveyId}/metrics/reset")
	public Response metricsReset(@PathParam("surveyId") String surveyId) throws JsonParseException, JsonMappingException, IOException{
		Survey o=Survey.findById(surveyId);
		o.clearMetrics();
		o.persist();
		return Response.ok().build();
	}
	
	/** #### QUESTION HANDLERS #### */
	
	@PUT
	@Path("/{surveyId}/questions")
	public Response saveQuestions(@PathParam("surveyId") String surveyId, String questionsJson) throws FileNotFoundException, IOException{
		Survey survey=Survey.findById(surveyId);
		log.debug("Saving Questions:/n"+questionsJson);
		
		survey.setQuestionsAsString(questionsJson);
		survey.saveQuestions();
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
	
	
	/* this is for =Import(<url>) function an a google sheet for selection data validation */
	@GET
	@PermitAll
	@Path("/{surveyId}/questionNames/{format}")
	public Response getQuestions(@PathParam("surveyId") String surveyId, @PathParam("format") String format) throws FileNotFoundException, IOException{
		String surveyName=surveyId+".json";
		log.debug("Loading questionNames for: "+surveyName);
		String questions=Survey.findById(surveyId).getQuestionsAsString();
		mjson.Json root=mjson.Json.read(questions);
		List<String> lines=Lists.newArrayList();
		List<mjson.Json> pages=(List<mjson.Json>)root.at("pages").asJsonList();
		
		Function<String,String> addQuotes=s -> "\""+s+"\"";
		
		for(mjson.Json page:pages){
			List<mjson.Json> elements=(List<mjson.Json>)page.at("elements").asJsonList();
			for(mjson.Json question:elements){
				String name=question.at("name").asString();
				String type=question.at("type").asString();
				String title=question.at("title").asString();
//				lines.add(Joiner.on(", ").join(new String[]{name,type,title}));
				lines.add(Lists.newArrayList(new String[]{name,type,title}).stream()
						.map(addQuotes)
						.collect(Collectors.joining(", "))
				);
			}
		}
		
		String result=Joiner.on("\n").join(lines);
		return Response.ok().entity(result).build();
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
