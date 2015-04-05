package com.itworx.syncme.network;

import java.util.ArrayList;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import android.content.Context;

import com.itworx.syncme.controller.INetworkController;
import com.itworx.syncme.model.AuthenticationData;
import com.itworx.syncme.model.Property;
import com.itworx.syncme.model.User;
import com.itworx.syncme.model.VCard;

public class NetworkController implements INetworkController {
	private static NetworkController instance;

	private NetworkController() {
	}

	public static synchronized NetworkController getInstance(Context context) throws ConnectionException {
		if (instance == null) {
			instance = new NetworkController();
		}
		if (new Connection().checkConnection(context)==0)
			throw new ConnectionException();
		return instance;
	}
	@SuppressWarnings("unchecked")
	public String register(User user) throws HttpClientErrorException {

		ResponseEntity<String> responseEntity = (ResponseEntity<String>) new Connection()
				.sendRequest("User/register", HttpMethod.POST, null,
						String.class, user);

		return checkResult(responseEntity);

	}

	@SuppressWarnings("unchecked")
	public String loginUser(AuthenticationData authenticationData)
			throws HttpClientErrorException {
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) new Connection()
				.sendRequest("User/login", HttpMethod.POST, null, String.class,
						authenticationData);

		return checkResult(responseEntity);
	}

	@SuppressWarnings("unchecked")
	public String loginUserWithFacebook(String accessToken) throws HttpClientErrorException {
		ArrayList<Property> propertyList = new ArrayList<Property>();
		Property accessProperty = new Property("AccessToken", accessToken);
		propertyList.add(accessProperty);
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) new Connection()
				.sendRequest("User/login-facebook", HttpMethod.GET,
						propertyList, String.class, null);

		return checkResult(responseEntity);
	}


	@SuppressWarnings("unchecked")

	public String loginUserWithTwitter(String accessToken,
			String accessTokenSecret) throws HttpClientErrorException {
		ArrayList<Property> propertyList = new ArrayList<Property>();
		Property accessProperty = new Property("AccessToken", accessToken);
		Property accessSecretProperty = new Property("AccessTokenSecret",
				accessTokenSecret);
		propertyList.add(accessProperty);
		propertyList.add(accessSecretProperty);
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) new Connection()
				.sendRequest("user/login-twitter", HttpMethod.GET,
						propertyList, String.class, null);

		return checkResult(responseEntity);
	}

	@Override
	public String loginUsingGoogle(String token) throws HttpClientErrorException {
		ArrayList<Property> propertyList = new ArrayList<Property>();
		Property accessProperty = new Property("AccessToken", token);
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) new Connection()
				.sendRequest("User/login-google-access-token", HttpMethod.GET,
						propertyList, String.class, null);

		return checkResult(responseEntity);
	}

	@SuppressWarnings("unchecked")
	public VCard[] getAllUserContacts(String AccessToken)throws HttpClientErrorException {
		
		ArrayList<Property> propertyList = new ArrayList<Property>();
		Property accessProperty = new Property("Token", AccessToken);
		propertyList.add(accessProperty);
		
		ResponseEntity<VCard[]> responseEntity = (ResponseEntity<VCard[]>) new Connection()
				.sendRequest("vcard/GetAllVCard", HttpMethod.GET, propertyList, VCard[].class, null);
		VCard[] contacts = responseEntity.getBody();
		isAuthorized(responseEntity);
		return contacts;
	}

	@SuppressWarnings("unchecked")
	public String addUserContacts(String AccessToken, ArrayList<VCard> contacts)throws HttpClientErrorException {
		
		ArrayList<Property> propertyList = new ArrayList<Property>();
		Property accessProperty = new Property("Token", AccessToken);
		propertyList.add(accessProperty);
		
		ResponseEntity<String> responseEntity = (ResponseEntity<String>) new Connection()
		.sendRequest("vcard/AddContactsVcard", HttpMethod.POST,
				propertyList, String.class,
				contacts);
		isAuthorized(responseEntity);
		String result = responseEntity.getBody();
		return result;
	}
	private void isAuthorized(ResponseEntity<?> responseEntity) throws HttpClientErrorException{
		if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) 
            throw new HttpClientErrorException(responseEntity.getStatusCode());
		
	}


	private String checkResult(ResponseEntity<String> responseEntity) throws HttpClientErrorException {
		String result = responseEntity.getBody();
		if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) 
                 throw new HttpClientErrorException(responseEntity.getStatusCode());
		if (!Connection.FAILED_FORMATE.equals(result)) {
			return result;
		} else
			return Connection.FAILED + "";
	}

}
