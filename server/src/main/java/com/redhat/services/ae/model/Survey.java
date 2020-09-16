package com.redhat.services.ae.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;

//import org.bson.codecs.pojo.annotations.BsonProperty;
//import org.bson.types.ObjectId;

//import com.mongodb.BasicDBObject;
//import com.mongodb.DBObject;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.Utils;
import com.redhat.services.ae.model.storage.EntityStorage;
import com.redhat.services.ae.model.storage.StorageFactory;
import com.redhat.services.ae.model.storage.Surveys;
import com.redhat.services.ae.utils.Json;

//import io.quarkus.mongodb.panache.MongoEntity;
//import io.quarkus.mongodb.panache.PanacheMongoEntity;
//import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
//import io.quarkus.mongodb.panache.PanacheQuery;

//@MongoEntity(collection="Surveys")
public class Survey{
	public String id; // eek - will this clash with the mongo/panache id field?
	public String name;
	public String description;
	public String owner;
	public String theme;
	
//	private EntityStorage<Metrics> getMetricsStorage(){
//		return new EntityStorage<Metrics>(){
//			@Override public Metrics createNew(){ return new Metrics(); }
//			@Override public String getStorageLocation(){ return "metrics"; }
//		};
//	}
	
	@JsonIgnore
	private EntityStorage<Map<String,Object>> resultsStorage=new StorageFactory<Map<String,Object>>(){
		@Override public Map<String, Object> createNewT(){ return new LinkedHashMap<>();
		}}.create("results.json");
	
	@JsonIgnore
	private EntityStorage<Map<String,Object>> metricsStorage=new StorageFactory<Map<String,Object>>(){
		@Override public Map<String, Object> createNewT(){ return new LinkedHashMap<>();
		}}.create("metrics.json");

