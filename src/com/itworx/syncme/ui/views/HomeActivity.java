package com.itworx.syncme.ui.views;

import java.util.ArrayList;

import org.springframework.web.client.HttpClientErrorException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.itworx.common.utils.Constants;
import com.itworx.syncme.R;
import com.itworx.syncme.controller.INetworkController;
import com.itworx.syncme.controller.PreferencesController;
import com.itworx.syncme.model.AuthenticationData;
import com.itworx.syncme.model.VCard;
import com.itworx.syncme.network.NetworkController;
import com.itworx.syncme.ui.customs.DefaultActivity;
import com.itworx.syncme.ui.customs.ProgressDialogTaskTemplate;

public class HomeActivity extends DefaultActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.home);
		super.onCreate(savedInstanceState);

		initUI();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(HomeActivity.this, SettingActivity.class));
			return true;
		}
		return false;
	}

	private void initUI() {
		Button btnSendContact = (Button) findViewById(R.id.butSendContact);
		Button btnGetContact = (Button) findViewById(R.id.butGetContact);

		btnSendContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new ContactsTaskSend().execute();
			}
		});

		btnGetContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new getContactsTask().execute();
			}
		});

	}

	// / get contacts from server and import it to phone book
	class getContactsTask extends
			ProgressDialogTaskTemplate<AuthenticationData, Void, Void> {
		String msg;

		// VCardEngine vCardEngine = null;
		public getContactsTask() {
			super(HomeActivity.this);

		}

		@Override
		protected Void inBackground(AuthenticationData... params)
				throws Exception {
			INetworkController networkController = NetworkController
					.getInstance(HomeActivity.this);
			// try {
			String TokenKey = "";
			PreferencesController Prefs = PreferencesController.getInstance();
			TokenKey = Prefs.getValue(Constants.TOKEN);

			if (TokenKey != null) {
				/*
				 * VCard[] contacts = networkController
				 * .getAllUserContacts(((AuthenticationData) params[0])
				 * .getUsername());
				 */
				try {
					VCard[] contacts = networkController
							.getAllUserContacts(TokenKey);
					for (int i = 0; i < contacts.length; i++)
						contacts[i].addContact(HomeActivity.this);
					msg = "done sync";

				} catch (HttpClientErrorException e) {
					msg = "login";
				}

			}

			// /////// cardme testing//////
			//
			//
			// vCardEngine = new VCardEngine();
			// vCardEngine.setCompatibilityMode(CompatibilityMode.RFC2426);
			// net.sourceforge.cardme.vcard.VCard vcard=
			// vCardEngine.parseVCard(contacts[0].getVCardData());
			//
			// vcard.getName().getFamilyName();
			// Log.d("getCountryName",
			// vcard.getAddresses().next().getCountryName());
			// Log.d("getFamilyName", vcard.getName().getFamilyName());
			// Log.d("getOrganizations",
			// vcard.getOrganizations().getOrganizations().next());

			// ////////////////////////////

			return null;
		}

		@Override
		protected void postExecute(Void result) throws Exception {
			if (msg.equals("login")) {
				goToSignInActivity();
			} else
				Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_LONG)
						.show();

		}
	}

	class ContactsTaskSend extends
			ProgressDialogTaskTemplate<AuthenticationData, Void, Void> {
		String msg;

		public ContactsTaskSend() {
			super(HomeActivity.this);

		}

		@Override
		protected Void inBackground(AuthenticationData... params)
				throws Exception {
			INetworkController networkController = NetworkController
					.getInstance(HomeActivity.this);

			ArrayList<VCard> contacts = new VCard()
					.getContacts(HomeActivity.this);

			String TokenKey = "";
			PreferencesController Prefs = PreferencesController.getInstance();
			TokenKey = Prefs.getValue(Constants.TOKEN);

			if (TokenKey != null) {
				try {
					networkController.addUserContacts(TokenKey, contacts);

					msg = "done sending all your contacts to server";
				} catch (HttpClientErrorException e) {
					msg = "login";
				}

			}
			return null;
		}

		@Override
		protected void postExecute(Void result) throws Exception {

			if (msg.equals("login")) {

				goToSignInActivity();
			} else
				Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_LONG)
						.show();

		}

	}

	private void goToSignInActivity() {
		PreferencesController prefs = PreferencesController.getInstance();
		prefs.saveValue(com.itworx.common.utils.Constants.TOKEN,
				com.itworx.common.utils.Constants.NON);
		finish();
		startActivity(new Intent(HomeActivity.this, SignInActivity.class));

	}
}
