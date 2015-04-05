package com.itworx.syncme.model;

import java.io.Serializable;

import com.itworx.syncme.R;

@SuppressWarnings("serial")
public class AuthenticationData implements Serializable {
	
	private String username, password, AccessToken;
	String userNamepattern="[a-zA-Z0-9\\.\\-_ ]{3,}";
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password.trim();
	}
	
	public String getAccessToken(){
		return AccessToken;
	}
	
	public void setAccessToken(String AccessToken){
		this.AccessToken = AccessToken.trim();
	}

	
	public int checkLoginValidation() {

		if (!isUsernameValid())
			return R.string.forget_username;
		if (!isPasswordValid())
			return R.string.forget_password;

		return -1;
	}

	/**
	 * Methods for check validations
	 */
	private boolean isUsernameValid() {
		if ((username == null) || (username.length() == 0)/*||!username.matches(userNamepattern)*/)
			return false;
		
		return true;
	}

	private boolean isPasswordValid() {
		if ((password == null) || (password.length() ==0))
			return false;
		return true;
	}
	
/*	private boolean isAccessTokenValid() {
		if ((AccessToken == null) || (AccessToken.length() == 0))
			return false;
		return true;
	}*/

}
