package com.redhat.services.ae;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapBuilder<K, V>{
	private Map<K, V> values;
	public static <K, V> Map<K, V> newHashMap(){
		return new MapBuilder<K, V>().build();
	}
	public static <K, V> Map<K, V> newLinkedHashMap(){
		return new MapBuilder<K, V>(true).build();
	}
	public MapBuilder(){
		values=new HashMap<K, V>();
	}
	public MapBuilder(boolean retainInsertionOrder){
		values=new LinkedHashMap<K, V>();
	}
	public MapBuilder<K, V> put(K key, V value){
		values.put(key, value);
		return this;
	}
	public Map<K, V> build(){
		return values;
	}
}