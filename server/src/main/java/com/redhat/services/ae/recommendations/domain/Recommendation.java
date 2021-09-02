package com.redhat.services.ae.recommendations.domain;

import com.redhat.services.ae.plugins.droolsscore.DroolsRecommendation;

public class Recommendation extends DroolsRecommendation{
	
	public Recommendation(String section, String level1, String level2, String text){
		this.section=section;
		this.level1=level1;
		this.level2=level2;
		this.text=text;
	}
	public Recommendation(String section, String level1, String text){
		this.section=section;
		this.level1=level1;
		this.level2=null;
		this.text=text;
	}
//	public String getText(){return text;}
//	public String getSection(){return section;}
	public void setSection(String value){this.section=value;}
//	public String getLevel1(){return level1;}
	public void setLevel1(String value){this.level1=value;}
//	public String getLevel2(){return level2;}
	public void setLevel2(String value){this.level2=value;}

	public String toString(){
		return this.getClass().getSimpleName()+": section="+section+", level1="+level1+", level2="+level2+", text="+text+"";
	}
	
	public static class builder{
		private String section,level1,level2,text;
		public builder text(String value)   { this.text   =value; return this; }
		public builder section(String value){ this.section=value; return this; }
		public builder tab(String value){ this.section=value; return this; } // alias setter
		public builder level1(String value){ this.level1=value; return this; }
		public builder header1(String value){ this.level1=value; return this; } // alias setter
		public builder level2(String value){ this.level2=value; return this; }
		public builder header2(String value){ this.level2=value; return this; } // alias setter
		public Recommendation build(){
			return new Recommendation(section, level1, level2, text);
		}
	}
	
	public void doKeyValueReplacement(String key, String value){
		text=text.replaceAll("\\$"+key, value);	
//		text=text.replaceAll("\\$\\{.+\\}", value);
//		text=text.replaceAll("\\{.+\\}", value);
	}
}
