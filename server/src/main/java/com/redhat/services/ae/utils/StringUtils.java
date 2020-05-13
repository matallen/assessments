package com.redhat.services.ae.utils;

public class StringUtils{

	public static boolean isBlank(final String str){
		if (str==null) return true;
		return str.trim().length()<=0;
	}
}
