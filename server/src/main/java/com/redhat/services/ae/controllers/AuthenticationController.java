package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharSet;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;
//import org.apache.commons.lang3.StringUtils;
//import org.bson.types.ObjectId;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Splitter;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.Utils;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.model.User;
import com.redhat.services.ae.modules.login.DefaultLoginModule.Role;
import com.redhat.services.ae.modules.login.LoginModule;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.Jwt;
import com.redhat.services.ae.utils.StringUtils;

@Path("/")
@RequestScoped
public class AuthenticationController{
	public static final Logger log=LoggerFactory.getLogger(AuthenticationController.class);
	
	@Context
	UriInfo uri;

	public static String getDomainName(String url, boolean stripSubdomain) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    domain=domain.startsWith("www.") ? domain.substring(4) : domain;
    domain=domain.substring(domain.indexOf(".")+1);
    return domain;
	}
	
	@POST
	@Path("/login")
	public Response login(String payload) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException{
		Map<String, String> params=parseQueryString(payload);
		
		log.info("Attempting login with "+params.get("username")+"/"+params.get("password"));
		String loginModuleClass=ConfigProvider.getConfig().getValue("modules.login.class", String.class);
		try{
			LoginModule loginModule=(LoginModule)Class.forName(loginModuleClass).newInstance();
			User user=loginModule.login(params.get("username"), params.get("password"));
			if (null!=user){
				Map<String,Object> jwtClaims=new MapBuilder<String,Object>()
						.put("iss", "https://quarkus.io/using-jwt-rbac")
						.put("upn", user.getUsername())
						.put("groups", user.getRoles())
						.build();
				
				Long ttlMins=Long.parseLong(ConfigProvider.getConfig().getValue("modules.login.jwt.ttlInMins", String.class));
				
				String jwtToken=Jwt.createJWT(jwtClaims, ttlMins*60);
				log.info("returning jwt token in cookie rhae-jwt: "+jwtToken);
				log.info("uri.baseUri = "+uri.getBaseUri());
				log.info("uri.getPath= "+uri.getPath(true));
				log.info("uri.getAbsolutePath = "+uri.getAbsolutePath());
				
				log.info("uri.domainName = "+getDomainName(uri.getBaseUri().toString(), true));
				
				return Response.status(302).location(new URI(params.get("onSuccess"))).cookie(new NewCookie("rhae-jwt", jwtToken, "/", getDomainName(uri.getBaseUri().toString(), true), "comment", 60*60 /*1hr*/, false)).build();
				
			}else{
				log.info("Failure to authenticate user, returning to login screen with error 0");
				System.out.println("onFailure.url:: "+params.get("onFailure"));
				return Response.status(302).location(new URI(params.get("onFailure")+"?error=0")).build();
			}
		}catch(Exception e){
			throw new RuntimeException("Unable to instantiate login module: "+loginModuleClass, e);
		}
	}
	
	@GET
	@Path("/logout")
	public Response logout(@QueryParam("onSuccess") String onSuccess) throws URISyntaxException, UnsupportedEncodingException{
		// delete cookie?
		// invalidate session?
		return Response.status(302).location(new URI(onSuccess)).cookie(new NewCookie("rhae-jwt", null)) .build();
	}

	private Map<String,String> parseQueryString(String url) throws UnsupportedEncodingException{
		Map<String,String> params=new HashMap<>();
		for (String param:Splitter.on("&").trimResults().splitToList(url)){
			String key=param.substring(0, param.indexOf("="));
			String value=param.substring(param.indexOf("=")+1);
			params.put(key,URLDecoder.decode(value, "UTF-8"));
		}
		return params;
	}
}
