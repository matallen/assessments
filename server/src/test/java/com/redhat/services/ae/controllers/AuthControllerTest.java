package com.redhat.services.ae.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Assert;

import com.google.common.collect.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.AuthenticationController.Cookie;


public class AuthControllerTest{

	
	@org.junit.jupiter.api.Test
	public void testDomainExtraction() throws URISyntaxException{
		
		List<Map<String,String>> domains=Lists.newArrayList(
				new MapBuilder<String,String>().put("test", "http://localhost:8080/admin.html")																		 .put("expected", "localhost").build()
				,new MapBuilder<String,String>().put("test", "http://www.test.com:8080/admin.html")																		 .put("expected", "test.com").build()
				,new MapBuilder<String,String>().put("test", "http://192.168.1.123:8080/admin.html")																		 .put("expected", "192.168.1.123").build()
				,new MapBuilder<String,String>().put("test", "http://ui-assessments.6923.rh-us-east-1.openshiftapps.com/admin.html").put("expected", ".openshiftapps.com").build()
		);
		
		
		for(Map<String, String> e:domains){
			String test=e.get("test");
			String expected=e.get("expected");
			
			Cookie c=new AuthenticationController().new Cookie();
			Assert.assertEquals(expected, c.domainUrl(expected).domain);
			
		}
		
	}
	
}
