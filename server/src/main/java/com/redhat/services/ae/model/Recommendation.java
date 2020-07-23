package com.redhat.services.ae.model;


public class Recommendation{
	protected String text;
	protected String section;
	public Recommendation(){}
	public Recommendation(String section, String text){
		this.section=section;
		this.text=text;
	}
	public String getText(){
		return text;
	}
	public String getSection(){
		return section;
	}
	public static RecommendationBuilder builder(){
		return new RecommendationBuilder();
	}
	public static class RecommendationBuilder extends Recommendation{
		public Recommendation build(){
			return new Recommendation(section, text);
		}
		public RecommendationBuilder text(String value){
			this.text=value; return this;
		}
		public RecommendationBuilder section(String value){
			this.section=value; return this;
		}
	}
	public String toString(){
		return "Recommendation: section->"+section+", text->"+text+"";
	}
}