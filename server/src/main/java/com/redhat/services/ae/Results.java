package com.redhat.services.ae;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

public class Results{
  private static final Logger log=Logger.getLogger(Results.class.getSimpleName());
  public static String STORAGE="persistence/results.json";
  public static final File STORAGE_AS_FILE=new File(STORAGE);
  
  private Map<String, String> results;
  
  public Map<String, String> getResults(){
  	if (null==results) results=new HashMap<>();
  	return results;
  }
  
  private String created;
  private String version;
  static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  public Results(){
    created=sdf.format(new Date());
  }
  public String getCreated(){ return created; }
  public String getVersion(){ return version; }
  public void setVersion(String version){
  	this.version=version;
  }
  
  
  public synchronized void save(){
    save(new File(STORAGE));
  }
  public synchronized void save(File storeHere){
    try{
      long s=System.currentTimeMillis();
      if (!storeHere.getParentFile().exists())
        storeHere.getParentFile().mkdirs();
//      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsBytes(this), new FileOutputStream(storeHere));
      IOUtils.write(Json.toJson(this).getBytes(), new FileOutputStream(storeHere));
      log.info(Results.class.getSimpleName()+" saved ("+(System.currentTimeMillis()-s)+"ms, size="+storeHere.length()+")");
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static synchronized Results load(){
    try{
      log.info(Results.class.getSimpleName()+" loading (size="+new File(STORAGE).length()+", location="+new File(STORAGE).getAbsolutePath()+")");
//      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(STORAGE))), Database.class);
      Results db=Json.toObject(IOUtils.toString(new FileInputStream(new File(STORAGE)), "UTF-8"), Results.class);
      return db;
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    log.severe("Returning NULL Results - this is cause issues!");
    return null;
  }
  
  private static Results instance=null;
  public static Results get(){
    return get(new File(STORAGE));
  }
  public static synchronized Results get(File storage){
    if (instance!=null) return instance;
    if (!new File(STORAGE).exists()){
    	log.warning("No results file found, creating new/blank/default one... "+new File(STORAGE).getAbsolutePath());
    	new Results().save();
    }
    instance=Results.load();
    log.info("Loading/Replaced 'instance' of results in memory (loaded ok?="+(null==instance?"NO! it's NULL!!":"Yes")+")");
    
    return instance;
  }
  public static void reset(){
  	instance=null;
  }
  
}
