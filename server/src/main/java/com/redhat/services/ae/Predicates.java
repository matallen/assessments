package com.redhat.services.ae;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Predicate;

public class Predicates{

	public static final Predicate<String> allNonReportFields=new Predicate<String>(){public boolean apply(@Nullable String str){
		return !"_sectionScore".equals(str) && !str.startsWith("_report");
	}};
	
}