	@JsonIgnore
	private EntityStorage<String> questionsStorage=new StorageFactory<String>(){
		@Override public String createNewT(){ return "{}";
		}}.create("questions.json");
		
		
//	@JsonIgnore
//	private static EntityStorage<Map<String,Survey>> surveysStorage=new StorageFactory<Map<String,Survey>>(){
//		@Override public Map<String,Survey> createNewT(){ return new HashMap<>();
//		}}.create("surveys.json");
	
	

//	private Persister<Map<String,Object>> metricsStorage=new Persister<Map<String,Object>>(this.id, "metrics.json");
	
	
	
//	class Persister<T>{
//		private String surveyId;
//		private String filename;
//		public Persister(String surveyId, String filename){}
//		public T get(){
//			File file=getStorage(getStorageRoot(), surveyId+File.separator+filename);
//			if (file.exists()){
//				try{
//					String json=IOUtils.toString(new FileInputStream(file), "UTF-8");
//					return Json.newObjectMapper(true).readValue(json, new TypeReference<T>(){});
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}else{
//				return null;
//			}
//		}
//		public void save(T obj){
//			try{
//				String json=Json.newObjectMapper(true).writeValueAsString(obj);
//				File saveTo=getStorage(getStorageRoot(), surveyId+File.separator+filename);
//				saveTo.getParentFile().mkdirs(); // ensure the parent exists
//				System.out.println("Saving to: "+saveTo.getAbsolutePath());
//				IOUtils.write(json, new FileOutputStream(saveTo), "UTF-8");
//			}catch(IOException e){
//				e.printStackTrace();
//			}
//		}
//	}
//	
	
//	private Map<String,Object> metrics;
//	public Map<String,Object> getMetrics2(boolean createIfNotFound){
//		if (null==metrics){
//			metrics=new Persister<Map<String,Object>>(this.id, "metrics.json").get();
//			if (metrics==null) metrics=new LinkedHashMap<>();
//		}
//		return metrics;
//	}
	
	
	
	@JsonIgnore
	public Map<String,Object> getMetrics(){
		return metricsStorage.load(this.id);
	}
	public void saveMetrics(){
		metricsStorage.save(this.id);
	}
	public void clearMetrics(){
		metricsStorage.load(this.id).clear();
		metricsStorage.save(this.id);
		metricsStorage.data=null;
	}
	
	@JsonIgnore
	public Map<String,Object> getResults(){
		return resultsStorage.load(this.id);
	}
	public void saveResults(){
		resultsStorage.save(this.id);
	}
	
	@JsonIgnore
	public String getQuestions(){
		return questionsStorage.load(this.id);
	}
	public void setQuestions(String questions){
		questionsStorage.data=questions;
	}
	public void saveQuestions(){
		questionsStorage.save(this.id);
	}
	
	public void persist(){
		save();
	}
	public void save(){
		
		Surveys.get().getSurveys().put(this.id, this);
		Surveys.get().save();
//		surveysStorage.load();
//		surveysStorage.data.put(this.id, this);
//		surveysStorage.save();
	}
	public static Survey findById(String id){
		return Surveys.get().getSurveys().get(id);
//		surveysStorage.load();
//		Survey survey=surveysStorage.data.get(id);
//		return survey;
	}
	public static List<Survey> findAll(){
//		surveysStorage.load();
//		return surveysStorage.data.values().stream().collect(Collectors.toList());
		return Surveys.get().getSurveys().values().stream().collect(Collectors.toList());
	}
	
	public void delete(){
		Surveys.get().getSurveys().remove(this.id);
		Surveys.get().save();
//		surveysStorage.load();
//		surveysStorage.data.remove(this.id);
//		surveysStorage.save();
		// TODO: Delete from surveys storage & the sub folder on the filesystem
		
		File toDelete=new File(Surveys.getStorageRoot(), this.id);
		System.out.println("Deleting survey: "+toDelete);
		try{
			FileUtils.forceDelete(toDelete);
		}catch (IOException e){
			e.printStackTrace();
		}
//  	File storage=new File(Database.STORAGE).getParentFile();
//  	File questionsLocation=new File(storage, id+".json");
//  	System.out.println("Removing questionnaire file:"+questionsLocation.getAbsolutePath() );
//		questionsLocation.delete();
	}
	
	
//	public static Survey findById(String id){
//		Database db=Database.get();
//		if (null==db) System.err.println("Database.get() returned NULL!!!!!!");
//		Map<String, Survey> surveys=db.getSurveys();
//		if (null==surveys) System.err.println("Database.get().getSurveys() returned NULL!!!!!!");
//		Survey survey=surveys.get(id);
//		if (null==survey) System.err.println("Database.get().getSurveys().get('"+id+"') returned NULL!!!!!!");
//		return survey;
//	}
//	public static List<Survey> findAll(){
//		return Database.get().getSurveys().values().stream().collect(Collectors.toList());
//	}
	
	private Map<String,Map<String,Object>> plugins;
	public Map<String,Map<String,Object>> getPlugins(){
		if (null==plugins) plugins=new LinkedHashMap<String, Map<String,Object>>();
		return plugins;
	}
	
//	public static List<Survey> findAll(){
//		return Database.get().getSurveys().values().stream().collect(Collectors.toList());
//	}
//	public static Survey findById(String id){
//		EntityStorage<Survey> db2=new StorageFactory<Survey>().create(Survey.class, "survey.json");
//		Survey s2=db2.get(id, false);
//		return s2;
//		
//		Database db=Database.get();
//		if (null==db) System.err.println("Database.get() returned NULL!!!!!!");
//		Map<String, Survey> surveys=db.getSurveys();
//		if (null==surveys) System.err.println("Database.get().getSurveys() returned NULL!!!!!!");
//		Survey survey=surveys.get(id);
//		if (null==survey) System.err.println("Database.get().getSurveys().get('"+id+"') returned NULL!!!!!!");
//		return survey;
////		return Database.get().getSurveys().get(id);
//	}
//	public void persist(){
//		Database.get().getSurveys().put(id, this);
//		Database.get().save();
//	}
	public void update(){
		// should throw exception if it doesnt exist, but not necessary
		persist();
	}
//	public void delete(){
//		Database.get().getSurveys().remove(id);
//		Database.get().save();
//		// TODO: Delete file
//  	File storage=new File(Database.STORAGE).getParentFile();
//  	File questionsLocation=new File(storage, id+".json");
//  	System.out.println("Removing questionnaire file:"+questionsLocation.getAbsolutePath() );
//		questionsLocation.delete();
//	}
	public Survey copy() throws FileNotFoundException, IOException{
		Survey o=new Survey();
		o.id=Utils.generateId();
		o.name="Copy of "+this.name;
		o.description=this.description;
		o.owner=this.owner;
		o.theme=this.theme;
		o.setQuestions(this.getQuestions());
		o.persist();
		return o;
	}
	
//	@JsonIgnore
//  public String getQuestions() throws FileNotFoundException, IOException{
//  	File storage=new File(Database.STORAGE).getParentFile();
//  	File questionsLocation=new File(storage, id+".json");
//  	System.out.println("Loading from: "+questionsLocation.getAbsolutePath());
//  	if (!questionsLocation.exists())
//  		setQuestions("{}");
//  	
////  	if (questionsLocation.exists()){
//  		return IOUtils.toString(questionsLocation.exists()?new FileInputStream(questionsLocation.getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(id), "UTF-8");
////  	}else{
////  		throw new FileNotFoundException("Can't find survey questions for "+id+" at "+questionsLocation.getAbsolutePath());
////  	}
//  }
//	@JsonIgnore
//	public void setQuestions(String questionsJson) throws IOException{
//  	File storage=new File(Database.STORAGE).getParentFile();
//  	File questionsLocation=new File(storage, id+".json");
//  	if (!questionsLocation.exists()){
//  		questionsLocation.getParentFile().mkdirs();
//  		questionsLocation.createNewFile();
//		}
//  	
//  	// TODO: check the questions can be read as json / ie valid/readable json format?
//  	
//		IOUtils.write(questionsJson, new FileOutputStream(questionsLocation), "UTF-8");
//	}
	
	@JsonIgnore
	public Map<String,Map<String,Object>> getActivePlugins() throws IOException{
		Map<String,Map<String,Object>> result=new LinkedHashMap<>();
		for(Entry<String, Map<String, Object>> e:getPlugins().entrySet()){
			if (e.getValue().containsKey("active") && (Boolean)e.getValue().get("active")){
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}
	
	
	
	public static Builder builder(){
		return new Builder();
	}
	public static class Builder extends Survey{
		public String getId(){return id;}                     public Builder id(String v){id=v; return this;}
		public String getName(){return name;}                 public Builder name(String v){name=v; return this;}
		public String getDescription(){return description;}   public Builder description(String v){description=v; return this;}
		public String getOwner(){return owner;}						    public Builder owner(String v){owner=v; return this;}
		public String getTheme(){return theme;}               public Builder theme(String v){theme=v; return this;}
//		public String getExternalUrl(){return externalUrl;}   public Builder externalUrl(String v){externalUrl=v; return this;}
//		public String getContent(){return content;}           public Builder content(String v){content=v; return this;}
		
		public Survey build(){
			Survey c=new Survey();
			c.id=super.id;
			c.name=super.name;
			c.description=super.description;
			c.owner=super.owner;
			c.theme=super.theme;
//			c.content=super.content;
			return c;
		}
		public Survey populate(Survey src, Survey dst){
			dst.id=src.id!=null?src.id:dst.id;
			dst.name=src.name!=null?src.name:dst.name;
			dst.description=src.description!=null?src.description:dst.description;
			dst.owner=src.owner!=null?src.owner:dst.owner;
			dst.theme=src.theme!=null?src.theme:dst.theme;
//			dst.content=src.content!=null?src.content:dst.content;
			return dst;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Survey)) 
			return false;
		Survey other = (Survey) obj;
		return Objects.equals(other.id, this.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}
	
	public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName()+"{");
    sb.append("id:").append(Utils.toIndentedString(id));
    sb.append(", name:").append(Utils.toIndentedString(name));
    sb.append(", description:").append(Utils.toIndentedString(description));
    sb.append(", owner:").append(Utils.toIndentedString(owner));
    sb.append(", theme:").append(Utils.toIndentedString(theme));
    sb.append("}");
    return sb.toString();
	}
}
