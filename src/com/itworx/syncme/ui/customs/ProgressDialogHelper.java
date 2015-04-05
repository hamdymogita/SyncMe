package com.itworx.syncme.ui.customs;

import android.app.ProgressDialog;
import android.content.Context;

import com.itworx.syncme.R;

/**
 * @author mcherri
 *
 */
public class ProgressDialogHelper {
	private static final ProgressDialogHelper INSTANCE = new ProgressDialogHelper(); 
	
	private ProgressDialogHelper() {
		
	}
	
	public static ProgressDialogHelper get() {
		return INSTANCE;
	}
	
	public ProgressDialog create(Context context) {
		ProgressDialog dialog = new ProgressDialog(context, R.style.ThemeDialog);
		dialog.setMessage(context.getString(R.string.dialog_loading));
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true); 
		return dialog;
	}
}
