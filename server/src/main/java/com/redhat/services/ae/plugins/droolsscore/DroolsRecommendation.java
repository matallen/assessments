package com.redhat.services.ae.plugins.droolsscore;


public class DroolsRecommendation{
	protected String text;
	protected String section;
	protected String level1="";
	protected String level2="";
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
	public String toString(){
		return this.getClass().getSimpleName()+": "+section+"->"+level1+"->"+level2+"->"+text+"";
	}
	
	public static class Builder extends DroolsRecommendation{
		public Builder text(String value)   { this.text   =value; return this; }
		public Builder section(String value){ this.section=value; return this; }
		public Builder level1(String value){ this.level1=value; return this; }
		public Builder level2(String value){ this.level2=value; return this; }
		public DroolsRecommendation build(){
			return new DroolsRecommendation(section, level1, level2, text);
		}
	}
	public void doKeyValueReplacement(String key, String value){
		text=text.replaceAll("\\$"+key, value);	
//		text=text.replaceAll("\\$\\{.+\\}", value);
//		text=text.replaceAll("\\{.+\\}", value);
	}
}