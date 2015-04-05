/**
 * 
 */
package com.itworx.syncme.ui.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.CompatibilityMode;
import net.sourceforge.cardme.vcard.VCard;

import org.springframework.web.client.HttpClientErrorException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itworx.common.utils.Constants;
import com.itworx.syncme.R;
import com.itworx.syncme.controller.INetworkController;
import com.itworx.syncme.controller.PreferencesController;
import com.itworx.syncme.model.AuthenticationData;
import com.itworx.syncme.model.User;
import com.itworx.syncme.network.ConnectionException;
import com.itworx.syncme.network.NetworkController;
import com.itworx.syncme.sync.ContactController;
import com.itworx.syncme.ui.customs.DefaultActivity;
import com.itworx.syncme.ui.customs.ProgressDialogTaskTemplate;
import com.vcardio.funambol.util.Controller;
import com.vcardio.funambol.util.TwitterConstant;

public class SignInActivity extends DefaultActivity {

	private EditText usernameEditTextView, passwordEditTextView,
			confirmPasswordEditTextView;
	private Twitter mTwitter;
	private RequestToken mRequestToken;
	private int googleRequestCode = 101;
	private TextView titleTextView;
	private Button signInRegisterButton;
	private Button signInButtonView;
	private Button registerButtonView;
	private boolean isSigninView = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		PreferencesController Prefs = PreferencesController.getInstance();
		String token = Prefs.getValue(com.itworx.common.utils.Constants.TOKEN);
		if (!token.equals(com.itworx.common.utils.Constants.NON)) {
			finish();
			Intent intentHome = new Intent(SignInActivity.this,
					HomeActivity.class);
			super.onCreate(savedInstanceState);
			startActivity(intentHome);
		} else {
			setContentView(R.layout.login);
			super.onCreate(savedInstanceState);
			initUI();
		}

	}

	private void initUI() {
		final Resources res = SignInActivity.this.getResources();
		titleTextView = (TextView) findViewById(R.id.titleTextView);
		usernameEditTextView = (EditText) findViewById(R.id.userNameEditText);
		passwordEditTextView = (EditText) findViewById(R.id.PasswordEditText);
		confirmPasswordEditTextView = (EditText) findViewById(R.id.ConfirmPasswordEditText);

		signInRegisterButton = (Button) findViewById(R.id.loginRegisterButton);
		signInRegisterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = usernameEditTextView.getText() + "";
				String password = (passwordEditTextView).getText() + "";
				if (isSigninView) {
					AuthenticationData authenticationData = new AuthenticationData();
					authenticationData.setUsername(username);
					authenticationData.setPassword(password);

					int mesg = authenticationData.checkLoginValidation();
					if (mesg != -1) {
						String message = getString(mesg);

						Toast.makeText(SignInActivity.this, message,
								Toast.LENGTH_LONG).show();
					} else
						new LoginTask().execute(authenticationData);
				} else {
					String confirmPassword = (confirmPasswordEditTextView)
							.getText().toString().trim();

					User user = new User();
					user.setUsername(username);
					user.setPassword(password);
					user.setConfirmPassword(confirmPassword);

					int mesg = user.checkRegisterValidation();
					if (mesg != -1) {
						String message = getString(mesg);

						Toast.makeText(SignInActivity.this, message,
								Toast.LENGTH_LONG).show();
					} else {
						new RegisterTask().execute(user);

					}
				}

			}
		});

		Button signInGoogleButton = (Button) findViewById(R.id.loginUsingGoogleButton);
		signInGoogleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new LoodingGoogle().execute();
			}
		});

		Button loginWithFacebookButton = (Button) findViewById(R.id.loginUsingFacebookButton);
		loginWithFacebookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					NetworkController.getInstance(SignInActivity.this);
					startActivityForResult(new Intent(SignInActivity.this,
							FacebookSigninActivity.class), Constants.FB_FLAG);

				} catch (ConnectionException e) {
					String msg = getString(R.string.no_internet_please_try_again);
					Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		Button signInWithTwitterButton = (Button) findViewById(R.id.loginUsingTwitterButton);
		signInWithTwitterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					NetworkController.getInstance(SignInActivity.this);
				} catch (ConnectionException e) {
					String msg = getString(R.string.no_internet_please_try_again);
					Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG)
							.show();
				}
				ConfigurationBuilder confbuilder = new ConfigurationBuilder();
				Configuration conf = confbuilder
						.setOAuthConsumerKey(TwitterConstant.CONSUMER_KEY)
						.setOAuthConsumerSecret(TwitterConstant.CONSUMER_SECRET)
						.build();
				mTwitter = new TwitterFactory(conf).getInstance();
				TwitterConstant.mTwitter = mTwitter;

				mTwitter.setOAuthAccessToken(null);
				try {
					mRequestToken = mTwitter.getOAuthRequestToken();
					RequestToken request_token = new RequestToken(mRequestToken
							.getToken(), mRequestToken.getTokenSecret());

					Controller.getInstance().createPooledModel("requestToken",
							request_token);
					Intent intent = new Intent(SignInActivity.this,
							TwitterLogin.class);
					intent.putExtra(TwitterConstant.IEXTRA_AUTH_URL,
							mRequestToken.getAuthorizationURL());
					startActivity(intent);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
		});

		signInButtonView = (Button) findViewById(R.id.signInButtonView);
		signInButtonView.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				isSigninView = true;
				titleTextView.setText(res.getString(R.string.sign_in));
				signInRegisterButton.setText(res.getString(R.string.sign_in));
				registerButtonView.setBackgroundDrawable(res
						.getDrawable(R.drawable.button));
				signInButtonView.setBackgroundDrawable(res
						.getDrawable(R.drawable.black_bg));
				registerButtonView.setTextColor(Color.parseColor("#ffffff"));
				signInButtonView.setTextColor(Color.parseColor("#999999"));
				signInButtonView.setText(res.getString(R.string.sign_in));
				confirmPasswordEditTextView.setVisibility(View.INVISIBLE);
				signInButtonView.setClickable(false);
				registerButtonView.setClickable(true);

			}
		});
		registerButtonView = (Button) findViewById(R.id.registerButtonView);
		registerButtonView.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				isSigninView = false;
				titleTextView.setText(res.getString(R.string.register));
				signInRegisterButton.setText(res.getString(R.string.register));

				registerButtonView.setBackgroundDrawable(res
						.getDrawable(R.drawable.black_bg));
				signInButtonView.setBackgroundDrawable(res
						.getDrawable(R.drawable.button));
				signInButtonView.setTextColor(Color.parseColor("#ffffff"));
				registerButtonView.setTextColor(Color.parseColor("#999999"));

				registerButtonView.setText(res.getString(R.string.register));
				confirmPasswordEditTextView.setVisibility(View.VISIBLE);
				signInButtonView.setClickable(true);
				registerButtonView.setClickable(false);

			}
		});

		TextView forgetPasswordButton = (TextView) findViewById(R.id.forgetPasswordTextView);
		forgetPasswordButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				VCardEngine vCardEngine = null;

				vCardEngine = new VCardEngine();
				vCardEngine.setCompatibilityMode(CompatibilityMode.RFC2426);
				StringBuilder sb = new StringBuilder();
				sb.append("BEGIN:VCARD\r\n");
				sb.append("VERSION:3.0\r\n");

				sb.append("BEGIN:VCARD\r\n");
				sb.append("VERSION:3.0\r\n");

				sb.append("N:Khalifa;Dr Test;Ahmed;;Pro \r\n");
				sb.append("FN:Dr Test Ahmed Khalifa, Pro \r\n");
				sb.append("ORG:Bubba Gump Shrimp Co. \r\n");
				sb.append("ORG:it Worx Co. \r\n");
				sb.append("NOTE:hello world \r\n");
				sb.append("EMAIL;TYPE=PREF,WORK:forwww@example.com \r\n");
				sb.append("EMAIL;TYPE=PREF,Home:forrhhh@example.com \r\n");
				sb.append("EMAIL;TYPE=PREF,yasmeen:foryyy@example.com \r\n");
				sb.append("TEL;WORK;VOICE:(111) 5255-1212 \r\n");
				sb.append("TEL;HOME;VOICE:(404) 5535-1212 \r\n");
				sb.append("TEL;mohamed;VOICE:(404) 555-1212 \r\n");
				sb.append("TITLE:Shrimp Man \r\n");
				sb.append("PHOTO;VALUE=URL;TYPE=GIF:http://www.example.com/dir_photos/my_photo.gif \r\n");
				sb.append("BDAY:2013-01-01\r\n");
				sb.append("ADR;TYPE=WORK:;;100 Waters Edge;Baytown;LA;30314;United States of America \r\n");
				sb.append("END:VCARD\r\n");

				try {
					VCard vcard = vCardEngine.parseVCard(sb.toString());

					vcard.getName().getFamilyName();
					Log.d("getCountryName", vcard.getAddresses().next()
							.getCountryName());
					Log.d("getFamilyName", vcard.getName().getFamilyName());
					Log.d("getOrganizations", vcard.getOrganizations()
							.getOrganizations().next());
					ContactController c = new ContactController();
					c.addContact(SignInActivity.this, vcard);
					// VCardImpl vcard =
					// (VCardImpl)vCardEngine.parse(sb.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	class LoginTask
			extends
			ProgressDialogTaskTemplate<AuthenticationData, Void, AuthenticationData> {
		String msg;
		String responseResult;

		public LoginTask() {
			super(SignInActivity.this);

		}

		@Override
		protected AuthenticationData inBackground(AuthenticationData... params)
				throws Exception, ConnectionException {
			try {

				INetworkController networkController = NetworkController
						.getInstance(SignInActivity.this);

				responseResult = networkController.loginUser(params[0]);
				// if(responseResult.equals(Connection.FAILED+""))
				// msg=msg = getString(R.string.);

			} catch (MalformedURLException e) {

				msg = getString(R.string.please_try_again);

				e.printStackTrace();
			} catch (ProtocolException e) {
				msg = getString(R.string.please_try_again);
				e.printStackTrace();
			} catch (IOException e) {
				msg = getString(R.string.please_try_again);
				e.printStackTrace();
			} catch (HttpClientErrorException e) {
				e.printStackTrace();
			} catch (ConnectionException e) {
				msg = getString(R.string.no_internet_please_try_again);
			}
			return params[0];
		}

		@Override
		protected void postExecute(AuthenticationData result) throws Exception {

			if (!responseResult
					.equalsIgnoreCase((com.itworx.syncme.network.Connection.FAILED)
							+ "")) {
				PreferencesController Prefs = PreferencesController
						.getInstance();
				Prefs.saveValue(com.itworx.common.utils.Constants.TOKEN,
						responseResult);

				msg = getString(R.string.welcome_syncMe);

				Intent intentHome = new Intent(SignInActivity.this,
						HomeActivity.class);

				// not used using token
				// intentHome.putExtra("AuthenticationDataResult", result);

				startActivity(intentHome);
				finish();
			} else
				msg = getString(R.string.invalid_username_or_password);
			Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG).show();
		}
	}

	class LoodingGoogle extends ProgressDialogTaskTemplate<Void, Void, Void> {

		public LoodingGoogle() {
			super(SignInActivity.this);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Void inBackground(Void... params) throws Exception {
			return null;
		}

		@Override
		protected void postExecute(Void result) throws Exception {

			// getAvalibleGoogleAccount
			AccountManager accountManager = AccountManager
					.get(getApplicationContext());
			Account[] GoogleAccounts = accountManager
					.getAccountsByType("com.google");
			CharSequence[] AccountsName = new CharSequence[GoogleAccounts.length];
			for (int i = 0; i < GoogleAccounts.length; i++) {
				AccountsName[i] = GoogleAccounts[i].name;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(
					SignInActivity.this);
			builder.setTitle(getString(R.string.sign_in_Google));
			builder.setItems(AccountsName,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int account) {

							Intent intentGoogle = new Intent(
									SignInActivity.this,
									GoogleSignInActivity.class);
							intentGoogle.putExtra("SelectedAccount", account);
							startActivityForResult(intentGoogle,
									googleRequestCode);
							// finish();
						}
					});
			builder.setPositiveButton(R.string.new_google_account,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

							Intent intentGoogle = new Intent(
									SignInActivity.this,
									GoogleSignInActivity.class);
							intentGoogle.putExtra("SelectedAccount", -1);
							startActivityForResult(intentGoogle,
									googleRequestCode);
							// finish();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("--- onActivity ---", requestCode + " " + resultCode + " ");
		if (requestCode == 101) {
			// google
			if (resultCode == 11)
				Toast.makeText(SignInActivity.this,
						getString(R.string.dont_have_gmail), Toast.LENGTH_LONG)
						.show();
			
		}
		if (requestCode == Constants.FB_FLAG)
			if (resultCode == Constants.CLOSE) {
				Intent intentHome = new Intent(SignInActivity.this,
						HomeActivity.class);

				startActivity(intentHome);
				String msg = getString(R.string.welcome_syncMe);
				Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG)
				.show();
				finish();
			}


		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	class RegisterTask extends ProgressDialogTaskTemplate<User, Void, User> {
		String msg;

		public RegisterTask() {
			super(SignInActivity.this);

		}

		@Override
		protected User inBackground(User... params) throws Exception,
				ConnectionException {
			// Connection connection = new Connection();
			// int status = connection.checkConnection(SignInActivity.this);
			// Log.d("status", status + "");
			// if (status != 0) {
			try {
				INetworkController networkController = NetworkController
						.getInstance(SignInActivity.this);

				String responseResult = networkController.register(params[0]);

				if (!responseResult
						.equalsIgnoreCase((com.itworx.syncme.network.Connection.FAILED)
								+ "")) {
					PreferencesController Prefs = PreferencesController
							.getInstance();
					Prefs.saveValue(com.itworx.common.utils.Constants.TOKEN,
							responseResult);
					msg = getString(R.string.registration_success);
				} else
					msg = getString(R.string.user_exist);

			} catch (MalformedURLException e) {

				msg = getString(R.string.registration_failed_please_try_again);

				e.printStackTrace();
			} catch (ProtocolException e) {
				msg = getString(R.string.registration_failed_please_try_again);
				e.printStackTrace();
			} catch (IOException e) {
				msg = getString(R.string.registration_failed_please_try_again);
				e.printStackTrace();
			} catch (ConnectionException e) {
				msg = getString(R.string.no_internet_please_try_again);
			}
			return params[0];
			// } else {
			// msg = getString(R.string.no_internet_please_try_again);
			// return null;
			// }
		}

		@Override
		protected void postExecute(User user) throws Exception {
			if (getString(R.string.registration_success).equals(msg)) {
				clearData();

				SharedPreferences myPrefs = getBaseContext()
						.getSharedPreferences("SyncMEAPP", MODE_PRIVATE);
				SharedPreferences.Editor prefsEditor = myPrefs.edit();
				prefsEditor.putString("user_name", user.getUsername());
				prefsEditor.commit();

			}
			Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG).show();

		}
	}

	/**
	 * clear the data on all fields on register view
	 */
	public void clearData() {
		usernameEditTextView.setText("");
		passwordEditTextView.setText("");
		confirmPasswordEditTextView.setText("");

	}
}
