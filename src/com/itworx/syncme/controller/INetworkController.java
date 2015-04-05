//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : @Sync ME
//  @ File Name : INetworkController.java
//  @ Date : 1/15/2013
//  @ Author : @ Muhammad hamdy & Ahmed Madkour
//
//

package com.itworx.syncme.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;

import org.springframework.web.client.HttpClientErrorException;

import com.itworx.syncme.model.AuthenticationData;
import com.itworx.syncme.model.VCard;
import com.itworx.syncme.model.User;

public interface INetworkController {
	/**
	 * 
	 * @param user
	 *            Registration information
	 * @return SUCCESSED = 111 which refer that the request successed ; FAILED =
	 *         110 the request failed;
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	public String register(User user) throws MalformedURLException,
			ProtocolException, IOException, HttpClientErrorException;

	/**
	 * 
	 * @param authenticationData
	 *            object contains username and password
	 * @return SUCCESSED = 111 which refer that the request successed ; FAILED =
	 *         110 the request failed;
	 * @throws IOException
	 * @throws Exception
	 */
	public String loginUser(AuthenticationData authenticationData)
			throws IOException, HttpClientErrorException;

	public String loginUserWithFacebook(String token) throws IOException,
			HttpClientErrorException;

	public String loginUserWithTwitter(String token, String tokenSecret)
			throws IOException, HttpClientErrorException;

	public VCard[] getAllUserContacts(String AccessToken)
			throws HttpClientErrorException;

	public String addUserContacts(String AccessToken, ArrayList<VCard> contacts)
			throws HttpClientErrorException;

	public String loginUsingGoogle(String token) throws IOException,
			HttpClientErrorException;

}
