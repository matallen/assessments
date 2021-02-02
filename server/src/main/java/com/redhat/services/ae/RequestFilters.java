package com.redhat.services.ae;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.http.runtime.filters.Filters;

@ApplicationScoped
public class RequestFilters{
	public static final Logger log=LoggerFactory.getLogger(RequestFilters.class);
	
	/**
	 * Fixes https://github.com/quarkusio/quarkus/issues/6096
	 */
	public void filters(@Observes final Filters filters){
		filters.register(rc->{
			boolean removeAuth=false;
			
			if (rc.request().path().contains("/onPageChange"))   removeAuth=true;
			if (rc.request().path().contains("/generateReport")) removeAuth=true;
			if (rc.request().path().contains("/results/"))       removeAuth=true;
			if (rc.request().path().contains("/config"))         removeAuth=true;
			if (rc.request().path().contains("/geoInfo"))        removeAuth=true;
			if (rc.request().path().contains("/customfield/"))   removeAuth=true;
			
			
			if ("GET".equals(rc.request().method().name())){
				if (rc.request().path().contains("/resources/"))   removeAuth=true;
				if (rc.request().path().contains("/questions/"))   removeAuth=true;
			}
			
			if (removeAuth){
				log.info("Removing Auth header for: ["+rc.request().method().name()+"]"+rc.request().path());
				rc.request().headers().remove("Authorization");
			}
				
			rc.next();
		}, 9000);
	}
	
}
