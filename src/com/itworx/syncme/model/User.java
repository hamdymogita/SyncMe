package com.itworx.syncme.model;

import com.itworx.syncme.R;

public class User {

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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword.trim();
	}

	public String getAccessToken() {
		return AccessToken;
	}

	public void setAccessToken(String AccessToken) {
		this.AccessToken = AccessToken.trim();
	}

	public int checkRegisterValidation() {

		if (!isUsernameValid())
			return R.string.forget_username;

		if (!isPasswordValid())
			return R.string.forget_password;
		else if (!checkPasswordConfirmedValid())
			return R.string.Error_confirm_password;

		return -1;
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
		if ((username == null) || (username.length() == 0))
			return false;

		return true;
	}

	private boolean isPasswordValid() {
		if ((password == null) || (password.length() == 0))
			return false;
		return true;
	}

	private boolean checkPasswordConfirmedValid() {
		if ((confirmPassword == null) || (confirmPassword.length() == 0))
			return false;
		else if (!password.equals(confirmPassword))
			return false;

		return true;
	}

	private String username, password, confirmPassword, AccessToken;

}
