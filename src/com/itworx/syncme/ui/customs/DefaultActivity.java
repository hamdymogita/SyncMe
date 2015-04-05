package com.itworx.syncme.ui.customs;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;

import com.itworx.syncme.ui.customs.ProgressDialogTaskTemplate.ProgressDialogTaskMaintainer;

public class DefaultActivity extends Activity implements
		ProgressDialogTaskMaintainer {
	private AtomicInteger dialogRefCount = new AtomicInteger();
	protected ProgressDialog dialog = null;
	final Set<AsyncTask<? extends Object, ? extends Object, ? extends Object>> tasks = new HashSet<AsyncTask<? extends Object, ? extends Object, ? extends Object>>();

	public DefaultActivity() {
		super();

	}

	@Override
	public void createProgressDialog() {
	
		if (dialog == null) {
			dialog = ProgressDialogHelper.get().create(this);
		}
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		loadLanguage();

	}

	@Override
	public void showDialog() {
		if (dialog == null) {
			return;
		}

		if (dialogRefCount.incrementAndGet() >= 1) {
			dialog.show();
		}
	}

	@Override
	public void dismissDialog() {
		if (dialog == null) {
			return;
		}

		if (dialogRefCount.decrementAndGet() < 1) {
			dialog.dismiss();
		}
	}

	@Override
	public Dialog getDialog() {
		return dialog;
	}

	@Override
	public void add(
			AsyncTask<? extends Object, ? extends Object, ? extends Object> task) {
		tasks.add(task);
	}

	@Override
	public void remove(
			AsyncTask<? extends Object, ? extends Object, ? extends Object> task) {
		tasks.remove(task);
	}

	protected void loadLanguage() {

		String languageToLoad = "en";
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, null);

	}

	@Override
	protected void onDestroy() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}

		for (AsyncTask<? extends Object, ? extends Object, ? extends Object> task : tasks) {
			if (task != null) {
				task.cancel(true);
			}
		}

		super.onDestroy();
	}

	/*
	 * mcherri Use TaskTemplate instead.
	 */
	@Deprecated
	protected static class Task<Params, Progress, Result> extends
			TaskTemplate<Params, Progress, Result> {
		private final BackgroundCallable<Params, Result> background;
		private final PostCallable<Result> post;

		public Task(TaskTemplate.TaskMaintainer activity,
				BackgroundCallable<Params, Result> background,
				PostCallable<Result> post) {
			super(activity);

			this.background = background;
			this.post = post;
		}

		@Override
		protected Result inBackground(Params... params) throws Exception {
			if (background == null) {
				return null;
			}
			return background.call(params);
		}

		@Override
		protected void postExecute(Result result) throws Exception {
			if (post != null) {
				post.call(result);
			}
		}

	}

	/*
	 * mcherri Use ProgressDialogTaskTemplate instead.
	 */
	@Deprecated
	protected static class ProgressDialogTask<Params, Progress, Result> extends
			Task<Params, Progress, Result> {

		public ProgressDialogTask(
				ProgressDialogTaskTemplate.ProgressDialogTaskMaintainer activity,
				BackgroundCallable<Params, Result> background,
				PostCallable<Result> post) {
			super(activity, background, post);
			activity.createProgressDialog();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			((ProgressDialogTaskTemplate.ProgressDialogTaskMaintainer) maintainer)
					.showDialog();
		}

		@Override
		protected void onCancelled(Result result) {
			((ProgressDialogTaskTemplate.ProgressDialogTaskMaintainer) maintainer)
					.dismissDialog();
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Result result) {

			try {
				super.onPostExecute(result);
			} finally {
				((ProgressDialogTaskTemplate.ProgressDialogTaskMaintainer) maintainer)
						.dismissDialog();
			}
		}
	}

	@Deprecated
	protected interface BackgroundCallable<Params, Result> {
		Result call(Params... params) throws Exception;
	}

	@Deprecated
	protected interface PostCallable<Result> {
		void call(Result result) throws Exception;
	}

}
