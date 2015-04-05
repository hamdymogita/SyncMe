package com.vcardio.funambol.util;

import java.util.HashMap;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Controller extends Application {

	private HashMap<String, Object> restModelPool;

	protected static Controller instance;

	public Controller() {
		instance = this;

		restModelPool = new HashMap<String, Object>();

	}

	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();
		return instance;
	}

	public Object getPooledModel(String name) {
		return restModelPool.get(name);
	}

	public Object createPooledModel(String name, Object model) {
		restModelPool.put(name, model);
		return restModelPool.get(name);
	}

	
}
