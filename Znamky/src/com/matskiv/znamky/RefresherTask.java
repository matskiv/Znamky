package com.matskiv.znamky;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class RefresherTask extends AsyncTask<Context, Integer, Refresher.STATUS> {
	
	private StatusCallbackable callbackObject;
	private Refresher refresher;

	public RefresherTask(StatusCallbackable callbackObject) {
		super();
		this.callbackObject = callbackObject;
		refresher = new Refresher();
	}
	
	@Override
	protected Refresher.STATUS doInBackground(Context... c) {
		APIClient client = new APIClient(c[0]);
		client.registerDevice();
		return refresher.run(c[0]);
	}
	
	protected void onProgressUpdate(Integer... progress) {
	}
	
	@Override
	protected void onPostExecute(Refresher.STATUS status) {
		callbackObject.statusCallback(status);
	}
	
	public boolean isNetworkAvailable(Context cxt) {
		ConnectivityManager cm = (ConnectivityManager) cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
	
}