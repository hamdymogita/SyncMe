package com.itworx.syncme.controller;

import com.itworx.common.utils.Constants;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesController extends Application {

	protected static PreferencesController instance;

	public PreferencesController() {
		instance = this;

		

	}

	public static PreferencesController getInstance() {
		if (instance == null)
			instance = new PreferencesController();
		return instance;
	}

	public String getCurrentLanguage() {
		if (shared == null)
			shared = getInstance().getSharedPreferences(settingsPreference,MODE_PRIVATE);
		return shared.getString("language", getDeviceLang());
	}

	public void setCurrentLanguage(String langName) {
		if (shared == null)
			shared = getInstance().getSharedPreferences(settingsPreference, MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putString("language", langName);
		editor.commit();
	}

	private String getDeviceLang() {

		String locale = getResources().getConfiguration().locale
				.getDisplayName();
		if (locale.contains("Arabic"))
			return "ar";
		else
			return "en";
	}
	public String getValue(String key) {
		if (shared == null)
			shared = getInstance().getSharedPreferences(settingsPreference,MODE_PRIVATE);
		return shared.getString(key, Constants.NON);
	}

	public void saveValue(String key,String value) {
		if (shared == null)
			shared = getInstance().getSharedPreferences(settingsPreference, MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	
	protected SharedPreferences shared;
	protected final String settingsPreference = "SyncMEAPP";
}
