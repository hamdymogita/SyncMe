package com.itworx.syncme.ui.views;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.itworx.syncme.R;
import com.itworx.syncme.ui.customs.DefaultActivity;

public class RegisterActivity extends DefaultActivity {

	EditText userNameEditTextView, passwordEditTextView,
			confirmPasswordEditTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.register);
		super.onCreate(savedInstanceState);

		userNameEditTextView = (EditText) findViewById(R.id.userNameEditText);
		passwordEditTextView = (EditText) findViewById(R.id.PasswordEditText);
		confirmPasswordEditTextView = (EditText) findViewById(R.id.ConfirmPasswordEditText);
		Button btnRegister = (Button) findViewById(R.id.registerButton);

		btnRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	

}
