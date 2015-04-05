package com.itworx.syncme.ui.views;


import com.itworx.syncme.R;



import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SettingActivity extends PreferenceActivity{
	
	  @SuppressWarnings("deprecation")
	@Override
	    public void onCreate(Bundle savedInstanceState) {    	
	        super.onCreate(savedInstanceState);        
	        addPreferencesFromResource(R.xml.preferences);        
	    }
	
	  
	  @Override
	    public boolean onCreateOptionsMenu(Menu menu)
	    {
	        MenuInflater menuInflater = getMenuInflater();
	        menuInflater.inflate(R.menu.setting_menu, menu);
	        return true;
	    }

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	switch (item.getItemId()) {
	    		case R.id.save_settings:
	    			finish();
	    			return true;
	    	}
	    	return false;
	    }
}
