package com.redhat.services.ae;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class Initialization {
	private static Logger log=LoggerFactory.getLogger(Initialization.class);
	
	void onStartup(@Observes StartupEvent e) {
		log.info("Starting up...");
	}
	
	void onShutdown(@Observes ShutdownEvent e) {
		log.info("Shutting down...");
	}
	
	
}
