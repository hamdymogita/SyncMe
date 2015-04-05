package com.itworx.syncme.ui.customs;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

@SuppressLint("NewApi")
public
abstract class TaskTemplate<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	protected TaskMaintainer maintainer;

	public TaskTemplate(TaskMaintainer maintainer) {
		super();
		
		this.maintainer = maintainer;
		maintainer.add(this);

	}

	@Override
	protected Result doInBackground(Params... params) {
		try {
			return inBackground(params);
		} catch (Exception e) {
			// FIXME: should show a toast.
			e.printStackTrace();
			cancel(false);
		}
		return null;
	}
	
	@Override
	protected void onCancelled(Result result) {
		maintainer.remove(this);
		super.onCancelled(result);
	}

	@Override
	protected void onPostExecute(Result result) {
		try {
			postExecute(result);
		} catch (Exception e) {
			// FIXME: should show a toast.
			e.printStackTrace();
		} finally {
			maintainer.remove(this);
		}
	}

	protected abstract Result inBackground(Params... params) throws Exception;
	protected abstract void postExecute(Result result) throws Exception;
	
	public interface TaskMaintainer {
		void add(AsyncTask<? extends Object, ? extends Object, ? extends Object> task);
		void remove(AsyncTask<? extends Object, ? extends Object, ? extends Object> task);
	}
}