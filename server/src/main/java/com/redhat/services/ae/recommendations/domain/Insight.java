package com.redhat.services.ae.recommendations.domain;

public class Insight{
	private String value;
	public Insight(String value){this.value=value;}
	public String getValue(){ return value;}
	public void setValue(String value){ this.value=value;}
	
	public static class builder{
		private String value;
		public builder value(String value){this.value=value;return this;}
		public Insight build(){
			return new Insight(value);
		}
	}
	public String toString(){
		return String.format(Insight.class.getSimpleName()+": value=%s", value);
	}
}
