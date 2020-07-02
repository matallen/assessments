package com.redhat.services.ae.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.ConfigProvider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class CacheHelper<T>{
	
	public static Map<String, String> cache=new HashMap<>();
	
	
	private static Map<String, Cache> caches=new HashMap<String, Cache>();
	
	
	public Cache<String, T> getCache(String cacheId){
		Optional<Integer> initialCapacity=ConfigProvider.getConfig().getOptionalValue("quarkus.cache.caffeine."+cacheId+".initial-capacity", Integer.class);
		Optional<Integer> maxSize=ConfigProvider.getConfig().getOptionalValue("quarkus.cache.caffeine."+cacheId+".maximum-size", Integer.class);
		Optional<Long> expiryInSeconds=ConfigProvider.getConfig().getOptionalValue("quarkus.cache.caffeine."+cacheId+".expire-after-write", Long.class);
		return getCache(cacheId, initialCapacity.get(), maxSize.get(), expiryInSeconds.get());
	}

	
	public Cache<String, T> getCache(String cacheId, int initialCapacity, int maxSize, long expiryInSeconds){
		
		if (!caches.containsKey(cacheId)){
			System.out.println("Creating a NEW Cache for : "+cacheId);
			caches.put(cacheId, Caffeine.newBuilder()
					.expireAfterWrite(expiryInSeconds, TimeUnit.SECONDS)
					.initialCapacity(initialCapacity)
					.maximumSize(maxSize)
					.build());
		}
		
		return caches.get(cacheId);
	}

}
