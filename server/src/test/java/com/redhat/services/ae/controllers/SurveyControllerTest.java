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
import java.util.Optional;
import java.util.Map.Entry;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
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
import com.google.api.client.util.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.model.storage.Surveys;
import com.redhat.services.ae.utils.Json;

import io.quarkus.test.junit.QuarkusTest;
//import io.quarkus.test.security.TestSecurity;
import io.restassured.response.Response;
import io.smallrye.config.common.utils.ConfigSourceUtil;

@QuarkusTest
public class SurveyControllerTest {
	
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
		
		Survey survey=(Survey)new SurveyAdminController().createSurvey("{\"name\":\"TESTING\"}").getEntity();
		
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
	public void testDefect() throws JsonParseException, JsonMappingException, IOException{
		Surveys.STORAGE_ROOT="target/persistence_testing";
		Survey s=Survey.builder()
		.id(SurveyControllerTest.class.getSimpleName())
		.name("My Test Survey")
		.theme("theme1")
		.build();
		s.save();
		
		String testPayload="{\"_data\":{\n" + 
				"  \"interests\" : [ \"platforms\" ],\n" + 
				"  \"platforms_env_q1\" : \"Operations and software/infrastructure  support\",\n" + 
				"  \"platforms_env_q2\" : \"100+\",\n" + 
				"  \"platforms_env_q3\" : [ \"100#DevSecOps\" ],\n" + 
				"  \"platforms_env_q4\" : \"100#Advanced automation using IaC principles (e.g: Ansible + CI/CD)\"\n" + 
				"}}";
		
		
		System.out.println(
				new SurveyController().generateReport(s.id, "visitorId-123", "pageId", testPayload)
		);
	}
	
	
	
	@Test
	public void testResultsGatheringWithMultiChoice() throws JsonParseException, JsonMappingException, IOException{
		Survey survey=(Survey)new SurveyAdminController().createSurvey("{\"name\":\"SurveyControllerTest-1\"}").getEntity();
//		String payload="{\"question1\":[\"lion\"],\"cloud_q1\":[\"item1\"],\"question3\":{\"text1\":\"John\",\"text2\":\"Doe\"}}";
		String visitorId="61b76b3d-401b-4eda-9e6a-842697cf466f";
		String payload2="{\"_page\":{\"visitorId\":\""+visitorId+"\",\"timeOnpage\":\"27 sec\",\"geo\":\"NA\",\"countryCode\":\"US\",\"region\":\"TX\"},\"_data\":{\"interests\":[\"platforms\"],\"platforms_env_q1\":\"Operations and software/infrastructure  support\",\"platforms_env_q2\":\"100+\",\"platforms_env_q3\":[\"100#DevSecOps\"],\"platforms_env_q4\":\"100#Advanced automation using IaC principles (e.g: Ansible + CI/CD)\",\"_FirstName\":\"test\",\"_LastName\":\"test\",\"_WorkEmail\":\"mallen@redhat.com\",\"_WorkPhone\":\"123\",\"_Company\":\"123\",\"_Industry\":\"Aerospace & Defense\",\"_Department\":\"IT - Business Intelligence\",\"_JobRole\":\"Chief Architect\",\"_Country\":\"US\"}}";
		
		
		new SurveyController().generateReport(survey.id, visitorId, "pageId", payload2);
		survey=Survey.findById(survey.id);
		
		System.out.println(
				survey.getMetrics()
				);
		
		
//		System.out.println(
//				new SurveyController().generateReport(survey.id, visitorId, "pageId", payload2)
//		);
	}
	
  @Test
//  @TestSecurity(authorizationEnabled = false)
  public void addWithSameIDShouldFail() {
  	String sameSurveyName="Duplicate Survey Name";
  	given()
  	.body("{\"name\":\""+sameSurveyName+"\"}")
  	.contentType("application/json")
		.when().post("/api/surveys")
		.then()
			.statusCode(200)
			.body(containsString("\"name\":\""+sameSurveyName+"\""));
  	
  	given()
  	.body("{\"name\":\""+sameSurveyName+"\"}")
  	.contentType("application/json")
		.when().post("/api/surveys")
		.then()
			.statusCode(500);
  	
  }
  
  
  @Test
  public void testPluginsRetainOrder() throws IOException{
  	Surveys.reset();
  	Surveys db=Surveys.get(new File("/home/mallen/Work/assessments/server/target/persistence/database.json"));
  	Survey survey=db.getSurveys().get("DMZZIW");
  	Map<String, Map<String, Object>> activePlugins=survey.getActivePlugins();
  	for(Entry<String, Map<String, Object>> e:activePlugins.entrySet()){
  		System.out.println(e.getKey());
  	}
  	
  	
  }
  

}