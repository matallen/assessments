package com.redhat.services.ae;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.google.api.client.util.Base64;
import com.google.common.base.Splitter;


@Path("/")
public class Controller{
	
	@GET
	@Path("/assets/js/env.js")
	public Response getEnvJavascript(){
		return Response.ok("var env={server:"+(System.getenv("SERVER")!=null?"\""+System.getenv("SERVER")+"\"":"undefined")+"}", "application/javascript").build();
	}
	
	
//	private Map<String,String> parseQueryString(String url) throws UnsupportedEncodingException{
//		Map<String,String> params=new HashMap<>();
//		for (String param:Splitter.on("&").trimResults().splitToList(url)){
//			String key=param.substring(0, param.indexOf("="));
//			String value=param.substring(param.indexOf("=")+1);
//			params.put(key,URLDecoder.decode(value, "UTF-8"));
//		}
//		return params;
//	}
//	
//	@POST
//	@Path("/login")
//	public Response login(String payload) throws Exception{
//		boolean success=false;
//		System.out.println("payload was "+payload);
//		Map<String, String> params=parseQueryString(payload);
//		if ("admin".equals(params.get("username")) && "admin".equals(params.get("password"))){
//			success=true;
//		}
//		
//		long ttlMillis=2 /*hrs*/ * 60 * 60 * 1000;
//		
//		String jwtToken=Jwt2.createJWT(ttlMillis*1000);
//		
////		String jwtToken=Jwt.createJWT(UUID.randomUUID().toString(), "RHAssessments", params.get("username"), ttlMillis);
////		String jwtTokenEncoded=new String(java.util.Base64.getEncoder().encode(jwtToken.getBytes()), "UTF-8");
//		
//		System.out.println(success?"success":"error");
//		if (success){
//			return Response.status(302).location(new URI("/admin.html")).cookie(new NewCookie("rhrti-jwt", jwtToken)).build();
////			return Response.temporaryRedirect(new URI("admin.html")).cookie(new NewCookie("rhjwt", jwtToken)).build();
//		}else{
//			return Response.status(302).location(new URI("/login.html?error=")).build();
//		}
//	}
//	@GET
//	@Path("/logout")
//	public Response logout() throws URISyntaxException, UnsupportedEncodingException{
//		
//		// delete cookie?
//		// invalidate session?
//		
//		return Response.status(302).location(new URI("/login.html")).cookie(new NewCookie("rhrti-jwt", null)) .build();
//	}
	
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
