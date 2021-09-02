package com.redhat.services.ae.plugins;

import org.kie.api.builder.Message;
import org.kie.api.builder.Results;

public class DroolsCompilationException extends Exception{
	private Results errors;
	public DroolsCompilationException(Results errors){
		this.errors=errors;
	}
	public Results getErrors(){
		return errors;
	}
	public String getMessage(){
		StringBuffer sb=new StringBuffer();
		for (Message error:errors.getMessages()) sb.append(error+"\n");
		return sb.toString();
	}
	
}
