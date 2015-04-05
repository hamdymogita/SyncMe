/*
 * Funambol is a mobile platform developed by Funambol, Inc. 
 * Copyright (C) 2003 - 2007 Funambol, Inc.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission 
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 * 
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 * 
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol". 
 * 
 * 2009-02-25 ducktayp: Modified to parse and format more Vcard fields, including PHOTO.
 * 						No attempt was made to preserve compatibility with previous code.
 *    					
 */

package com.vcardio.vcard.io;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Contacts;

import com.vcardio.funambol.util.StringUtil;

/**
 * A Contact item
 */
public class AndroidParser {
	// Parse birthday in notes
	final static String BIRTHDAY_FIELD = "Birthday:";
	final static Pattern birthdayPattern = Pattern.compile("^" + BIRTHDAY_FIELD
			+ ":\\s*([^;]+)(;\\s*|\\s*$)", Pattern.CASE_INSENSITIVE);

	// Protocol labels
	static final String[] PROTO = { "AIM", // ContactMethods.PROTOCOL_AIM = 0
			"MSN", // ContactMethods.PROTOCOL_MSN = 1
			"YAHOO", // ContactMethods.PROTOCOL_YAHOO = 2
			"SKYPE", // ContactMethods.PROTOCOL_SKYPE = 3
			"QQ", // ContactMethods.PROTOCOL_QQ = 4
			"GTALK", // ContactMethods.PROTOCOL_GOOGLE_TALK = 5
			"ICQ", // ContactMethods.PROTOCOL_ICQ = 6
			"JABBER" // ContactMethods.PROTOCOL_JABBER = 7
	};

	/**
	 * Add a new contact to the Content Resolver
	 * 
	 * @param key
	 *            the row number of the existing contact (if known)
	 * @param destGroup
	 *            the contact group to which a new contact will be added
	 * @param useOnlyGroup
	 *            don't add the contact to the "All contacts" group as well
	 * @param replace
	 *            replace existing contacts with the same syncid.
	 * @return The row number of the inserted column
	 */
	public long addContact(Contact contact, ContentResolver cResolver,
			long key, List<String> destGroups, boolean replace) {
		ContentValues pCV = getPeopleCV(contact);

		Uri newContactUri = null;
		newContactUri = insertContentValues(cResolver,
				Contacts.People.CONTENT_URI, pCV);

		// // Set the contact person id
		contact.setId(newContactUri.getLastPathSegment());

		// Phones
		for (Contact.RowData phone : contact.phones) {
			insertContentValues(cResolver, Contacts.Phones.CONTENT_URI,
					getPhoneCV(contact, phone));
		}

		// Organizations
		for (Contact.OrgData org : contact.orgs) {
			insertContentValues(cResolver, Contacts.Organizations.CONTENT_URI,
					getOrganizationCV(contact, org));
		}

		Builder builder = newContactUri.buildUpon();
		builder.appendEncodedPath(Contacts.ContactMethods.CONTENT_URI.getPath());

		// Emails
		for (Contact.RowData email : contact.emails) {
			insertContentValues(cResolver, builder.build(),
					getEmailCV(contact, email));
		}

		// Addressess
		for (Contact.RowData addr : contact.addrs) {
			insertContentValues(cResolver, builder.build(),
					getAddressCV(contact, addr));
		}

		// IMs
		for (Contact.RowData im : contact.ims) {
			insertContentValues(cResolver, builder.build(),
					getImCV(contact, im));
		}

		// Photo
		if (contact.photo != null) {
			Uri person = ContentUris.withAppendedId(
					Contacts.People.CONTENT_URI, key);
			Contacts.People.setPhotoData(cResolver, person, contact.photo);
		}

		return key;
	}

	/**
	 * Retrieve the People fields from a Cursor
	 */
	private void setPeopleFields(Contact contact, Cursor cur) {

		int selectedColumn;

		// Set the contact id
		selectedColumn = cur.getColumnIndex(Contacts.People._ID);
		long nid = cur.getLong(selectedColumn);
		contact._id = String.valueOf(nid);

		//
		// Get PeopleColumns fields
		//
		selectedColumn = cur.getColumnIndex(Contacts.PeopleColumns.NAME);
		contact.displayName = cur.getString(selectedColumn);

		contact.parseDisplayName(contact.displayName);

		selectedColumn = cur.getColumnIndex(Contacts.People.NOTES);
		contact.notes = cur.getString(selectedColumn);
		if (contact.notes != null) {
			Matcher ppm = birthdayPattern.matcher(contact.notes);

			if (ppm.find()) {
				contact.birthday = ppm.group(1);
				contact.notes = ppm.replaceFirst("");
			}
		}
	}

