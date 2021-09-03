package com.redhat.services.ae;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.dt.GoogleDrive3_1;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class Initialization {
	private static Logger log=LoggerFactory.getLogger(Initialization.class);
	
	public void onStartup(@Observes StartupEvent e) {
		log.info("Starting up...");
		
		GoogleDrive3_1.initialise(GoogleDrive3_1.DriverType.gdrive, "v2.1.1PreRelease");
	}
	
	void onShutdown(@Observes ShutdownEvent e) {
		log.info("Shutting down...");
	}
	
	
}
