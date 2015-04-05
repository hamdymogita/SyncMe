package com.itworx.syncme.sync;

import java.util.Iterator;

import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.features.EmailFeature;
import net.sourceforge.cardme.vcard.features.OrganizationFeature;
import net.sourceforge.cardme.vcard.features.TelephoneFeature;
import net.sourceforge.cardme.vcard.types.parameters.EmailParameterType;
import net.sourceforge.cardme.vcard.types.parameters.TelephoneParameterType;
import net.sourceforge.cardme.vcard.types.parameters.XEmailParameterType;
import net.sourceforge.cardme.vcard.types.parameters.XTelephoneParameterType;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Contacts;
import android.provider.ContactsContract;

public class ContactController {
	Builder builder;
	public void addContact(Context context, VCard vCard) {
		Uri newContactUri = null;

		ContentResolver contentResolver = context.getContentResolver();

		newContactUri = PhoneBookManager.insertContentValues(contentResolver,
				Contacts.People.CONTENT_URI, getPeopleCV(vCard));

		vCard.setID(newContactUri.getLastPathSegment());

		
		 builder = newContactUri.buildUpon();
		builder.appendEncodedPath(Contacts.ContactMethods.CONTENT_URI.getPath());
		
		
		getPhones(contentResolver, vCard);
		getEmails(contentResolver, vCard);
		getOrganiztions(contentResolver, vCard);



	}

	private void getOrganiztions(ContentResolver contentResolver, VCard contact) {
		
		Iterator<OrganizationFeature> organizationFeatures = contact.getOrganizationsIterator();
		
		while (organizationFeatures.hasNext()) {
			OrganizationFeature oragnizationFeature=organizationFeatures.next();
			String oragnizationName=oragnizationFeature.getOrganizations().next();
			PhoneBookManager.insertContentValues(contentResolver,
					Contacts.Organizations.CONTENT_URI,
					getOrganizationCV(contact, oragnizationName));
		}
		
	}
	
	private void getEmails(ContentResolver contentResolver, VCard contact) {
		// Phones
		Iterator<EmailFeature> emailIterator = contact.getEmails();

		while (emailIterator.hasNext()) {
			EmailFeature emailFeature = emailIterator.next();
			PhoneBookManager.insertContentValues(contentResolver,
					builder.build(), getEmailCV(contact, emailFeature));
		}

	}

	
	private void getPhones(ContentResolver contentResolver, VCard contact) {
		// Phones
		if (contact.getTelephoneNumbers() != null){
			Iterator<TelephoneFeature> telephoneIterator = contact.getTelephoneNumbers();
			while(telephoneIterator.hasNext()) {
				TelephoneFeature tel = telephoneIterator.next();
				PhoneBookManager.insertContentValues(contentResolver,
						Contacts.Phones.CONTENT_URI,
						getPhoneCV(tel, contact.getID()));
			}}

	}
	
	
	private ContentValues getOrganizationCV(VCard contact,
			String oragnizationName) {

		ContentValues cv = new ContentValues();

		cv.put(Contacts.Organizations.COMPANY, oragnizationName);
	
		cv.put(Contacts.Organizations.PERSON_ID, contact.getID());
		

		return cv;
	}
	
	private ContentValues getPhoneCV(TelephoneFeature tel, String id) {
		ContentValues cv = new ContentValues();

		cv.put(Contacts.Phones.NUMBER, tel.getTelephone());
		int type = 0;
		boolean primary = false;
		Iterator<TelephoneParameterType> telephoneParameterTypes = tel.getTelephoneParameterTypes();
		while (telephoneParameterTypes.hasNext()) {
		
			TelephoneParameterType telType = telephoneParameterTypes.next();
			if (telType.getType().equalsIgnoreCase("home")) {

				type = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
			}

			// else if (telType.equalsIgnoreCase("fax")) {
			// type = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
			// }
			else if (telType.getType().equalsIgnoreCase("mobile")) {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
			} else if (telType.getType().equalsIgnoreCase("work")) {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
			} else if (telType.getType().equalsIgnoreCase("pref")) {
				primary = true;
			}

			else if (telType.getType().equalsIgnoreCase("pager")) {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
			}

			else if (telType.getType().equalsIgnoreCase("car")) {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_CAR;
			} else if (telType.getType().equalsIgnoreCase("isdn")) {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_ISDN;
			}

			else if (telType.getType().equalsIgnoreCase("mms")) {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_MMS;
			} else {
				type = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

			}

			cv.put(Contacts.Phones.TYPE, type);
		}
		Iterator<XTelephoneParameterType> xtelephoneParameterTypes = tel.getExtendedTelephoneParameterTypes();
		while (xtelephoneParameterTypes.hasNext()) {
			XTelephoneParameterType telParameterType = xtelephoneParameterTypes
					.next();
			type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
			String label = telParameterType.getXtendedTypeName();
			cv.put(Contacts.ContactMethods.TYPE, type);
			cv.put(Contacts.ContactMethods.LABEL, label);
		}
		cv.put(Contacts.Phones.ISPRIMARY, primary ? 1 : 0);
		cv.put(Contacts.Phones.PERSON_ID, id);
		
		return cv;
	}

