package com.redhat.services.ae.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.User;
import com.redhat.services.ae.modules.login.LoginModule;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.Jwt;

@Path("/")
@RequestScoped
public class AuthenticationController{
	public static final Logger log=LoggerFactory.getLogger(AuthenticationController.class);
	
	@Context
	UriInfo uri;

	private static final String IPV4_REGEX =
			"^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
			"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
			"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
			"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

	public static String getDomainName(String url, boolean stripSubdomain) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    boolean isIp=IPv4_PATTERN.matcher(domain).matches();
    if (isIp) return domain;
    
    domain=domain.startsWith("www.") ? domain.substring(4) : domain;
    if (!isIp && stripSubdomain) domain=domain.substring(domain.indexOf(".")+1);
    return domain;
	}
	
	@POST
	@Path("/login")
	public Response login(String payload) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException{
		System.out.println("payload="+payload);
		Map<String, String> params=parseQueryString(payload);
		
		log.info("Attempting login with "+params.get("username")+"/****");//+params.get("password"));
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
				String domainName=getDomainName(uri.getBaseUri().toString(), true);

				System.out.println("URI.getPath="+uri.getPath());
				System.out.println("URI.getAbsolutePath="+uri.getAbsolutePath());
				System.out.println("URI.getRequestUri="+uri.getRequestUri());
				System.out.println("URI.getBaseUri.getHost="+uri.getBaseUri().getHost());
				
				if (uri.getRequestUri().toString().contains("localhost")){
					domainName="";//getDomainName(uri.getBaseUri().toString(), true); // for dev purposes
				}else
					domainName=getDomainName(params.get("onSuccess"), true);
				
//				domainName="";
				
				System.out.println("domain="+getDomainName(params.get("onSuccess"), true));
//				System.out.println("URI="+Json.toJson(uri));
//				uri.getAbsolutePath();
//				uri.get
//				System.out.println("domain name="+domainName);
//				log.info("returning jwt token in cookie rhae-jwt: "+jwtToken);
//				log.info("uri.baseUri = "+uri.getBaseUri());
//				log.info("uri.getPath= "+uri.getPath(true));
//				log.info("uri.getAbsolutePath = "+uri.getAbsolutePath());
//				log.info("uri.domainName = "+domainName);
				
				return Response.status(302)
						.location(new URI(params.get("onSuccess")))
//						.header("Access-Control-Allow-Origin", domainName)
//						.header("Access-Control-Allow-Credentials", "true")
//						.header("Access-Control-Allow-Methods", "GET, POST")
//						.header("Access-Control-Allow-Headers", "Content-Type, *")
//						.cookie(new NewCookie("rhae-jwt", jwtToken, "/", domainName, "__SAME_SITE_NONE__", 60*60 /*1hr*/, false, false))
						.header("Set-Cookie", "rhae-jwt="+jwtToken+";Path=/;Domain=redhat.com;Max-Age="+(60*60)+"; SameSite=none;")
						.build();
				
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
