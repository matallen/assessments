package com.redhat.services.ae.charts;

import java.util.ArrayList;
import java.util.List;

public class PieData {
	private List<Integer> data;
	private List<String> backgroundColor;
	public List<Integer> getData(){
		if (data==null) data=new ArrayList<>();
		return data;
	}
	public List<String> getBackgroundColor(){
		if (backgroundColor==null) backgroundColor=new ArrayList<>();
		return backgroundColor;
	}
}
