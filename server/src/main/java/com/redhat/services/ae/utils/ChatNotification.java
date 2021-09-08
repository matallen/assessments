package com.redhat.services.ae.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.controllers.SurveyController;
import com.redhat.services.ae.utils.Http.Response;

/**
 * Integration with google chat boards, to push notifications of events such as user promotions, script failures etc..
 * Requires ENV variables:
 * 	- NOTIFICATIONS_GOOGLE_CHAT_WEBHOOK_TEMPLATE -> take from a google chat room/webhooks
 *  - NOTIFICATIONS -> [{"enabled": "true","channel":"https://chat.googleapis.com/v1/spaces/upUybAAAAAE/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=0g3sxujUXee_wlmEN9rj65CS3mC5847OVsXNMFwk_3c%3D","events": "onWarning,onError"}]
 * 
 * 
 * @author mallen
 */
public class ChatNotification{
	public static final Logger log=LoggerFactory.getLogger(SurveyController.class);
	public String DEFAULT_TEMPLATE="{'text':'%s'}";
	public String DEFAULT_NOTIFICATIONS="[{'channel':'google chat webhook url here','events': 'onError,onWarning','enabled': 'false'}]";
//	[{'channel':'https://chat.googleapis.com/v1/spaces/AAAA_3xbMB0/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=W86nwmWdCUqTeu6tlPTSd51zCyx6RP2B-iPo58nNL5M%3D','events': 'onError,onWarning','enabled': 'true'}]
	
	private String template;
	private List<Map<String,String>> notifications=null;
	
	public ChatNotification(){
		template=null==System.getenv("NOTIFICATIONS_GOOGLE_CHAT_WEBHOOK_TEMPLATE")?DEFAULT_TEMPLATE:System.getenv("NOTIFICATIONS_GOOGLE_CHAT_WEBHOOK_TEMPLATE");
		try{
			if (null!=System.getenv("NOTIFICATIONS"))
				notifications=Json.toObject(System.getenv("NOTIFICATIONS"), new TypeReference<List<Map<String,String>>>(){});
		}catch(IOException e){
			log.error("notifications config found, but was invalid");
			e.printStackTrace();
		}
	}
	
	public enum ChatEvent{onError,onWarning}
	
	public void send(ChatEvent type, String notificationText){
		if (null!=notifications){
			for(Map<String, String> notification:notifications){
				if (!"false".equalsIgnoreCase(notification.get("enabled"))){
					List<String> events=Lists.newArrayList(notification.get("events").split(","));
					if (events.contains(type.name())){
						// send the notification!
						String channel=notification.get("channel");
//					String template= c.getOptions().get("googlehangoutschat.webhook.template");
						String googleHangoutsChatPayload=String.format(template, notificationText);
						Response r=Http.post(channel, googleHangoutsChatPayload, new MapBuilder<String, String>().put("Content-Type", "application/json; charset=UTF-8").build());
						log.debug("Response = "+r.getResponseCode());
					}
				}
			}
		}
	}
}
