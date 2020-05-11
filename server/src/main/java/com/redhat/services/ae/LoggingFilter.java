package com.redhat.services.ae;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter {
    private static final Logger log = Logger.getLogger(LoggingFilter.class);
    @Context UriInfo info;
    
    @Override
    public void filter(ContainerRequestContext context) {
        log.infof("[@%s] %s", context.getMethod(), info.getPath());
    }
}
