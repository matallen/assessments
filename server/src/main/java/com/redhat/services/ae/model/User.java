package com.redhat.services.ae.model;

import java.util.ArrayList;
import java.util.List;

import com.redhat.services.ae.modules.login.DefaultLoginModule.Role;

public class User{
	private User(){}
	protected String username; public String getUsername(){ return username; }
	protected List<Role> roles; public List<Role> getRoles(){ if (null==roles) roles=new ArrayList<Role>(); return roles; }

	public static Builder newBuilder(){return new User().new Builder();}
	public class Builder extends User{
		public Builder username(String username){ this.username=username; return this;}
		public Builder addRole(Role role){ getRoles().add(role); return this;}
		public User build(){
			User u=new User();
			u.username=super.username;
			u.roles=super.roles;
			return u;
		}
	}
}
