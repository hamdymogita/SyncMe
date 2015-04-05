 package com.itworx.syncme.ui.views;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.itworx.syncme.R;
import com.itworx.syncme.controller.INetworkController;
import com.itworx.syncme.controller.PreferencesController;
import com.itworx.syncme.network.NetworkController;
import com.itworx.syncme.ui.customs.DefaultActivity;
import com.itworx.syncme.ui.customs.ProgressDialogTaskTemplate;

public class GoogleSignInActivity extends DefaultActivity{

	private int requestCode = 100;
	private Account[] GoogleAccounts = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        Toast.makeText(GoogleSignInActivity.this, "oncreat", Toast.LENGTH_LONG).show();
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		GoogleAccounts = accountManager.getAccountsByType("com.google");
		
		int SelectedAccount = 0;
		Bundle b = getIntent().getExtras();
		if(b!=null){
			SelectedAccount = b.getInt("SelectedAccount");
		}
		
		if(SelectedAccount+"" != null){
			if(SelectedAccount == -1){
//				Intent googleIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
//				googleIntent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {ContactsContract.AUTHORITY });
//			//	googleIntent.setPackage("com.google.android.gsf);
//				googleIntent.setClassName( "com.google.android.gsf", "com.google.android.gsf.login.AccountIntroActivity" );
//		
//				startActivityForResult(googleIntent, requestCode);
//			//	}catch (Exception e) {
//				//	this.finish();
//			//	}
			/*	 AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
			     helpBuilder.setTitle("Add Gmail account");
			     helpBuilder.setMessage("These options rely on a Gmail account, but you  seem to have one configured. Would you like to configure one now?");

			     helpBuilder.setPositiveButton("Yes",
			     new DialogInterface.OnClickListener() {
			         //@Override
			         public void onClick(DialogInterface dialog, int which) {
			             //try/ catch block was here*/
				try{
			             AccountManager acm = AccountManager.get(getApplicationContext());
			             acm.addAccount("com.google", null, null, null, GoogleSignInActivity.this, 
			             null, null);
			             Toast.makeText(GoogleSignInActivity.this, "1", Toast.LENGTH_LONG).show();
				}catch(Exception e){
					this.finish();
					 Toast.makeText(GoogleSignInActivity.this, "2", Toast.LENGTH_LONG).show();
				}
			        /*     System.out.print("TTT:");
			            }
			     });

			     helpBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
			             // close the dialog, return to activity
			         }
			     });    

			     AlertDialog helpDialog = helpBuilder.create();
			     helpDialog.show();*/
			}else{
				 Toast.makeText(GoogleSignInActivity.this, "3", Toast.LENGTH_LONG).show();
				if(GoogleAccounts != null)
					LoginUsingGoogle(GoogleAccounts[SelectedAccount]);
				 Toast.makeText(GoogleSignInActivity.this, "4", Toast.LENGTH_LONG).show();
			}
		}
		
		//initUI();
		
		super.onCreate(savedInstanceState);
	}

	
	private void LoginUsingNewAccount() {
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		 Account[] GoogleAccounts_updated = accountManager.getAccountsByType("com.google");
		
		if(GoogleAccounts != null){
			for(int i=0; i<GoogleAccounts_updated.length; i++){
				boolean found = false;
				Account account = GoogleAccounts_updated[i];
				
				for(int j=0; j<GoogleAccounts.length; j++){
					if(account.equals(GoogleAccounts[j])){
						 found = true;
						 break;
					}
				}
				if(!found){
					LoginUsingGoogle(GoogleAccounts[i]);
					 break;
				}
			}
			
		}
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Toast.makeText(GoogleSignInActivity.this, "onActivity Result", Toast.LENGTH_LONG).show();
		if(requestCode == 100){
			
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}



	protected void LoginUsingGoogle(Account account) {

	/*	 "oauth2:https://www.googleapis.com/auth/userinfo.email oauth2:https://www.googleapis.com/auth/userinfo.userinfo",            // Auth scope
			    */
		Bundle options = new Bundle();
        options.putString("client_id", "3367730884424-linic1nhkaojgnil6ln4u7l007oafim7.apps.googleusercontent.com");
		AccountManager accountManager = AccountManager.get(this);
	    String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
	    accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, options,
	                   this, new AccountManagerCallback<Bundle>() {
	                        public void run(AccountManagerFuture<Bundle> future) {
	                    
	                             String token;
								try {
									token = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
		                            System.out.println("token " + token);
		                            Toast.makeText(GoogleSignInActivity.this,"token = "+token, Toast.LENGTH_LONG).show();
		                            new LoginUsingGoogleTask().execute(token);
		                            
								} catch (OperationCanceledException e) {
									 Toast.makeText(GoogleSignInActivity.this, "1"+ e.getMessage(), Toast.LENGTH_LONG).show();
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (AuthenticatorException e) {
									// TODO Auto-generated catch block
									 Toast.makeText(GoogleSignInActivity.this, "2"+ e.getMessage(), Toast.LENGTH_LONG).show();
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									 Toast.makeText(GoogleSignInActivity.this, "3"+ e.getMessage(), Toast.LENGTH_LONG).show();
									e.printStackTrace();
								}

	                            }
	                        }, null);
	}
	
	class LoginUsingGoogleTask extends ProgressDialogTaskTemplate<String, Void, String>{
		String msg;
		public LoginUsingGoogleTask( ) {

			super(GoogleSignInActivity.this);
			 Toast.makeText(GoogleSignInActivity.this, "1ss", Toast.LENGTH_LONG).show();
		}

		@Override
		protected String inBackground(String... params) throws Exception {
			INetworkController networkController = NetworkController.getInstance(GoogleSignInActivity.this);

			String responseResult = networkController.loginUsingGoogle(params[0]);

			 Toast.makeText(GoogleSignInActivity.this, "response "+ responseResult, Toast.LENGTH_LONG).show();
			if (!responseResult
					.equalsIgnoreCase((com.itworx.syncme.network.Connection.FAILED)
							+ "")) {
				
				PreferencesController Prefs =PreferencesController.getInstance();
				Prefs.saveValue(com.itworx.common.utils.Constants.TOKEN, responseResult);
				
				msg = getString(R.string.welcome_syncMe);

				Intent intentHome = new Intent(GoogleSignInActivity.this,
						HomeActivity.class);
				startActivity(intentHome);
				finish();
			} else
				msg = getString(R.string.please_try_again);
			
			return responseResult;
		}

		@Override
		protected void postExecute(String result) throws Exception {
			Toast.makeText(GoogleSignInActivity.this, msg, Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	protected void onRestart() {

        Toast.makeText(GoogleSignInActivity.this, "OnRestart", Toast.LENGTH_LONG).show();

        LoginUsingNewAccount();
        Toast.makeText(GoogleSignInActivity.this, "111", Toast.LENGTH_LONG).show();
        
		super.onRestart();
	}


	@Override
	protected void onResume() {


        Toast.makeText(GoogleSignInActivity.this, "onResume", Toast.LENGTH_LONG).show();
		super.onResume();
	}
	
	
}
