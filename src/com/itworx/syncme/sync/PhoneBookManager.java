package com.itworx.syncme.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class PhoneBookManager {

	
	public static Uri insertContentValues(ContentResolver contentResolver,
			Uri contentUri, ContentValues peopleCV) {
		if (peopleCV != null) {
			return contentResolver.insert(contentUri, peopleCV);

		}
		return null;
	}
	
}
