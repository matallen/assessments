package com.redhat.services.ae.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.utils.Json;

public class RemoveReportPropertiesPluginTest{

	@Test
	public void testRemoveFieldsBasedOnRegEx() throws Exception{
		String answersJson=IOUtils.toString(this.getClass().getClassLoader().getResource("test_2_answers.json"), "UTF-8");
		Map<String,Object> answers=Json.toObject(answersJson, new TypeReference<HashMap<String,Object>>(){});
		
		RemoveReportPropertiesPlugin p=new RemoveReportPropertiesPlugin();
		// regex = negative look behind, so any _* fields except ones that start with _Industry
		p.setConfig(new MapBuilder<String,Object>().put("fieldsToRemoveRegEx","(?!_Industry)_.*").build());
		
		Map<String, Object> result=p.execute("surveyId", "visitorId", answers);
		
		Assert.assertEquals(true, result.containsKey("_Industry"));
		Assert.assertEquals(false, result.containsKey("_FirstName"));
		Assert.assertEquals(false, result.containsKey("_Email"));
	}
	
	
	@Test
	public void test2() throws Exception{
		
		Map<String,String> answers=new HashMap<>();
		answers.put("_value1", "1");
		answers.put("_value2", "2");
		answers.put("_value3", "3");
		
		StringSubstitutor substitutor=new StringSubstitutor(answers);
		System.out.println(substitutor.replace("${_value1} - ${_value2} - ${_value3}"));
	}
	
	@Test
	public void test3(){
//		String.format("%-7s", "").replaceAll(" ", "*")
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("a"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("ab"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abc"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcd"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcde"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcdef"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcdefg"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcdefgh"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("a@redhat.com"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abc@redhat.com"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcdefgh@redhat.com"));
		System.out.println(new RemoveReportPropertiesPlugin().obfuscate("abcdefghijklmno@redhat.com"));
	}
}
