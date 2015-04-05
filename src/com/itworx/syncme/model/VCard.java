package com.itworx.syncme.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.vcardio.vcard.io.VCardAdder;

public class VCard {
	String FirstName, LastName, FullName, Email, PhoneNumber, VCardData,
			DisplayName;

	public String getFirstName() {
		return FirstName;
	}

	public void setFirstName(String firstName) {
		this.FirstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}

	public void setLastName(String lastName) {
		this.LastName = lastName;
	}

	public String getFullName() {
		return FullName;
	}

	public void setFullName(String fullName) {
		this.FullName = fullName;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		this.Email = email;
	}

	public String getPhoneNumber() {
		return PhoneNumber;
	}

	public void setPhoneNumber(String mobile) {
		this.PhoneNumber = mobile;
	}

	public String getVCardData() {
		return VCardData;
	}

	public void setVCardData(String vCardData) {
		VCardData = vCardData;
	}

	public String getDisplayName() {
		return DisplayName;
	}

	public void setDisplayName(String displayName) {
		DisplayName = displayName;
	}

	public void addContact(Context context) {

//		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
//
//		int rawContactID = ops.size();
//
//		// Adding insert operation to operations list
//		// to insert a new raw contact in the table ContactsContract.RawContacts
//		ops.add(ContentProviderOperation
//				.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//				.withValue(RawContacts.ACCOUNT_NAME, null).build());
//
//		// Adding insert operation to operations list
//		// to insert display name in the table ContactsContract.Data
//		ops.add(ContentProviderOperation
//				.newInsert(ContactsContract.Data.CONTENT_URI)
//				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
//						rawContactID)
//				.withValue(ContactsContract.Data.MIMETYPE,
//						StructuredName.CONTENT_ITEM_TYPE)
//				.withValue(StructuredName.DISPLAY_NAME, getFullName()).build());
//
//		// Adding insert operation to operations list
//		// to insert Mobile Number in the table ContactsContract.Data
//		ops.add(ContentProviderOperation
//				.newInsert(ContactsContract.Data.CONTENT_URI)
//				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
//						rawContactID)
//				.withValue(ContactsContract.Data.MIMETYPE,
//						Phone.CONTENT_ITEM_TYPE)
//				.withValue(Phone.NUMBER, getPhoneNumber())
//				.withValue(Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
//				.build());
//
//		// Adding insert operation to operations list
//		// to insert Work Email in the table ContactsContract.Data
//		ops.add(ContentProviderOperation
//				.newInsert(ContactsContract.Data.CONTENT_URI)
//				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
//						rawContactID)
//				.withValue(
//						ContactsContract.Data.MIMETYPE,
//						android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
//				.withValue(
//						android.provider.ContactsContract.CommonDataKinds.Email.ADDRESS,
//						getEmail())
//				.withValue(
//						android.provider.ContactsContract.CommonDataKinds.Email.TYPE,
//						android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK)
//				.build());
//
//		try {
//			// Executing all the insert operations as a single database
//			// transaction
//			context.getContentResolver().applyBatch(ContactsContract.AUTHORITY,
//					ops);
//			// Toast.makeText(getBaseContext(), "Contact is successfully added",
//			// Toast.LENGTH_SHORT).show();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (OperationApplicationException e) {
//			e.printStackTrace();
//		}
		// for import contacts from vcf
		VCardAdder vCardAdder = new VCardAdder(context);
		vCardAdder.importCard(context.getContentResolver(),VCardData, null);

	}

	public ArrayList<VCard> getContacts(Context context) {
		ArrayList<VCard> contacts = new ArrayList<VCard>();
		VCard contact;

		Uri uri = ContactsContract.Contacts.CONTENT_URI;

		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ ("1") + "'";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";

		Cursor cursor = context.getContentResolver().query(uri, null,
				selection, selectionArgs, sortOrder);

		

		while (cursor.moveToNext()) {
			contact = new VCard();
			contact.setDisplayName(cursor.getString(cursor
					.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));
			contact.setVCardData(getVcardContact(cursor,context));
			contacts.add(contact);
		}

		cursor.close();
		return contacts;

	}

	Cursor cursor;
	ArrayList<String> vCards;
	String vfile;
//	Context context;
//
//	/** Called when the activity is first created. */
//	public VCard(Context context) {
//		this.context = context;
//		vfile = "Contacts" + "_" + System.currentTimeMillis() + ".vcf";
//		/**
//		 * This Function For Vcard And here i take one Array List in Which i
//		 * store every Vcard String of Every Conatact Here i take one Cursor and
//		 * this cursor is not null and its count>0 than i repeat one loop up to
//		 * cursor.getcount() means Up to number of phone contacts. And in Every
//		 * Loop i can make vcard string and store in Array list which i declared
//		 * as a Global. And in Every Loop i move cursor next and print log in
//		 * logcat.
//		 * */
//		getVcardString();
//
//	}

	public VCard() {
		// TODO Auto-generated constructor stub
	}

//	private void getVcardString() {
//		// TODO Auto-generated method stub
//		vCards = new ArrayList<String>();
//		cursor = context.getContentResolver().query(
//				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
//				null, null);
//		if (cursor != null && cursor.getCount() > 0) {
//			cursor.moveToFirst();
//			for (int i = 0; i < cursor.getCount(); i++) {
//
//				getVcardContact(cursor);
//				Log.d("TAG",
//						"Contact " + (i + 1) + "VcF String is" + vCards.get(i));
//				cursor.moveToNext();
//			}
//
//		} else {
//			Log.d("TAG", "No Contacts in Your Phone");
//		}
//
//	}

	public String getVcardContact(Cursor cursor, Context context) {
//		Cursor cursor = context.getContentResolver().query(
//				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
//				null, null);
		// cursor.moveToFirst();
		String lookupKey = cursor.getString(cursor
				.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
		Uri uri = Uri.withAppendedPath(
				ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
		AssetFileDescriptor fd;

		String vcardstring = "";
		try {
			fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");

			// Your Complex Code and you used function without loop so how can
			// you get all Contacts Vcard.??

			/*
			 * FileInputStream fis = fd.createInputStream(); byte[] buf = new
			 * byte[(int) fd.getDeclaredLength()]; fis.read(buf); String VCard =
			 * new String(buf); String path =
			 * Environment.getExternalStorageDirectory().toString() +
			 * File.separator + vfile; FileOutputStream out = new
			 * FileOutputStream(path); out.write(VCard.toString().getBytes());
			 * Log.d("Vcard", VCard);
			 */

			FileInputStream fis = fd.createInputStream();
			byte[] buf = new byte[(int) fd.getDeclaredLength()];
			fis.read(buf);
			vcardstring = new String(buf);
			// vCards.add(vcardstring);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// String storage_path = Environment.getExternalStorageDirectory()
		// .toString() + File.separator + vfile;
		// FileOutputStream mFileOutputStream = new FileOutputStream(
		// storage_path, false);
		// mFileOutputStream.write(vcardstring.toString().getBytes());
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vcardstring;

	}

}
