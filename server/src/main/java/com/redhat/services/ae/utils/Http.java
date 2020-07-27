package com.redhat.services.ae.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;

public class Http{

	public static void post(String url, Map<String,String> headers, Map<String,String> queryParams, String data) throws IOException{
		HttpURLConnection con=null;
		try{

			URL myurl=new URL(url);
			con=(HttpURLConnection)myurl.openConnection();

			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			for(Entry<String, String> e:headers.entrySet()){
				con.setRequestProperty(e.getKey(), e.getValue());	
			}
			
			StringBuffer qp=new StringBuffer();
			for(Entry<String, String> e:queryParams.entrySet()){
				qp.append("&"+e.getKey()+"="+e.getValue());
			}
			qp.deleteCharAt(0);
			url+="?"+qp.toString();
			
			
			if (null!=data){
				try (DataOutputStream wr=new DataOutputStream(con.getOutputStream())){
					wr.write(data.getBytes());
				}
			}

			StringBuilder content;
			try (BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()))){

				String line;
				content=new StringBuilder();

				while ((line=br.readLine()) != null){
					content.append(line);
					content.append(System.lineSeparator());
				}
			}

			System.out.println(content.toString());

		}finally{
			if (null!=con)
				con.disconnect();
		}
	}
}
