package com.redhat.services.ae;

//import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
//import org.bson.Document;
//import org.bson.codecs.BsonTypeClassMap;
//import org.bson.codecs.DocumentCodec;
//import org.bson.codecs.configuration.CodecRegistries;
//import org.bson.codecs.configuration.CodecRegistry;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

//import io.restassured.http.Header;


@Path("/")
public class Controller{
	
	@GET
	@Path("/assets/js/env.js")
	public Response getEnvJavascript(){
		return Response.ok("var env={server:"+(System.getenv("SERVER")!=null?"\""+System.getenv("SERVER")+"\"":"undefined")+"}", "application/javascript").build();
	}
	
	
//	@GET
//  @Path("/survey/{Id}")
//  public Response getSurveyJavascript(@PathParam("Id") String surveyId, 
//  		@DefaultValue("application/json") @QueryParam("responseContentType") String responseContentType,
//  		@DefaultValue("false") @QueryParam("questionsOnly") String questionsOnly
//  		/*, @Context HttpServletRequest request, @Context HttpServletResponse response*/) throws URISyntaxException, IOException{
//		String surveyName=surveyId+".json";
////		String responseContentType=(request.getParameter("responseContentType")!=null?request.getParameter("responseContentType"):"application/json");
////		boolean questionsOnly="true".equalsIgnoreCase(request.getParameter("questionsOnly"));
//		
//		System.out.println("Loading questions: "+surveyName);
//		String surveyToInsert=IOUtils.toString(new File("target/classes", surveyName).exists()?new FileInputStream(new File("target/classes", surveyName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(surveyName), "UTF-8");
//		
//		String templateName="survey-template.js";
//		String template=IOUtils.toString(new File("target/classes", templateName).exists()?new FileInputStream(new File("target/classes", templateName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(templateName), "UTF-8");
//		
//		String result;
//		if ("true".equalsIgnoreCase(questionsOnly)){
//			result=surveyToInsert;
//		}else{
//			result=template.toString();
//			int i=result.indexOf("SURVEY_CONTENT");
//			if (i>=0){
//				result=new StringBuffer(result).delete(i, i+"SURVEY_CONTENT".length()).toString();
//				result=new StringBuffer(result).insert(i, surveyToInsert).toString();
//			}
//		}
//		
//  	return Response.status(200)
//        .header("Access-Control-Allow-Origin",  "*")
//        .header("Content-Type", null==responseContentType?"text/html; charset=UTF-8":responseContentType/*"application/json"*/)
//        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
//        .header("Pragma", "no-cache")
//        .header("X-Content-Type-Options", "nosniff").entity(result).build();
//  }
	
	
//	@GET
//  @Path("/surveyxxxxx/{Id}")
//  public Response getQuestions(@PathParam("Id") String surveyId, @Context HttpServletRequest request, @Context HttpServletResponse response) throws URISyntaxException, IOException{
//		String resourceName=surveyId+".json";
//		String responseContentType=(request.getParameter("responseContentType")!=null?request.getParameter("responseContentType"):"application/json");
//		
//		System.out.println("Loading questions: "+resourceName);
//		String result=IOUtils.toString(new File("target/classes", resourceName).exists()?new FileInputStream(new File("target/classes", resourceName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(resourceName));
//		
//  	return Response.status(200)
//        .header("Access-Control-Allow-Origin",  "*")
//        .header("Content-Type", null==responseContentType?"text/html; charset=UTF-8":responseContentType/*"application/json"*/)
//        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
//        .header("Pragma", "no-cache")
//        .header("X-Content-Type-Options", "nosniff").entity(result).build();
//  }
	
	
//  private Response returnTemplate(String resourceName, String responseContentType) throws IOException{
//  	InputStream is=new File("target/classes", resourceName).exists()?new FileInputStream(new File("target/classes", resourceName).getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(resourceName);
//  	String str=IOUtils.toString(is);
////  	String content=Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
//  	
////  	int i=str.indexOf("ROADMAP_TEMPLATE");
////  	if (i>=0){
////	  	str=new StringBuffer(str).delete(i, i+"ROADMAP_TEMPLATE".length()).toString();
////	  	str=new StringBuffer(str).insert(i, content).toString();
////  	}
//  	
//  	return Response.status(200)
//        .header("Access-Control-Allow-Origin",  "*")
//        .header("Content-Type", null==responseContentType?"text/html; charset=UTF-8":responseContentType/*"application/json"*/)
//        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
//        .header("Pragma", "no-cache")
//        .header("X-Content-Type-Options", "nosniff").entity(str).build();
//  }
	
}
