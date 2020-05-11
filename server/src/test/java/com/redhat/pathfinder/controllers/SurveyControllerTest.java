package com.redhat.pathfinder.controllers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
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

}