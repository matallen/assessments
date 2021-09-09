package com.redhat.services.ae.utils;

import java.text.DecimalFormat;

public class TimeUtils{

  static int[] s=new int[]{1000,60,60,24,365};
  static String[] d=new String[]{"ms","s","m","h","d"};
  public static String msToSensibleString(long ms){
  	double result=ms;
  	String denomination=d[0];
  	for(int i=0;i<=s.length;i++){
  		if (result>=s[i]){
  			result=result/(double)s[i];
  			denomination=d[i+1];
  		}else
  			break;
  	}
  	return new DecimalFormat("##.###").format(result)+denomination;
  }
}
