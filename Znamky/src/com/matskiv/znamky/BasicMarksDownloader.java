package com.matskiv.znamky;

import java.util.Hashtable;

import javax.security.auth.login.LoginException;

import org.apache.http.HttpException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class BasicMarksDownloader {
	
	public static final String SERVER = "https://www.sspbrno.cz/ISAS/";

	public static final String DELIMITER = "█";
	
	protected String which_half;
	protected String userName;
	protected String password;
	protected String server;
	protected String cookie;
	protected Boolean logged = true;
	protected DefaultHttpClient httpClient;
	protected Context cxt;
	
	BasicMarksDownloader(Context cxt) {
		this.cxt = cxt;
		loadPreferences();
	}

	protected abstract Hashtable<Integer, Mark> run () throws LoginException, HttpException;

	protected void loadPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
		which_half = prefs.getString(PreferenceActivity.WHICH_HALF, "1");
		userName = prefs.getString(PreferenceActivity.USER_NAME, "");
		password = prefs.getString(PreferenceActivity.USER_PASSWORD, "");
		server = SERVER;
	}
	
	protected abstract int getWeight(String examType);

	public static String getServer(Context cxt) {
		return SERVER;
	}
	
}