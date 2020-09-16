package com.redhat.services.ae.model.storage;

public abstract class StorageFactory<T>{
	
	public abstract T createNewT();
	
	public EntityStorage<T> create(String filename){
		return new EntityStorage<T>(){
			@Override public T createNew(){
//				try{
					return createNewT();
//					return (T)cls.newInstance();
//				}catch (InstantiationException | IllegalAccessException e){
//					e.printStackTrace();
//					throw new RuntimeException("Unable to instantiate", e);
//				}
			}
			@Override public String getStorageFilename(){ return filename; }
		};
	}
	
//	@Deprecated
//	public static EntityStorage<Metrics> createMetricsStorage(){
//		return new EntityStorage<Metrics>(){
//			@Override public Metrics createNew(){ return new Metrics(); }
//			@Override public String getStorageFilename(){ return "metrics.json"; }
//		};
//	}
//	
//	@Deprecated
//	public static EntityStorage<Results> createResultsStorage(){
//		return new EntityStorage<Results>(){
//			@Override public Results createNew(){ return new Results(); }
//			@Override public String getStorageFilename(){ return "results.json"; }
//		};
//	}

}
