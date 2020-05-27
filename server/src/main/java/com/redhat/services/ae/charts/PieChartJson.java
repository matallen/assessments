package com.redhat.services.ae.charts;

import java.util.ArrayList;
import java.util.List;

public class PieChartJson{
  
	private List<PieData> datasets;
	private List<String> labels;
	public List<PieData> getDatasets(){
		if (null==datasets) datasets=new ArrayList<>();
		return datasets;
	}
	public List<String> getLabels(){
		if (null==labels) labels=new ArrayList<>();
		return labels;
	}
	
	
	
}
