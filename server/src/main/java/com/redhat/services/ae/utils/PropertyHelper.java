package com.redhat.services.ae.utils;

import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;

public class PropertyHelper<T>{

	public static Optional<String> getPropertyAsString(String key){
		return ConfigProvider.getConfig().getOptionalValue(key, String.class);
	}
}
