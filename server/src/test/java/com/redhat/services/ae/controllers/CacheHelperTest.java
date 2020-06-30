package com.redhat.services.ae.controllers;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.github.benmanes.caffeine.cache.Cache;
import com.redhat.services.ae.utils.CacheHelper;

public class CacheHelperTest{
	int initSize=10;
	int maxSize=10;
	int expiryInSec=10;
	
	
	@Test
	public void test1(){
		Cache<String, String> cache=new CacheHelper<String>().getCache("testCache", initSize, maxSize, expiryInSec);
		
		cache.put("id1", "123");
		Map<String, String> map=cache.asMap();
		for(Entry<String, String> k:map.entrySet()){
			System.out.println(String.format("Test1:: k=%s,v=%s", k.getKey(), k.getValue()));
		}
		
	}
	@Test
	public void test2(){
		Cache<String, String> cache=new CacheHelper<String>().getCache("testCache", initSize, maxSize, expiryInSec);
		Map<String, String> map=cache.asMap();
		for(Entry<String, String> k:map.entrySet()){
			System.out.println(String.format("Test2:: k=%s,v=%s", k.getKey(), k.getValue()));
		}
	}
}
