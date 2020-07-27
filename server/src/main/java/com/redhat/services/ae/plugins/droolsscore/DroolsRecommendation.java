package com.redhat.services.ae.plugins.droolsscore;


public class DroolsRecommendation{
	protected String text;
	protected String section;
	public DroolsRecommendation(){}
	public DroolsRecommendation(String section, String text){
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
	public static class RecommendationBuilder extends DroolsRecommendation{
		public DroolsRecommendation build(){
			return new DroolsRecommendation(section, text);
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