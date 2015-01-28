package com.matskiv.znamky;

import android.content.Context;
import android.os.AsyncTask;

import com.matskiv.znamky.APIClient.*;

public class APIClientTask extends AsyncTask<METHOD, Integer, STATUS> {
	
	private APIClient.ApiCallbackable callbackObject;
	private APIClient client;

	public APIClientTask(Context cxt, ApiCallbackable callbackObject) {
		super();
		this.callbackObject = callbackObject;
		client = new APIClient(cxt);
	}
	
	@Override
	protected STATUS doInBackground(METHOD... methods) {
		for (METHOD m : methods) {
			switch (m) {
				case REGISTERDEVICE:
					return client.registerDevice();
				
				case COMMANDS:
					return client.retrieveComands();
			
				case REPORT:
					break;
					
				default:
					break;
			}
		}
		return STATUS.OK;
	}
	
	protected void onProgressUpdate(Integer... progress) {
	}
	
	@Override
	protected void onPostExecute(STATUS status) {
		callbackObject.apiCallback(status);
	}
}