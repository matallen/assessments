package com.redhat.services.ae.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.api.client.util.Lists;

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
	
	@JsonIgnore
	private EntityStorage<Map<String,Object>> resultsStorage=new StorageFactory<Map<String,Object>>(){
		@Override public Map<String, Object> createNewT(){ return new LinkedHashMap<>();
		}}.create("results.json");
	
	@JsonIgnore
	private EntityStorage<Map<String,Object>> metricsStorage=new StorageFactory<Map<String,Object>>(){
		@Override public Map<String, Object> createNewT(){ return new LinkedHashMap<>();
		}}.create("metrics.json");

	@JsonIgnore
	private EntityStorage<Map<String,Object>> questionsStorage=new StorageFactory<Map<String,Object>>(){
		@Override public Map<String,Object> createNewT(){ return new LinkedHashMap<>();
		}}.create("questions.json");
	
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
	public Map<String,Object> getQuestions(){
		return questionsStorage.load(this.id);
	}
	@JsonIgnore
	public String getQuestionsAsString() throws JsonProcessingException{
		return Json.toJson(questionsStorage.load(this.id));
	}
	public void setQuestionsAsString(String questionsJson) throws JsonParseException, JsonMappingException, IOException{
		Map<String,Object> toStore=Json.toObject(questionsJson, new TypeReference<Map<String,Object>>(){});
		questionsStorage.data=toStore;
	}
	public void setQuestions(Map<String,Object> questions){
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
	}
	public static Survey findById(String id){
		return Surveys.get().getSurveys().get(id);
	}
	public static List<Survey> findAll(){
		return Surveys.get().getSurveys().values().stream().collect(Collectors.toList());
	}
	
	public void delete(){
		Surveys.get().getSurveys().remove(this.id);
		Surveys.get().save();
		// TODO: Delete from surveys storage & the sub folder on the filesystem
		
		File toDelete=new File(Surveys.getStorageRoot(), this.id);
		System.out.println("Deleting survey: "+toDelete);
		try{
			FileUtils.forceDelete(toDelete);
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	private Map<String,Map<String,Object>> plugins;
	public Map<String,Map<String,Object>> getPlugins(){
		if (null==plugins) plugins=new LinkedHashMap<String, Map<String,Object>>();
		return plugins;
	}
	
	public void update(){
		// should throw exception if it doesnt exist, but not necessary
		persist();
	}
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
	
	
	class Resource{
		public Resource(String name, String path){
			this.name=name;
			this.path=path;
		}
		private String name; public String getName(){ return name; }
		private String path; public String getPath(){ return path; }
	}
	@JsonIgnore
	private File getResourcesRoot(){
		return new File(Surveys.getStorageRoot()+File.separator+id+File.separator+"resources");
	}
	public File getResource(String name){
		File result=new File(getResourcesRoot(), name);
		return result;
	}
	@JsonIgnore
	public List<Resource> getResources(){
		// list files in the survey/resources folder
		List<Resource> result=Lists.newArrayList();
		for (File f:getResourcesRoot().listFiles()){
			result.add(new Resource(f.getName(), "/api/surveys/"+id+"/resources/"+f.getName()));
//			result.add(new Resource(f.getName(), "/resources/"+f.getName()));
		}
		return result;
	}
	public void deleteResource(String filename){
		try{
			FileUtils.forceDelete(new File(getResourcesRoot(), filename));
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	public void addResource(String filename, InputStream fileStream){
		// upload file into survey/resources folder
		
		File destinationFile=new File(Surveys.getStorageRoot()+File.separator+id+File.separator+"resources"+File.separator+filename);
		destinationFile.getParentFile().mkdirs();
		
		System.out.println("saving resource to: "+destinationFile.getAbsolutePath());
		
		try(OutputStream out=new FileOutputStream(destinationFile)){
		    IOUtils.copy(fileStream, out);
		} catch (FileNotFoundException e) {
		    // handle exception here
			e.printStackTrace();
		} catch (IOException e) {
		    // handle exception here
			e.printStackTrace();
		}
		
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
