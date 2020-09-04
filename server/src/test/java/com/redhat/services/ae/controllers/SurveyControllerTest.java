package com.redhat.services.ae.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;

@QuarkusTest
public class SurveyControllerTest {
	
	@BeforeEach
	public void init(){
		Database.get().getSurveys().clear();
		Database.get().save();
	}
  @Test
  public void getEmptyCustomers() {
      given()
        .when().get("/api/surveys")
        .then()
           .statusCode(200)
           .body(is("[]"));
  }
  @Test
  public void addNewAndGet() throws IOException {
  	given()
	  	.body("{\"name\":\"Survey 1\"}")
	  	.contentType("application/json")
  		.when().post("/api/surveys")
  		.then()
  			.statusCode(200)
  			.body("id", matchesPattern("^[A-Z]{6}$"))
  			.body("name", matchesPattern("^[A-Za-z0-9\\ -]+$"))
  			;
  	
  	given().when().get("/api/surveys").then()
    	.statusCode(200)
    	// Check the ID is 6 uppercase characters
    	.body("[0].id", matchesPattern("^[A-Z]{6}$"))
    	;
  }
  
	@Test
	public void testResultsGathering() throws ParseException, IOException{
		
		Survey survey=(Survey)new SurveyAdminController().create("{\"name\":\"TESTING\"}").getEntity();
		
		// TODO: fix - this is no longer correct as the payload is mixed with page stats 
		String testPayload="{\n" + 
				"  \"automation-development\" : \"item3\",\n" + 
				"  \"question7\" : \"item5\",\n" + 
				"  \"question6\" : \"item1\",\n" + 
				"  \"question9\" : \"item5\",\n" + 
				"  \"automation-operations\" : \"item3\",\n" + 
				"  \"question8\" : \"item3\",\n" + 
				"  \"question10\" : \"item1\",\n" + 
				"  \"methodology-operations\" : \"item4\",\n" + 
				"  \"question5\" : \"item5\",\n" + 
				"  \"methodology-development\" : \"item4\"\n" + 
				"}";
		
		
		System.out.println(
				new SurveyController().generateReport(survey.id, "visitorId-123", "pageId", testPayload)
		);
	}
	
	
	@Test
	public void testResultsGatheringWithMultiChoice() throws JsonParseException, JsonMappingException, IOException{
		Survey survey=(Survey)new SurveyAdminController().create("{\"name\":\"TESTING\"}").getEntity();
		String payload="{\"question1\":[\"lion\"],\"cloud_q1\":[\"item1\"],\"question3\":{\"text1\":\"John\",\"text2\":\"Doe\"}}";
		
	// TODO: fix - this is no longer correct as the payload is mixed with page stats 
		
		System.out.println(
				new SurveyController().generateReport(survey.id, "visitorId-123", "pageId", payload)
		);
	}
	
  @Test
  public void addWithSameIDShouldFail() {
  	given()
  	.body("{\"name\":\"Survey 2\"}")
  	.contentType("application/json")
		.when().post("/api/surveys")
		.then()
			.statusCode(200)
			.body(containsString("\"name\":\"Survey 2\""));
  	
  	given()
  	.body("{\"name\":\"Survey 2\"}")
  	.contentType("application/json")
		.when().post("/api/surveys")
		.then()
			.statusCode(500);
  	
  }
  
  
  @Test
  public void testPluginsRetainOrder() throws IOException{
  	Database.reset();
  	Database db=Database.get(new File("/home/mallen/Work/assessments/server/target/persistence/database.json"));
  	Survey survey=db.getSurveys().get("DMZZIW");
  	Map<String, Map<String, Object>> activePlugins=survey.getActivePlugins();
  	for(Entry<String, Map<String, Object>> e:activePlugins.entrySet()){
  		System.out.println(e.getKey());
  	}
  	
  	
  }
  

}