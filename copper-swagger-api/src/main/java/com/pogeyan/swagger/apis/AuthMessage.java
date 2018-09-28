package com.pogeyan.swagger.apis;

import com.pogeyan.swagger.api.utils.HttpUtils;

public class AuthMessage implements IAuthRequest {
	private String userName;
	private String password;

	public AuthMessage(String authorization) throws Exception {
		super();
		String credentials[] = HttpUtils.getCredentials(authorization);
		this.userName = credentials[0];
		this.password = credentials[1];
	}

	@Override
	public String getUserName() {
		return this.userName;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
