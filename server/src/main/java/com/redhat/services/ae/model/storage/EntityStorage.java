package com.redhat.services.ae.model.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.services.ae.utils.ChatNotification;
import com.redhat.services.ae.utils.ChatNotification.ChatEvent;
import com.redhat.services.ae.utils.Json;

public abstract class EntityStorage<T>{
	public static final Logger log=LoggerFactory.getLogger(EntityStorage.class);
	
	private long lastModified=-1;

	public File getStorageRoot(){
		return Surveys.getStorageRoot();
	}
	
	public abstract T createNew();
	public abstract String getStorageFilename();
	
//	private Map<String, T> data=new HashMap<>();
	public T data;
	
	public synchronized T load(){
		return load(null, true);
	}
	public synchronized T load(String objectId){
		return load(objectId, true);
	}
	public synchronized T load(String objectId, boolean createIfNotFound){
//		if (!data.containsKey(objectId)){
			
			// load from disk
//			File file=getStorage(getStorageRoot(), objectId, getStorageFilename());
			File loadFrom=new File(getStorageRoot(), null!=objectId?objectId+File.separator+getStorageFilename():getStorageFilename());
			if (null!=data && lastModified>=loadFrom.lastModified()) return data; // no need to read the file again, it's not been modified
			lastModified=loadFrom.lastModified();
			
			log.debug("Loading from (create? "+createIfNotFound+"): "+loadFrom.getAbsolutePath());
			
			if (loadFrom.exists()){
				try{
					String raw=IOUtils.toString(new FileInputStream(loadFrom), "UTF-8");
//				Map<String, T> oJson=Json.newObjectMapper(true).readValue(json, new TypeReference<Map<String,T>>(){});
					T oJson=Json.newObjectMapper(true).readValue(raw, new TypeReference<T>(){});
					data=oJson;
					
//					data.put(objectId, oJson);
				}catch(Exception e){
					e.printStackTrace();
					new ChatNotification().send(ChatEvent.onError, "Error ("+System.getenv("HOSTNAME")+"):: Investigate immediately! EntityStorage unable to load data - "+loadFrom);
					
					try{
						FileStore fs=Files.getFileStore(Paths.get(loadFrom.getAbsolutePath()));
						log.error("FileStore Name: "+ fs.name());
						long bytes = fs.getUsableSpace();
						long sizeInMB = bytes / (1024 * 1024);
						log.error("Usable Space: "+ sizeInMB +" MB");
					}catch (IOException e1){
						e1.printStackTrace();
					}

				}
			}else{
				
				if (createIfNotFound){
//					data.put(objectId, createNew());
					data=createNew();
				}else
					throw new RuntimeException("Entity '"+objectId+"' not found in file '"+loadFrom.getAbsolutePath()+"'");
			}
			
			return data;
//		}
//		return data.get(objectId);
	}
	
//	private File getStorage(String storageRoot, String objectId, String storage){
//		return new File(getStorageRoot(), objectId+File.separator+storage);
//	}

	public void save(){
		save(null);
	}
	public void save(String objectId){
		try{
			String json=Json.newObjectMapper(true).writeValueAsString(data);
			File saveTo=new File(getStorageRoot(), null!=objectId?objectId+File.separator+getStorageFilename():getStorageFilename());
			saveTo.getParentFile().mkdirs(); // ensure the parent exists
//			System.out.println("Saving to: "+saveTo.getAbsolutePath());
			IOUtils.write(json, new FileOutputStream(saveTo), "UTF-8");
			lastModified=saveTo.lastModified(); // save the modified date so when it's next accessed it doesnt force a reload after each save
		}catch(JsonMappingException sinkButDontStop){
			log.error("Unable to write metrics, printing trace but continuing - check the data structure: "+data);
			sinkButDontStop.printStackTrace();
		}catch(IOException sinkButDontStop){
			log.error("Unable to write metrics, printing trace but continuing - check the data structure: "+data);
			sinkButDontStop.printStackTrace();
		}
	}
}
