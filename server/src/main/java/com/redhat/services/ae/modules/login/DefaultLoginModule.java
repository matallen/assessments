package com.redhat.services.ae.modules.login;

import com.redhat.services.ae.model.User;

public class DefaultLoginModule implements LoginModule{

	public enum Role{
		Admin("JwtClaims.json");
		public String template;
		Role(String v){ this.template=v;};
	}
	
	/**
	 * Return null if login failure, return populated User object if sucessful
	 * @param username
	 * @param password
	 * @return
	 */
	public User login(String username, String password){
		if ("admin".equals(username) && "admin".equals(password)){
			return User.newBuilder().username(username).addRole(Role.Admin).build();
		}else{
			return null;
		}
	}

}