	/**
	 * Retrieve the organization fields from a Cursor
	 */
	private void setOrganizationFields(Contact contact, Cursor cur) {

		int selectedColumn;

		//
		// Get Organizations fields
		//
		selectedColumn = cur
				.getColumnIndex(Contacts.OrganizationColumns.COMPANY);
		String company = cur.getString(selectedColumn);

		selectedColumn = cur.getColumnIndex(Contacts.OrganizationColumns.TITLE);
		String title = cur.getString(selectedColumn);

		selectedColumn = cur.getColumnIndex(Contacts.OrganizationColumns.TYPE);
		int orgType = cur.getInt(selectedColumn);

		String customLabel = null;
		if (orgType == Contacts.ContactMethodsColumns.TYPE_CUSTOM) {
			selectedColumn = cur
					.getColumnIndex(Contacts.ContactMethodsColumns.LABEL);
			customLabel = cur.getString(selectedColumn);
		}

		contact.orgs.add(new Contact.OrgData(orgType, title, company,
				customLabel));
	}

	/**
	 * Retrieve the Phone fields from a Cursor
	 */
	private void setPhoneFields(Contact contact, Cursor cur) {

		int selectedColumn;
		int selectedColumnType;
		int preferredColumn;
		int phoneType;
		String customLabel = null;

		//
		// Get PhonesColums fields
		//
		selectedColumn = cur.getColumnIndex(Contacts.PhonesColumns.NUMBER);
		selectedColumnType = cur.getColumnIndex(Contacts.PhonesColumns.TYPE);
		preferredColumn = cur.getColumnIndex(Contacts.PhonesColumns.ISPRIMARY);
		phoneType = cur.getInt(selectedColumnType);
		String phone = cur.getString(selectedColumn);
		boolean preferred = cur.getInt(preferredColumn) != 0;
		if (phoneType == Contacts.PhonesColumns.TYPE_CUSTOM) {
			customLabel = cur.getString(cur
					.getColumnIndex(Contacts.PhonesColumns.LABEL));
		}

		contact.phones.add(new Contact.RowData(phoneType, phone, preferred,
				customLabel));
	}

	/**
	 * Retrieve the email fields from a Cursor
	 */
	private void setContactMethodsFields(Contact contact, Cursor cur) {

		int selectedColumn;
		int selectedColumnType;
		int selectedColumnKind;
		int selectedColumnPrimary;
		int selectedColumnLabel;

		int methodType;
		int kind;
		String customLabel = null;
		String auxData = null;

		//
		// Get ContactsMethodsColums fields
		//
		selectedColumn = cur
				.getColumnIndex(Contacts.ContactMethodsColumns.DATA);
		selectedColumnType = cur
				.getColumnIndex(Contacts.ContactMethodsColumns.TYPE);
		selectedColumnKind = cur
				.getColumnIndex(Contacts.ContactMethodsColumns.KIND);
		selectedColumnPrimary = cur
				.getColumnIndex(Contacts.ContactMethodsColumns.ISPRIMARY);

		kind = cur.getInt(selectedColumnKind);

		methodType = cur.getInt(selectedColumnType);
		String methodData = cur.getString(selectedColumn);
		boolean preferred = cur.getInt(selectedColumnPrimary) != 0;
		if (methodType == Contacts.ContactMethodsColumns.TYPE_CUSTOM) {
			selectedColumnLabel = cur
					.getColumnIndex(Contacts.ContactMethodsColumns.LABEL);
			customLabel = cur.getString(selectedColumnLabel);
		}

		switch (kind) {
		case Contacts.KIND_EMAIL:
			contact.emails.add(new Contact.RowData(methodType, methodData,
					preferred, customLabel));
			break;
		case Contacts.KIND_POSTAL:
			contact.addrs.add(new Contact.RowData(methodType, methodData,
					preferred, customLabel));
			break;
		case Contacts.KIND_IM:
			Contact.RowData newRow = new Contact.RowData(methodType,
					methodData, preferred, customLabel);

			selectedColumn = cur
					.getColumnIndex(Contacts.ContactMethodsColumns.AUX_DATA);
			auxData = cur.getString(selectedColumn);

			if (auxData != null) {
				String[] auxFields = StringUtil.split(auxData, ":");
				if (auxFields.length > 1) {
					if (auxFields[0].equalsIgnoreCase("pre")) {
						int protval = 0;
						try {
							protval = Integer.decode(auxFields[1]);
						} catch (NumberFormatException e) {
							// Do nothing; protval = 0
						}
						if (protval < 0 || protval >= PROTO.length)
							protval = 0;
						newRow.auxData = PROTO[protval];
					} else if (auxFields[0].equalsIgnoreCase("custom")) {
						newRow.auxData = auxFields[1];
					}
				} else {
					newRow.auxData = auxData;
				}
			}

			contact.ims.add(newRow);
			break;
		}
	}

