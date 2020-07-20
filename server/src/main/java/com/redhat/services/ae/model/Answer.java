package com.redhat.services.ae.model;

public class Answer{

	private String id;
	private String title;
	private int score;

	public Answer(String id, int score, String title){
		this.score=score;
		this.id=id;
		this.title=title;
	}
	
	public int getScore(){
		return score;
	}

	public void setScore(int score){
		this.score=score;
	}

	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id=id;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title=title;
	}
	
	
}
