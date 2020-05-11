package com.redhat.services.ae.model;

//import java.util.Objects;
//
//import org.bson.codecs.pojo.annotations.BsonProperty;
//import org.bson.types.ObjectId;
//
//import com.mongodb.BasicDBObject;
//import com.mongodb.DBObject;
//import com.redhat.services.ae.Utils;
//
//import io.quarkus.mongodb.panache.MongoEntity;
//import io.quarkus.mongodb.panache.PanacheMongoEntity;
//import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
//import io.quarkus.mongodb.panache.PanacheQuery;
//
//@MongoEntity(collection="Surveys")
//public class SurveyMongo extends PanacheMongoEntity{
//	public String id; // eek - will this clash with the mongo/panache id field?
//	public String name;
//	public String description;
//	public String theme;
//	public String externalUrl;
//	
//	public static Survey findById(String id){
//		return findById(new ObjectId(id));
//	}
//	public static Builder builder(){
//		return new Builder();
//	}
//	public static class Builder extends Survey{
//		public String getId(){return id;}                     public Builder id(String v){id=v; return this;}
//		public String getName(){return name;}                 public Builder name(String v){name=v; return this;}
//		public String getDescription(){return description;}   public Builder description(String v){description=v; return this;}
//		public String getTheme(){return theme;}               public Builder theme(String v){theme=v; return this;}
//		public String getExternalUrl(){return externalUrl;}   public Builder externalUrl(String v){externalUrl=v; return this;}
//		public Survey build(){
//			Survey c=new Survey();
//			c.id=super.id;
//			c.name=super.name;
//			c.description=super.description;
//			c.theme=super.theme;
//			c.externalUrl=super.externalUrl;
//			return c;
//		}
//		public Survey populate(Survey src, Survey dst){
//			dst.id=src.id;
//			dst.name=src.name;
//			dst.description=src.description;
//			dst.theme=src.theme;
//			dst.externalUrl=src.externalUrl;
//			return dst;
//		}
//	}
//	
//	@Override
//	public boolean equals(Object obj) {
//		if (!(obj instanceof Survey)) 
//			return false;
//		Survey other = (Survey) obj;
//		return Objects.equals(other.id, this.id);
//	}
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(this.id);
//	}
//	
//	public String toString(){
//    StringBuilder sb = new StringBuilder();
//    sb.append(this.getClass().getSimpleName()+"{");
//    sb.append("id:").append(Utils.toIndentedString(id));
//    sb.append(", name:").append(Utils.toIndentedString(name));
//    sb.append(", description:").append(Utils.toIndentedString(description));
//    sb.append("}");
//    return sb.toString();
//	}
//}
