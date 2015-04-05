package com.itworx.syncme.ui.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.itworx.common.utils.Constants;
import com.itworx.syncme.R;
import com.itworx.syncme.controller.INetworkController;
import com.itworx.syncme.controller.PreferencesController;
import com.itworx.syncme.model.AuthenticationData;
import com.itworx.syncme.network.ConnectionException;
import com.itworx.syncme.network.NetworkController;
import com.itworx.syncme.ui.customs.DefaultActivity;
import com.itworx.syncme.ui.customs.ProgressDialogTaskTemplate;

public class FacebookSigninActivity extends DefaultActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Session.openActiveSession(FacebookSigninActivity.this, true,
				new Session.StatusCallback() {

					@Override
					public void call(final Session session, SessionState state,
							Exception exception) {
						// TODO Auto-generated method stub

						if (session.isOpened()) {
						}
						Request.executeMeRequestAsync(session,
								new Request.GraphUserCallback() {

									@Override
									public void onCompleted(GraphUser user,
											Response response) {
										if (user != null) {

											new LoginTask().execute(session
													.getAccessToken());
										}

									}
								});

					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	class LoginTask extends
			ProgressDialogTaskTemplate<String, Void, AuthenticationData> {
		String msg;
		private String responseResult;

		public LoginTask() {
			super(FacebookSigninActivity.this);

		}

		@Override
		protected AuthenticationData inBackground(String... params)
				throws Exception, ConnectionException {
			try {
				INetworkController networkController = NetworkController
						.getInstance(FacebookSigninActivity.this);

				responseResult = networkController
						.loginUserWithFacebook(params[0]);

			} catch (MalformedURLException e) {

				msg = getString(R.string.please_try_again);

				e.printStackTrace();
			} catch (ProtocolException e) {
				msg = getString(R.string.please_try_again);
				e.printStackTrace();
			} catch (IOException e) {
				msg = getString(R.string.please_try_again);
				e.printStackTrace();
			} catch (ConnectionException e) {
				msg = getString(R.string.no_internet_please_try_again);
			}

			return null;
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

				setResult(Constants.CLOSE);
				finish();

			} else {
				msg = getString(R.string.invalid_username_or_password);
				Toast.makeText(FacebookSigninActivity.this, msg,
						Toast.LENGTH_LONG).show();
			}

		}
	}

}
