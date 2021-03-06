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

	
	enum SameSite{Strict,Lax,None}
	class Cookie{
		private static final String IPV4_REGEX =
				"^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
				"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
				"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
				"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
		private final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);
		protected String name,value,domain,path,maxAge,sameSite,secure,httpOnly;
		public Cookie name(String v){this.name=v;return this;}
		public Cookie value(String v){this.value=v;return this;}
		public Cookie path(String v){this.path=v;return this;}
		public Cookie domainStr(String v){this.domain=v;return this;}
		public Cookie domainUrl(String v){this.domain=getDomainForCookie(v);return this;}
		public Cookie maxAge(Integer v){this.maxAge=String.valueOf(v);return this;}
		public Cookie maxAge(String v){this.maxAge=v;return this;}
		public Cookie httpOnly(){this.httpOnly="HttpOnly";return this;}
		public Cookie secure(){this.secure="Secure";return this;}
		public Cookie sameSite(SameSite v){this.sameSite=v.name();return this;}
		public String build(){
			return name+"="+value+";"+
					(path!=null?  "Path="+path+";":"")+
					(domain!=null?"Domain="+domain+";":"")+
					(maxAge!=null?"Max-Age="+maxAge+";":"")+
					(httpOnly!=null?httpOnly+";":"")+
					(secure!=null?secure+";":"")+
					(sameSite!=null?"SameSite="+sameSite+";":"")+
					"";
		}
		private String getDomainForCookie(String url){
			String r=url;
			if (r.matches("http.*://.*"))
				r=r.substring(r.indexOf("://")+"://".length());
			if (r.contains(":")) r=r.substring(0, r.lastIndexOf(":"));
			if (r.contains("/")) r=r.substring(0, r.indexOf("/"));
			if (IPv4_PATTERN.matcher(r).matches()){
				// do nothing
			}else{
				r=r.startsWith("www.")?r.substring(4):r;
				
				if (r.contains(".")){
					int lastDot=r.lastIndexOf(".");
					if (r.substring(0,lastDot).contains(".")){
						r=r.substring(r.substring(0,lastDot).lastIndexOf("."));
					}
				}
			}

			return r;
		}
	}
	
	
	@POST
	@Path("/login")
	public Response login(String payload) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException{
//		System.out.println("payload="+payload);
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
				
				
				
				Cookie cookie=new Cookie()
						.name("rh_cat_jwt")
						.value(jwtToken)
						.path("/")
						.domainUrl(uri.getRequestUri().toString())
//						.domainUrl(params.get("onSuccess"))
//						.domain(domainName)
//						.maxAge(60*60*24) // if unset it's a Session cookie
//						.maxAge("Session")
						.sameSite(SameSite.Lax)
//						.secure(true)
//						.build()
						;
				
//				System.out.println("requestUri="+uri.getRequestUri().toString());
//				System.out.println("cookie.domain="+cookie.domain);
//				System.out.println("onsuccess="+params.get("onSuccess"));
				
//				String cookie="rh_cat_jwt="+jwtToken+";Path=/;Domain="+domainName+";Max-Age="+(60*60)+"; SameSite=None;";
				
				System.out.println("RETURNING JWT COOKIE: "+cookie.build().replace(jwtToken, "${token}"));
				
				return Response.status(302)
						.location(new URI(params.get("onSuccess")))
//						.header("Access-Control-Allow-Origin", domainName)
//						.header("Access-Control-Allow-Credentials", "true")
//						.header("Access-Control-Allow-Methods", "GET, POST")
//						.header("Access-Control-Allow-Headers", "Content-Type, *")
//						.cookie(new NewCookie("rh_cat_jwt", jwtToken, "/", domainName, "__SAME_SITE_NONE__", 60*60 /*1hr*/, false, false))

						// TODO: need to make the cookie cross-domain so it works when accessed using a CNAME domain name
						.header("Set-Cookie", cookie.build())
						.build();
				
			}else{
				log.warn("AuthenticationController.login::Failure to authenticate user, returning to login screen with error 0 - "+params.get("onFailure"));
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
		return Response.status(302).location(new URI(onSuccess)).cookie(new NewCookie("rh_cat_jwt", null)) .build();
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
