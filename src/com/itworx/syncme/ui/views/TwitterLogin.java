package com.itworx.syncme.ui.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import com.itworx.syncme.R;
import com.itworx.syncme.controller.INetworkController;
import com.itworx.syncme.controller.PreferencesController;
import com.itworx.syncme.model.AuthenticationData;
import com.itworx.syncme.network.ConnectionException;
import com.itworx.syncme.network.NetworkController;
import com.itworx.syncme.ui.customs.DefaultActivity;
import com.itworx.syncme.ui.customs.ProgressDialogTaskTemplate;
import com.vcardio.funambol.util.Controller;
import com.vcardio.funambol.util.TwitterConstant;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class TwitterLogin extends DefaultActivity {
	public static final String TAG = TwitterLogin.class.getSimpleName();
	WebView webView;

	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.twitter_login);

		webView = (WebView) findViewById(R.id.twitterlogin);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				/*
				 * This call inject JavaScript into the page which just finished
				 * loading.
				 */
				view.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				
			}

			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				boolean result = true;
				if (url != null && url.startsWith(TwitterConstant.CALLBACK_URL)) {
					Uri uri = Uri.parse(url);

					if (uri.getQueryParameter("denied") != null) {
						setResult(RESULT_CANCELED);
						finish();
					}
				} else {
					result = super.shouldOverrideUrlLoading(view, url);
				}
				return result;
			}
		});
		webView.loadUrl(this.getIntent().getExtras()
				.getString(TwitterConstant.IEXTRA_AUTH_URL));
		webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

	}

	

	String pin;

	class MyJavaScriptInterface {
		public void showHTML(String html) {
			String[] result = html.split("<code>");

			if (result.length > 1) {
				String htmlPage = "<html><body>Please Wait </body></html>";
				String mime = "text/html";
				String encoding = "utf-8";
				webView.loadDataWithBaseURL(null, htmlPage, mime, encoding,
						null);

				String[] result1 = result[1].split("</code>");
				pin = result1[0];

				RequestToken requestToken = (RequestToken) Controller
						.getInstance().getPooledModel("requestToken");
				try {
					AccessToken accessToken;
					if (pin.length() > 0) {

						accessToken = TwitterConstant.mTwitter
								.getOAuthAccessToken(requestToken, pin);
					} else {
						accessToken = TwitterConstant.mTwitter
								.getOAuthAccessToken();
					}
					TwitterConstant.mTwitter.setOAuthAccessToken(accessToken);

					SharedPreferences pref = getSharedPreferences(
							TwitterConstant.PREF_NAME, MODE_PRIVATE);
					SharedPreferences.Editor editor = pref.edit();
					editor.putString(TwitterConstant.PREF_KEY_ACCESS_TOKEN,
							accessToken.getToken());
					editor.putString(
							TwitterConstant.PREF_KEY_ACCESS_TOKEN_SECRET,
							accessToken.getTokenSecret());
					editor.commit();

					new LoginTask().execute(accessToken.getToken(),
							accessToken.getTokenSecret());
				} catch (TwitterException e) {

					e.printStackTrace();

				}
			}
		}
	}

	class LoginTask extends
			ProgressDialogTaskTemplate<String, Void, AuthenticationData> {
		String msg;

		public LoginTask() {
			super(TwitterLogin.this);

		}

		@Override
		protected AuthenticationData inBackground(String... params)
				throws Exception ,ConnectionException{
try {
			INetworkController networkController = NetworkController
					.getInstance(TwitterLogin.this);
			
				String responseResult = networkController.loginUserWithTwitter(
						params[0], params[1]);

				if (!responseResult
						.equalsIgnoreCase((com.itworx.syncme.network.Connection.FAILED)
								+ "")) {
					PreferencesController Prefs =PreferencesController.getInstance();
					Prefs.saveValue(com.itworx.common.utils.Constants.TOKEN, responseResult);
					
					msg = getString(R.string.welcome_syncMe);

					Intent intentHome = new Intent(TwitterLogin.this,
							HomeActivity.class);
					startActivity(intentHome);
					finish();
				} else
					msg = getString(R.string.invalid_username_or_password);

			} catch (MalformedURLException e) {

				msg = getString(R.string.please_try_again);

				e.printStackTrace();
			} catch (ProtocolException e) {
				msg = getString(R.string.please_try_again);
				e.printStackTrace();
			} catch (IOException e) {
				msg = getString(R.string.please_try_again);
				e.printStackTrace();
			}catch (ConnectionException e) {
				msg = getString(R.string.no_internet_please_try_again);
			}

			return null;
		}

		@Override
		protected void postExecute(AuthenticationData result) throws Exception {

			Toast.makeText(TwitterLogin.this, msg, Toast.LENGTH_LONG).show();

		}
	}

}