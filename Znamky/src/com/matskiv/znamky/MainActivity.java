package com.matskiv.znamky;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.mastkiv.znamky.R;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends SherlockFragmentActivity implements StatusCallbackable {	
	
	private ViewPager mainPager;
	private boolean refreshInProgress = false;
	private String whichHalf;
	private String userName;
	private long lastUpdate;
	private SharedPreferences prefs;
	private SharedPreferences prefsMP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefsMP = PreferenceActivity.getPreferences(getApplicationContext());
		Boolean firstRun = prefs.getBoolean(PreferenceActivity.FIRSTRUN, true);
		
		if(firstRun){
			Intent prefsIntent = new Intent(this, PreferenceActivity.class);
			prefsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(prefsIntent);
			
			this.finish();
		}
		whichHalf = prefs.getString(PreferenceActivity.WHICH_HALF, "1");
		userName = prefs.getString(PreferenceActivity.USER_NAME, "");
		lastUpdate = prefsMP.getLong(PreferenceActivity.LAST_REFRESH, 0);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setSupportProgressBarIndeterminateVisibility(false);      
		mainPager = (ViewPager)findViewById(R.id.pager);
		mainPager.setAdapter(new MainPagerAdapter(this, getSupportFragmentManager()));
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(mainPager);
		mainPager.setCurrentItem(0);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (whichHalf != prefs.getString(PreferenceActivity.WHICH_HALF, "1") 
				|| userName != prefs.getString(PreferenceActivity.USER_NAME, "")) {
			
			whichHalf = prefs.getString(PreferenceActivity.WHICH_HALF, "1");
			userName = prefs.getString(PreferenceActivity.USER_NAME, "");
			refresh();
		} else if (lastUpdate != prefs.getLong(PreferenceActivity.LAST_REFRESH, 0)) {
			lastUpdate = prefsMP.getLong(PreferenceActivity.LAST_REFRESH, 0);
			((MainPagerAdapter) mainPager.getAdapter()).refresh();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ab_refresh:
				refresh();
				break;
				
			case R.id.ab_preferences:
				Intent prefsIntent = new Intent(this, PreferenceActivity.class);
				prefsIntent.putExtra(PreferenceActivity.JUST_BACK, true);
				startActivity(prefsIntent);
				break;
				
		}
		return super.onOptionsItemSelected(item); 
	}
	
	private void refresh() {
		if (refreshInProgress) {
			return;
		}
		TimeAlarm.scheduleRefresh(getApplicationContext(), TimeAlarm.WIFIUPDATE);
		
		refreshInProgress = true;
		setABP(true);
		setProgress(0);
		
		SQLiteHelper db = new SQLiteHelper(getApplicationContext());
		db.open();
		db.truncateMarks();
		db.close();
		
		RefresherTask r = new RefresherTask(this);
		r.execute(this.getApplicationContext());
	}
	
	public void setABP(Boolean a) {
		setSupportProgressBarIndeterminateVisibility(a);
	}
	
	@Override
	public void statusCallback(Refresher.STATUS status) {
		Context cxt = this.getApplicationContext();
		Toast toast = new Toast(cxt);
		switch (status){
			case ALLOK:
				toast = Toast.makeText(cxt, this.getString(R.string.refresh_ok), Toast.LENGTH_LONG);
				((MainPagerAdapter) mainPager.getAdapter()).refresh();
				break;
			case NOINTERNET:    	
				toast = Toast.makeText(cxt, this.getString(R.string.refresh_fail_internet), Toast.LENGTH_LONG);
				break;
			case ANYMARKS:
				toast = Toast.makeText(cxt, this.getString(R.string.refresh_fail_any), Toast.LENGTH_LONG);
				((MainPagerAdapter) mainPager.getAdapter()).refresh();
				break;
			case ANYNEWMARKS:
				toast = Toast.makeText(cxt, this.getString(R.string.refresh_fail_any_new), Toast.LENGTH_LONG);
				break;
			case LOGINERROR:
				toast = Toast.makeText(cxt, this.getString(R.string.refresh_fail_login), Toast.LENGTH_LONG);
				break;
			case HTTPERROR:
				toast = Toast.makeText(cxt, this.getString(R.string.refresh_fail_download), Toast.LENGTH_LONG);
				break;
		}
		setABP(false);
		toast.show();
		refreshInProgress = false;
	}

}