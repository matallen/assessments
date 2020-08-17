package com.redhat.services.ae.plugins.droolsscore;


public class DroolsRecommendation{
	protected String text;
	protected String section;
	protected String level1;
	protected String level2;
	public DroolsRecommendation(){}
	public DroolsRecommendation(String section, String level1, String text){
		this.section=section;
		this.level1=level1;
		this.level2="";
		this.text=text;
	}
	
	public DroolsRecommendation(String section, String level1, String level2, String text){
		this.section=section;
		this.level1=level1;
		this.level2=level2;
		this.text=text;
	}
	public String getText(){
		return text;
	}
	public String getSection(){
		return section;
	}
	public String getLevel1(){
		return level1;
	}
	public String getLevel2(){
		return level2;
	}
	public static RecommendationBuilder builder(){
		return new RecommendationBuilder();
	}
	public static class RecommendationBuilder extends DroolsRecommendation{
		public DroolsRecommendation build(){
			return new DroolsRecommendation(section, level1, level2, text);
		}
		public RecommendationBuilder text(String value){
			this.text=value; return this;
		}
		public RecommendationBuilder section(String value){
			this.level1=value; return this;
		}
	}
	public String toString(){
		return "Recommendation: "+section+"->"+level1+"->"+level2+"->"+text+"";
	}
}