	private ContentValues getEmailCV(VCard contact, EmailFeature emailFeature) {
		ContentValues cv = new ContentValues();

		cv.put(Contacts.ContactMethods.DATA, emailFeature.getEmail());
		boolean preferred = false;
		String label = "";
		
		
		int type =ContactsContract.CommonDataKinds.Email.TYPE_HOME;
		Iterator<EmailParameterType> emailParamTypesIterator = emailFeature
				.getEmailParameterTypes();
		Iterator<XEmailParameterType> emailExtendedParametersIterator = emailFeature
				.getExtendedEmailParameterTypes();
		int size = emailFeature.getEmailParameterTypesList().size();

		while (emailParamTypesIterator.hasNext()) {
			EmailParameterType emailParameterType = emailParamTypesIterator.next();
			if (emailParameterType.getType().equalsIgnoreCase("HOME")) {
				type = Contacts.ContactMethodsColumns.TYPE_HOME;
			} else if (emailParameterType.getType().equalsIgnoreCase("WORK")) {
				type = Contacts.ContactMethodsColumns.TYPE_WORK;
			} else if (emailParameterType.getType().equalsIgnoreCase("PREF")) {
				preferred = true;

			} else if (emailParameterType.getType().equalsIgnoreCase("OTHER")) {
				type = Contacts.ContactMethodsColumns.TYPE_OTHER;
			} else {
				type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
				label = emailParameterType.getType();
			}
			cv.put(Contacts.ContactMethods.TYPE, type);
		}

		while (emailExtendedParametersIterator.hasNext()) {
			XEmailParameterType emailParameterType = emailExtendedParametersIterator
					.next();
			type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
			label = emailParameterType.getXtendedTypeName();
			cv.put(Contacts.ContactMethods.TYPE, type);
			cv.put(Contacts.ContactMethods.LABEL, label);
		}

		cv.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL);

		cv.put(Contacts.ContactMethods.ISPRIMARY, preferred ? 1 : 0);
		cv.put(Contacts.ContactMethods.PERSON_ID, contact.getID());

		return cv;
	}

	//
	// private ContentValues getOrganizationCV(Contact contact, Contact.OrgData
	// org) {
	//
	// if (StringUtil.isNullOrEmpty(org.company)
	// && StringUtil.isNullOrEmpty(org.title)) {
	// return null;
	// }
	// ContentValues cv = new ContentValues();
	//
	// cv.put(Contacts.Organizations.COMPANY, org.company);
	// cv.put(Contacts.Organizations.TITLE, org.title);
	// cv.put(Contacts.Organizations.TYPE, org.type);
	// cv.put(Contacts.Organizations.PERSON_ID, contact._id);
	// if (org.customLabel != null) {
	// cv.put(Contacts.Organizations.LABEL, org.customLabel);
	// }
	//
	// return cv;
	// }
	//

	private ContentValues getNoteCV(VCard contact) {
		ContentValues cv = new ContentValues();
		StringBuffer allnotes = new StringBuffer();
		// if(null!=contact.getBirthDay())
		// if ( null!=contact.getBirthDay().getBirthday() ) {
		// allnotes.append("BDAY").append(" ")
		// .append(contact.getBirthDay().getBirthday().getTimeInMillis());
		// }
		if (contact.getNotes().hasNext()) {

			allnotes.append(contact.getNotes().next().getNote());
			allnotes.append("\n");
		}

		if (allnotes.length() > 0)
			cv.put(Contacts.People.NOTES, allnotes + "");
		return cv;

	}

	private ContentValues getPeopleCV(VCard contact) {
		ContentValues cv = new ContentValues();

		StringBuffer fullname = new StringBuffer();
		if (contact.getFormattedName().getFormattedName() != null)
			if (contact.getFormattedName() != null)
				fullname.append(contact.getFormattedName().getFormattedName());
			else {

				String honorificPrefixes = contact.getName()
						.getHonorificPrefixes().next();
				if (honorificPrefixes != null)
					fullname.append(honorificPrefixes);
				if (contact.getName().getGivenName() != null) {
					if (fullname.length() > 0)
						fullname.append(" ");
					fullname.append(contact.getName().getGivenName());
				}
				// if (contact.midNames != null) {
				// if (fullname.length() > 0)
				// fullname.append(" ");
				// fullname.append(contact.midNames);
				// }
				if (contact.getName().getFamilyName() != null) {
					if (fullname.length() > 0)
						fullname.append(" ");
					fullname.append(contact.getName().getFamilyName());
				}
				String honorificPostfixes = contact.getName()
						.getHonorificSuffixes().next();
				if (honorificPostfixes != null) {
					if (fullname.length() > 0)
						fullname.append(" ");
					fullname.append(honorificPostfixes);
				}
			}
		// Use company name if only the company is given.
		String org = contact.getOrganizations().getOrganizations().next();
		if (fullname.length() == 0
				&& contact.getOrganizations().hasOrganizations() && org != null)
			fullname.append(org);

		cv.put(Contacts.People.NAME, fullname.toString());

		if (!net.sourceforge.cardme.util.StringUtil.isNullOrEmpty(contact
				.getID())) {
			cv.put(Contacts.People._ID, contact.getID());
		}

		return cv;
	}

}