package com.redhat.services.ae.charts;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChartJson {
  private Set<String> labels=new LinkedHashSet<String>();
  private List<DataSet> datasets=new ArrayList<DataSet>();
  
  public Set<String> getLabels() {
    return labels;
  }
  public void setLabels(Set<String> labels) {
    this.labels=labels;
  }
  public List<DataSet> getDatasets() {
    return datasets;
  }
  public void setDatasets(List<DataSet> datasets) {
    this.datasets=datasets;
  }
  public DataSet addNewDataSet(){
  	datasets.add(new DataSet());
  	return datasets.get(datasets.size()-1);
  }
  
  
}
