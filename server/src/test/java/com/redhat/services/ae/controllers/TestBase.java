package com.redhat.services.ae.controllers;

import org.junit.jupiter.api.BeforeEach;

import com.redhat.services.ae.model.storage.Surveys;

public class TestBase{

	
	@BeforeEach
	public void _init(){
		Surveys.STORAGE_ROOT="target/persistence_testing";
	}
}
