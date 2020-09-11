package com.redhat.services.ae;

import java.security.Principal;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter {
    private static final Logger log = Logger.getLogger(LoggingFilter.class);
    @Context UriInfo info;
    @Inject JsonWebToken jwt;
    @Context SecurityContext ctx;
    
    @Override
    public void filter(ContainerRequestContext context) {
    	if (!info.getPath().contains("/login")) // so we dont log our passwords!
    		log.infof("[@%s] %s (user:%s, jwtRoles: %s)", context.getMethod(), info.getPath(), (null!=ctx.getUserPrincipal()?ctx.getUserPrincipal().getName():"anonymous"), jwt.getClaimNames()!=null?jwt.getClaim("groups"):"No Jwt provided");
  		
      
//      System.out.println("principal="+(null!=ctx.getUserPrincipal()?ctx.getUserPrincipal().getName():"anonymous"));
//  		System.out.println("jwt.name="+jwt.getName());
//  		System.out.println("jwt.claimNames="+jwt.getClaimNames());

//      Principal userPrincipal=context.getSecurityContext().getUserPrincipal();
//      String user=null!=userPrincipal?userPrincipal.getName():null;
//      
//      Cookie jwtCookie=context.getCookies().get("rhjwt");
//      String jwt=null!=jwtCookie?jwtCookie.getValue():null;
//      
//      System.out.println("LoggingFilter:: principal="+user+", jwtCookie="+jwt);
      
//      context.
      
    }
}
