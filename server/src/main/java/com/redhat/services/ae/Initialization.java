package com.redhat.services.ae;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.dt.GoogleDrive3_1;
import com.redhat.services.ae.dt.GoogleDrive3_1.DriverType;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class Initialization {
	private static Logger log=LoggerFactory.getLogger(Initialization.class);
	
	public void onStartup(@Observes StartupEvent e) {
		log.info("Starting up...");
		
		GoogleDrive3_1.initialise(DriverType.gdrive, "v2.1.1PreRelease");
	}
	
	void onShutdown(@Observes ShutdownEvent e) {
		log.info("Shutting down...");
	}
	
	private static final int DEFAULT_CACHE_EXPIRY_IN_MS=10000;
	public static GoogleDrive3_1 newGoogleDrive(){
		return new GoogleDrive3_1(null!=System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")?Integer.parseInt(System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")):DEFAULT_CACHE_EXPIRY_IN_MS);
	}
}
