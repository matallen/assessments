package com.redhat.services.ae.model.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class Surveys{
	public static final Logger log=LoggerFactory.getLogger(Surveys.class);
  private Map<String, Survey> surveys;

  public Map<String, Survey> getSurveys(){
  	if (null==surveys) surveys=new HashMap<>();
  	return surveys;
  };
  
  private static String STORAGE_FILE="surveys.json";
	public static String STORAGE_ROOT=null;
	public static File getStorageRoot(){
		if (null==STORAGE_ROOT){
			try{
//				System.out.println("STORAGE_ROOT is null, loading...");
				STORAGE_ROOT=ConfigProvider.getConfig().getValue("storage.root", String.class);
//				System.out.println("STORAGE_ROOT (from app properties) = "+STORAGE_ROOT);
			}catch(NoSuchElementException e){
				e.printStackTrace();
				if (null==STORAGE_ROOT) STORAGE_ROOT="target/persistence";
				log.warn("STORAGE_ROOT (from exception clause) = "+STORAGE_ROOT);
			}
		}
		return new File(STORAGE_ROOT);
	}
  
  public synchronized void save(){
    save(new File(getStorageRoot(), STORAGE_FILE));
  }
  public synchronized void save(File storeHere){
    try{
      long s=System.currentTimeMillis();
      if (!storeHere.getParentFile().exists())
        storeHere.getParentFile().mkdirs();
      IOUtils.write(Json.toJson(this).getBytes(), new FileOutputStream(storeHere));
      log.debug("Database saved ("+(System.currentTimeMillis()-s)+"ms, size="+storeHere.length()+")");
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  
  
  
  private static Surveys instance=null;
  private static synchronized Surveys load(){
  	return load(new File(getStorageRoot(), STORAGE_FILE));
  }
  private static synchronized Surveys load(File storage){
    try{
      log.info("Database loading (size="+storage.length()+", location="+storage.getAbsolutePath()+")");
//      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(STORAGE))), Database.class);
      Surveys db=Json.toObject(IOUtils.toString(new FileInputStream(storage), "UTF-8"), Surveys.class);
      return db;
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    log.error("Returning NULL Database - this is cause issues!");
    return null;
  }
  
  public static Surveys get(){
    return get(new File(getStorageRoot(), STORAGE_FILE));
  }
  public static synchronized Surveys get(File storage){
    if (instance!=null) return instance;
    if (!storage.exists()){
    	log.warn("No database file found, creating new/blank/default one... "+storage.getAbsolutePath());
    	new Surveys().save();
    }
    instance=Surveys.load(storage);
    log.info("Loading/Replaced 'instance' of database in memory (loaded ok?="+(null==instance?"NO! it's NULL!!":"Yes")+")");
    
    return instance;
  }
  public static void reset(){
  	instance=null;
  }
  
}