	private ContentValues getPeopleCV(Contact contact) {
		ContentValues cv = new ContentValues();

		StringBuffer fullname = new StringBuffer();
		if (contact.displayName != null)
			fullname.append(contact.displayName);
		else {
			if (contact.preName != null)
				fullname.append(contact.preName);
			if (contact.firstName != null) {
				if (fullname.length() > 0)
					fullname.append(" ");
				fullname.append(contact.firstName);
			}
			if (contact.midNames != null) {
				if (fullname.length() > 0)
					fullname.append(" ");
				fullname.append(contact.midNames);
			}
			if (contact.lastName != null) {
				if (fullname.length() > 0)
					fullname.append(" ");
				fullname.append(contact.lastName);
			}
			if (contact.sufName != null) {
				if (fullname.length() > 0)
					fullname.append(" ");
				fullname.append(contact.sufName);
			}
		}

		// Use company name if only the company is given.
		if (fullname.length() == 0 && contact.orgs.size() > 0
				&& contact.orgs.get(0).company != null)
			fullname.append(contact.orgs.get(0).company);

		cv.put(Contacts.People.NAME, fullname.toString());

		if (!StringUtil.isNullOrEmpty(contact._id)) {
			cv.put(Contacts.People._ID, contact._id);
		}

		StringBuffer allnotes = new StringBuffer();
		if (contact.birthday != null) {
			allnotes.append(BIRTHDAY_FIELD).append(" ")
					.append(contact.birthday);
		}
		if (contact.notes != null) {
			if (contact.birthday != null) {
				allnotes.append(";\n");
			}
			allnotes.append(contact.notes);
		}

		if (allnotes.length() > 0)
			cv.put(Contacts.People.NOTES, allnotes.toString());

		return cv;
	}

	private ContentValues getOrganizationCV(Contact contact, Contact.OrgData org) {

		if (StringUtil.isNullOrEmpty(org.company)
				&& StringUtil.isNullOrEmpty(org.title)) {
			return null;
		}
		ContentValues cv = new ContentValues();

		cv.put(Contacts.Organizations.COMPANY, org.company);
		cv.put(Contacts.Organizations.TITLE, org.title);
		cv.put(Contacts.Organizations.TYPE, org.type);
		cv.put(Contacts.Organizations.PERSON_ID, contact._id);
		if (org.customLabel != null) {
			cv.put(Contacts.Organizations.LABEL, org.customLabel);
		}

		return cv;
	}

	private ContentValues getPhoneCV(Contact contact, Contact.RowData data) {
		ContentValues cv = new ContentValues();

		cv.put(Contacts.Phones.NUMBER, data.data);
		cv.put(Contacts.Phones.TYPE, data.type);
		cv.put(Contacts.Phones.ISPRIMARY, data.preferred ? 1 : 0);
		cv.put(Contacts.Phones.PERSON_ID, contact._id);
		if (data.customLabel != null) {
			cv.put(Contacts.Phones.LABEL, data.customLabel);
		}

		return cv;
	}

	private ContentValues getEmailCV(Contact contact, Contact.RowData data) {
		ContentValues cv = new ContentValues();

		cv.put(Contacts.ContactMethods.DATA, data.data);
		cv.put(Contacts.ContactMethods.TYPE, data.type);
		cv.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL);
		cv.put(Contacts.ContactMethods.ISPRIMARY, data.preferred ? 1 : 0);
		cv.put(Contacts.ContactMethods.PERSON_ID, contact._id);
		if (data.customLabel != null) {
			cv.put(Contacts.ContactMethods.LABEL, data.customLabel);
		}

		return cv;
	}

	private ContentValues getAddressCV(Contact contact, Contact.RowData data) {
		ContentValues cv = new ContentValues();

		cv.put(Contacts.ContactMethods.DATA, data.data);
		cv.put(Contacts.ContactMethods.TYPE, data.type);
		cv.put(Contacts.ContactMethods.KIND, Contacts.KIND_POSTAL);
		cv.put(Contacts.ContactMethods.ISPRIMARY, data.preferred ? 1 : 0);
		cv.put(Contacts.ContactMethods.PERSON_ID, contact._id);
		if (data.customLabel != null) {
			cv.put(Contacts.ContactMethods.LABEL, data.customLabel);
		}

		return cv;
	}

	private ContentValues getImCV(Contact contact, Contact.RowData data) {
		ContentValues cv = new ContentValues();

		cv.put(Contacts.ContactMethods.DATA, data.data);
		cv.put(Contacts.ContactMethods.TYPE, data.type);
		cv.put(Contacts.ContactMethods.KIND, Contacts.KIND_IM);
		cv.put(Contacts.ContactMethods.ISPRIMARY, data.preferred ? 1 : 0);
		cv.put(Contacts.ContactMethods.PERSON_ID, contact._id);
		if (data.customLabel != null) {
			cv.put(Contacts.ContactMethods.LABEL, data.customLabel);
		}

		if (data.auxData != null) {
			int protoNum = -1;
			for (int i = 0; i < PROTO.length; ++i) {
				if (data.auxData.equalsIgnoreCase(PROTO[i])) {
					protoNum = i;
					break;
				}
			}
			if (protoNum >= 0) {
				cv.put(Contacts.ContactMethods.AUX_DATA, "pre:" + protoNum);
			} else {
				cv.put(Contacts.ContactMethods.AUX_DATA, "custom:"
						+ data.auxData);
			}
		}

		return cv;
	}

	/**
	 * Insert a new ContentValues raw into the Android ContentProvider
	 */
	private Uri insertContentValues(ContentResolver cResolver, Uri uri,
			ContentValues cv) {
		if (cv != null) {
			return cResolver.insert(uri, cv);
			
		}
		return null;
	}
}
