package com.matskiv.znamky;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mastkiv.znamky.R;
import com.matskiv.znamky.Refresher.STATUS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PreferenceActivity extends SherlockPreferenceActivity implements StatusCallbackable {

	public static final String PREFERENCES		= "com.matskiv.isasclient";
	
	public static final String FIRSTRUN			= "first_run";
	public static final String JUST_BACK	 	= "just_back";
	
	public static final String WHICH_HALF		= "which_half";
	public static final String USER_NAME		= "user_name";
	public static final String USER_PASSWORD	= "user_password";
	public static final String LAST_REFRESH		= "last_refresh";
	
	private boolean firstRun;

	private Button button;
	private ProgressBar progress;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.login_preferences);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		firstRun = prefs.getBoolean(FIRSTRUN, true);
		
		if (firstRun) {
			button = new Button(this);
			progress = new ProgressBar(this);
			ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.WRAP_CONTENT, ListView.LayoutParams.WRAP_CONTENT);
			progress.setLayoutParams(params);
			
			button.setText(getApplicationContext().getString(R.string.preference_button_first_start));
			getListView().addFooterView(button);
			
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getListView().removeFooterView(button);
					getListView().addFooterView(progress);
					
					RefresherTask r = new RefresherTask(PreferenceActivity.this);
					r.execute(getApplicationContext());
				}
			});
		} else {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
	}
	
	@Override
	public void statusCallback(STATUS status) {
		Context cxt = this.getApplicationContext();
		Toast toast = new Toast(cxt);
		switch (status){
			case NOINTERNET:
				toast = Toast.makeText(cxt, cxt.getString(R.string.refresh_fail_internet), Toast.LENGTH_LONG);
				break;
				
			case LOGINERROR:
				toast = Toast.makeText(cxt, cxt.getString(R.string.refresh_fail_login), Toast.LENGTH_LONG);
				break;
				
			case HTTPERROR:
				toast = Toast.makeText(cxt, cxt.getString(R.string.refresh_fail_download), Toast.LENGTH_LONG);
				break;
				
			case ANYMARKS:
			case ANYNEWMARKS:
			case ALLOK:
				firstStartCompleted();
				return;
		}
		toast.show();
		
		getListView().removeFooterView(progress);
		getListView().addFooterView(button);
	}
	
	private void firstStartCompleted() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = prefs.edit();
		editor.putBoolean(FIRSTRUN, false);
		editor.commit();
		
		TimeAlarm.scheduleRefresh(getApplicationContext(), TimeAlarm.WIFIUPDATE);
		
		Intent prefsIntent = new Intent(PreferenceActivity.this, MainActivity.class);
		startActivity(prefsIntent);
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (firstRun) {
					Intent intent = new Intent(this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
				} else {
					if (getIntent().getBooleanExtra(JUST_BACK, false)) {
						onBackPressed();
					} else {
						Intent intent = new Intent(this, MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
					}
				}
				break;
		}
		return super.onOptionsItemSelected(item); 
	}
	
	public static SharedPreferences getPreferences(Context cxt) {
		return cxt.getSharedPreferences(PreferenceActivity.PREFERENCES, 4);
	}
	
}