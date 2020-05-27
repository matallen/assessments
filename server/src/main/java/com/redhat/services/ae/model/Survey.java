package com.redhat.services.ae.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.util.Lists;

//import org.bson.codecs.pojo.annotations.BsonProperty;
//import org.bson.types.ObjectId;

//import com.mongodb.BasicDBObject;
//import com.mongodb.DBObject;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.Utils;
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
//	public String theme;
//	public String externalUrl;
//	public String content;
	
	private Metrics metrics;
	public Metrics getMetrics(){
		if (null==metrics) metrics=new Metrics();
		return metrics;
	}
//	public void setMetrics(Metrics m){
//		this.metrics=m;
//	}
	
	public static List<Survey> findAll(){
		return Lists.newArrayList(Database.get().getSurveys().values());
	}
	public static Survey findById(String id){
//		return findById(new ObjectId(id));
//		if (null==id) System.err.println("findSurvey.ById():: ERROR!!!!! ID is NULL");
//		Database db=Database.get();
//		if (null==Database.get()) System.err.println("findSurvey.ById():: ERROR!!!!! Database.get() returned NULL");
//		Map<String, Survey> surveys=db.getSurveys();
//		if (null==Database.get().getSurveys()) System.err.println("findSurvey.ById():: ERROR!!!!! Database.get().getSurveys() returned NULL");
//		return surveys.get(id);
		return Database.get().getSurveys().get(id);
	}
	public void persist(){
		Database.get().getSurveys().put(id, this);
		Database.get().save();
	}
	public void update(){
		// should throw exception if it doesnt exist, but not necessary
		persist();
	}
	public void delete(){
		Database.get().getSurveys().remove(id);
		Database.get().save();
		// TODO: Delete file
  	File storage=new File(Database.STORAGE).getParentFile();
  	File questionsLocation=new File(storage, id+".json");
  	System.out.println("Removing questionnaire file:"+questionsLocation.getAbsolutePath() );
		questionsLocation.delete();
	}
	public Survey copy() throws FileNotFoundException, IOException{
		Survey o=new Survey();
		o.id=Utils.generateId();
		o.name="Copy of "+this.name;
		o.description=this.description;
		o.setQuestions(this.getQuestions());
		o.persist();
		return o;
	}
	
	@JsonIgnore
  public String getQuestions() throws FileNotFoundException, IOException{
  	File storage=new File(Database.STORAGE).getParentFile();
  	File questionsLocation=new File(storage, id+".json");
  	System.out.println("Loading from: "+questionsLocation.getAbsolutePath());
  	if (!questionsLocation.exists())
  		setQuestions("{}");
  	
//  	if (questionsLocation.exists()){
  		return IOUtils.toString(questionsLocation.exists()?new FileInputStream(questionsLocation.getAbsolutePath()):getClass().getClassLoader().getResourceAsStream(id), "UTF-8");
//  	}else{
//  		throw new FileNotFoundException("Can't find survey questions for "+id+" at "+questionsLocation.getAbsolutePath());
//  	}
  }
	@JsonIgnore
	public void setQuestions(String questionsJson) throws IOException{
  	File storage=new File(Database.STORAGE).getParentFile();
  	File questionsLocation=new File(storage, id+".json");
  	if (!questionsLocation.exists()){
  		questionsLocation.getParentFile().mkdirs();
  		questionsLocation.createNewFile();
		}
  	
  	// TODO: check the questions can be read as json / ie valid/readable json format?
  	
		IOUtils.write(questionsJson, new FileOutputStream(questionsLocation), "UTF-8");
	}
	
	
	
	
	public static Builder builder(){
		return new Builder();
	}
	public static class Builder extends Survey{
		public String getId(){return id;}                     public Builder id(String v){id=v; return this;}
		public String getName(){return name;}                 public Builder name(String v){name=v; return this;}
		public String getDescription(){return description;}   public Builder description(String v){description=v; return this;}
//		public String getTheme(){return theme;}               public Builder theme(String v){theme=v; return this;}
//		public String getExternalUrl(){return externalUrl;}   public Builder externalUrl(String v){externalUrl=v; return this;}
//		public String getContent(){return content;}           public Builder content(String v){content=v; return this;}
		
		public Survey build(){
			Survey c=new Survey();
			c.id=super.id;
			c.name=super.name;
			c.description=super.description;
//			c.theme=super.theme;
//			c.content=super.content;
			return c;
		}
		public Survey populate(Survey src, Survey dst){
			dst.id=src.id!=null?src.id:dst.id;
			dst.name=src.name!=null?src.name:dst.name;
			dst.description=src.description!=null?src.description:dst.description;
//			dst.theme=src.theme!=null?src.theme:dst.theme;
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
    sb.append("}");
    return sb.toString();
	}
}
