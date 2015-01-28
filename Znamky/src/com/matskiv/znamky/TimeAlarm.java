package com.matskiv.znamky;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class TimeAlarm extends BroadcastReceiver {

	final static String INTENT_ACTION = "periodical_update";
	
	final static long WIFIUPDATE = 1800000;
	final static long EDGEUPDATE = 7200000;
	private Context cxt;
	
	@Override
	public void onReceive(Context cxt, Intent intent) {
		this.cxt = cxt;
		String action = intent.getAction();
		
		if (action.equals(TimeAlarm.INTENT_ACTION)) {
			tryRefresh();
		} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			scheduleRefresh(cxt, WIFIUPDATE);
			refresh();
		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (netInfo.isConnected()){
				tryRefresh();
			} 
		}
	}
	
	private void tryRefresh() {
		SharedPreferences prefs = PreferenceActivity.getPreferences(cxt);
		long lastUpdate = prefs.getLong(PreferenceActivity.LAST_REFRESH, 0);
		if (checkDate(Calendar.getInstance())) {
			if ((lastUpdate+WIFIUPDATE) < System.currentTimeMillis() && isWifiConnected(cxt)) {
				scheduleRefresh(cxt, WIFIUPDATE);
				refresh();
			} else {
				if ((lastUpdate+EDGEUPDATE) < System.currentTimeMillis()) {
					scheduleRefresh(cxt, EDGEUPDATE);
					refresh();
				} else {
					scheduleRefresh(cxt, lastUpdate-System.currentTimeMillis()+EDGEUPDATE);
				}
			}
		}
	}
	
	private boolean checkDate(Calendar c) {
		if (c.get(Calendar.MONTH) == Calendar.JULY || c.get(Calendar.MONTH) == Calendar.AUGUST) {
			return false;
		} else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			return false;
		} else if (c.get(Calendar.HOUR_OF_DAY) < 7 || c.get(Calendar.HOUR_OF_DAY) > 17) {
			return false;
		} else {
			return true;
		}
	}
	
	private void refresh() {
		Intent intent = new Intent(cxt, ZnamkyService.class);
		cxt.startService(intent);
	}
	
	public static void scheduleRefresh(Context cxt, long within) {
		Intent newIntent = new Intent(TimeAlarm.INTENT_ACTION, null, cxt, TimeAlarm.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(cxt, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager am = (AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pendingIntent);
		am.set(AlarmManager.RTC_WAKEUP, findoutNextIntentTime(cxt, within), pendingIntent);
	}
	
	private static long findoutNextIntentTime(Context cxt, long within) {
		boolean dateOk = false;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(c.getTimeInMillis()+within+30000);
		
		while(!dateOk) {
			if (c.get(Calendar.MONTH) == Calendar.JULY || c.get(Calendar.MONTH) == Calendar.AUGUST) {
				c.set(Calendar.MONTH, Calendar.SEPTEMBER);
			} else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				c.add(Calendar.DAY_OF_MONTH, 2);
			} else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				c.add(Calendar.DAY_OF_MONTH, 1);
			} else if (c.get(Calendar.HOUR_OF_DAY) < 7) {
				c.set(Calendar.HOUR_OF_DAY, 7);
				c.set(Calendar.MINUTE, 15);
			} else if (c.get(Calendar.HOUR_OF_DAY) > 17) {
				c.add(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.HOUR_OF_DAY, 7);
				c.set(Calendar.MINUTE, 15);
			} else {
				dateOk = true;
			}
		}
		return c.getTimeInMillis();
	}
	
	private static boolean isWifiConnected(Context cxt) {
		ConnectivityManager manager = (ConnectivityManager) cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
	}
	
}