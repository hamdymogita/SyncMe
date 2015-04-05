package com.vcardio.vcard.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;

public class VCardAdder {

	public VCardAdder(Context context) {

	}

	public void importCard(final ContentResolver cResolver, final String vcard,
			final List<String> contactGroups) {
		AndroidParser aParser = new AndroidParser();

		VCardParser vParser = new VCardParser();
		Contact contact = new Contact();
		BufferedReader vcardReader = new BufferedReader(new StringReader(vcard));
		try {
			vParser.parseVCard(contact, vcardReader);
		} catch (IOException e) {
			e.printStackTrace();
		}

		 aParser.addContact(contact, cResolver, 0, contactGroups,
				false);

	}
}
