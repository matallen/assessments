package com.redhat.services.ae.modules.login;

import com.redhat.services.ae.model.User;

public interface LoginModule{
	
	public User login(String username, String password);

//	public String populateJwt(String jwtClaims);

}
