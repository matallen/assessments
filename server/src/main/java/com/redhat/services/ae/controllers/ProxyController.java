package com.redhat.services.ae.controllers;

import static io.restassured.RestAssured.given;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.MapBuilder;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProxyController{
	public static final Logger log=LoggerFactory.getLogger(ProxyController.class);

	
	
	// TODO: Change the following geoInfo method to these:

	// Use this URL to get the country & region
	//   https://api.company-target.com/api/v2/ip.json?key=b6b603b47ded9a3eff17c78423bbc773b9817cf6&src=adobelaunch
	// then use this list to extract the continent (since above url doesnt provide continent info for our metrics) 
	// http://country.io/continent.json
	// +
	// https://github.com/datasets/continent-codes/blob/master/data/continent-codes.csv
	
	
	@GET
	@Path("/geoInfo")
	public Response proxy(@QueryParam("url") String url, @QueryParam("fields") String fields){
		
//		url="http://ip-api.com/json";
//		fields="continentCode,country,countryCode,region";
		
		Map<String,String> queryParams=new MapBuilder<String,String>().put("fields", fields).build();
		
		
		RequestSpecification rs=given()
				.contentType(ContentType.JSON)
				.header("Accept", ContentType.JSON.getAcceptHeader());
		
		for (Entry<String, String> e:queryParams.entrySet())
			rs.queryParam(e.getKey(), e.getValue());
		
		io.restassured.response.Response response=rs.post(url).andReturn();
		
		if (response.getStatusCode()==200){
			return Response.ok().entity(response.getBody().asString()).type(response.contentType()).build();
		}else{
			return Response.noContent().build();
		}
	}
}
