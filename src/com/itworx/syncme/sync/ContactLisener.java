package com.itworx.syncme.sync;


import com.itworx.syncme.controller.PreferencesController;

import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;

public class ContactLisener extends Application{

	SharedPreferences ContactPrefs;
	protected static ContactLisener instance;
	
	public ContactLisener() {
		instance = this;
	}

	public static ContactLisener getInstance() {
		if (instance == null)
			instance = new ContactLisener();
		return instance;
	}
	
	public void initalListener(ContentResolver contentResolver) {
		
	    saveContactStatus(contentResolver);
	    
		ContactsContentObserver cco = new ContactsContentObserver(new Handler());
		contentResolver.registerContentObserver(RawContacts.CONTENT_URI,
		    true, cco);
	}

	public void saveContactStatus(ContentResolver contentResolver) {
		try{
		Cursor allContacts = contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
		String contactVersion = "";
		allContacts.moveToFirst();
		
		for (int i = 0; i < allContacts.getCount(); i++) {
			 int col_VERSION = allContacts.getColumnIndex(ContactsContract.RawContacts.VERSION);
			 contactVersion += "," + allContacts.getString(col_VERSION) + "";
			 allContacts.moveToNext();
				
			 PreferencesController Prefs =PreferencesController.getInstance();
			 Prefs.saveValue(com.itworx.common.utils.Constants.CONTACTVERSION, contactVersion);
			}
		}catch (Exception e) {
			
		}
		
	}

	public class ContactsContentObserver extends ContentObserver 
	{
	 public ContactsContentObserver(Handler h) 
	 {
	      super(h);
	    }

	    public void onChange(boolean selfChange) 
	    {
//	    	SyncManager manager = new SyncManager();
//	    	manager.getChangedContact(getApplicationContext(), ContactPrefs);
	    }
	}
}
