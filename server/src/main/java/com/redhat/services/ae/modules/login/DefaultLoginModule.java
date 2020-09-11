package com.redhat.services.ae.modules.login;

import org.eclipse.microprofile.config.ConfigProvider;

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
		String USERNAME=ConfigProvider.getConfig().getValue("modules.login.default.username", String.class);
		String PASSWORD=ConfigProvider.getConfig().getValue("modules.login.default.password", String.class);
		
//		System.out.println("loginmodule user/pass="+USERNAME+"/"+PASSWORD);
//		System.out.println("pass? = "+(USERNAME.equals(username) && PASSWORD.equals(password)));
		
		if (USERNAME.equals(username) && PASSWORD.equals(password)){
			return User.newBuilder().username(username).addRole(Role.Admin).build();
		}else{
			return null;
		}
	}

}
