package com.itworx.syncme.ui.customs;

import android.app.Dialog;

public abstract class ProgressDialogTaskTemplate<Params, Progress, Result> extends TaskTemplate<Params, Progress, Result> {
	
	public ProgressDialogTaskTemplate(ProgressDialogTaskMaintainer activity) {
		super(activity);
		
		activity.createProgressDialog();
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		((ProgressDialogTaskMaintainer) maintainer).showDialog();
	}

	@Override
	protected void onCancelled(Result result) {
		((ProgressDialogTaskMaintainer) maintainer).dismissDialog();
		super.onCancelled(result);
	}

	@Override
	protected void onPostExecute(Result result) {
		
		try {
			super.onPostExecute(result);
		} finally {
			((ProgressDialogTaskMaintainer) maintainer).dismissDialog();
		}
	}
	
	public interface ProgressDialogTaskMaintainer extends TaskTemplate.TaskMaintainer {
		void createProgressDialog();
		void showDialog();
		void dismissDialog();
		Dialog getDialog();
	}

}