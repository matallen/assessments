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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;

//import org.codehaus.jackson.JsonGenerationException;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.annotate.JsonIgnore;
//import org.codehaus.jackson.map.JsonMappingException;


public class Database{
  private static final Logger log=Logger.getLogger(Database.class.getSimpleName());
  public static String STORAGE="persistence/database.json";
  public static final File STORAGE_AS_FILE=new File(STORAGE);
  
  private Map<String, Survey> surveys;
  private Map<String, List<String>> visitors;
  
  public Map<String, List<String>> getVisitors(){
  	if (null==visitors) visitors=new HashMap<>();
  	return visitors;
  }
  public List<String> getVisitors(String YYMMM){
  	if (!getVisitors().containsKey(YYMMM))
  		getVisitors().put(YYMMM, new ArrayList<>());
  	return getVisitors().get(YYMMM);
  }
  
//  private Map<String, Map<String, String>> users;
//  private List<Map<String, String>> events;
  
  private String created;
  private String version;
  static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  static SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
  
  public Database(){
    created=sdf.format(new Date());
  }
  public String getCreated(){ return created; }
  public String getVersion(){ return version; }
  public void setVersion(String version){
  	this.version=version;
  }
  
  
  public Map<String, Survey> getSurveys(){
  	if (null==surveys) surveys=new HashMap<>();
  	return surveys;
  };
  
  
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
      log.info("Database saved ("+(System.currentTimeMillis()-s)+"ms, size="+storeHere.length()+")");
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static synchronized Database load(){
    try{
      log.info("Database loading (size="+new File(STORAGE).length()+", location="+new File(STORAGE).getAbsolutePath()+")");
//      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(STORAGE))), Database.class);
      Database db=Json.toObject(IOUtils.toString(new FileInputStream(new File(STORAGE)), "UTF-8"), Database.class);
      return db;
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    log.severe("Returning NULL Database - this is cause issues!");
    return null;
  }
  
  private static Database instance=null;
  public static Database get(){
    return get(new File(STORAGE));
  }
  public static synchronized Database get(File storage){
    if (instance!=null) return instance;
    if (!new File(STORAGE).exists()){
    	log.warning("No database file found, creating new/blank/default one... "+new File(STORAGE).getAbsolutePath());
    	new Database().save();
    }
    instance=Database.load();
    log.info("Loading/Replaced 'instance' of database in memory (loaded ok?="+(null==instance?"NO! it's NULL!!":"Yes")+")");
    
    return instance;
  }
  public static void reset(){
  	instance=null;
  }
  
}
