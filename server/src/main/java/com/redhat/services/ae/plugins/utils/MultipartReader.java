package com.redhat.services.ae.plugins.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.google.common.base.Splitter;


public class MultipartReader{
	private String htmlFileInputName;
	public MultipartReader(String htmlFileInputName){
		this.htmlFileInputName=htmlFileInputName;
	}
	
// Ideally this should return a list of files and fields separately as a map, but this will do for our needs now 
	public MultipartReaderResult process(MultipartFormDataInput data){
		MultipartReaderResult result=new MultipartReaderResult();
	  for (InputPart inputPart:data.getFormDataMap().get(htmlFileInputName)) {
	    try {
	      // Get the filename out of the Content-Disposition header
	      String[] contentDispositionHeader=inputPart.getHeaders().getFirst("Content-Disposition").split(";");
	      for (String name:contentDispositionHeader) {
	        if ((name.trim().startsWith("filename"))) {
	        	Map<String,String> filenameInfo=Splitter.on(",").trimResults().withKeyValueSeparator("=").split(name);
//	        	fileName=filenameInfo.get("filename");
	        	result.filename=filenameInfo.get("filename");
	        	if (null!=result.filename) result.filename=result.filename.replaceAll("\"", "");
	        }
	      }

	      // Get the file body as a stream
//	      InputStream istream = inputPart.getBody(InputStream.class,null);
	      result.fileStream=inputPart.getBody(InputStream.class,null);

      }catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
	  return result;
	}
	
	public class MultipartReaderResult{
		private String filename; public String getFilename(){return filename;}
		private InputStream fileStream; public InputStream getFileStream(){return fileStream;} 
	}
}
