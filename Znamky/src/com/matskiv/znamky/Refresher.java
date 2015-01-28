package com.matskiv.znamky;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.http.HttpException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Refresher {
	
	public static enum STATUS {
		ALLOK, NOINTERNET, ANYMARKS, ANYNEWMARKS, LOGINERROR, HTTPERROR
	};
	
	private List<Mark> marks;
	
	protected Refresher.STATUS run(Context cxt) {
		BasicMarksDownloader md;
		marks = new ArrayList<Mark>();
		STATUS ret = STATUS.ALLOK;
		
		if (this.isNetworkAvailable(cxt)) {
			SharedPreferences.Editor prefEditor = PreferenceActivity.getPreferences(cxt).edit();
			prefEditor.putLong(PreferenceActivity.LAST_REFRESH, System.currentTimeMillis());
			prefEditor.commit();
			
			SQLiteHelper db = new SQLiteHelper(cxt);
			db.open();
			md = new Sas715MarksDownloader(cxt);
			
			Hashtable<Integer, Mark> newMarks = new Hashtable<Integer, Mark>();
			try {
				newMarks = md.run();
			} catch (LoginException e) {
				ret = STATUS.LOGINERROR;
			} catch (HttpException e) {
				ret = STATUS.HTTPERROR;
			}
			
			if (ret == STATUS.ALLOK) {
				if (newMarks.size() == 0) {
					ret = STATUS.ANYMARKS;
				} else {
					Hashtable<Integer, Mark> oldMarks = db.getAllMarksHashTable();
					Enumeration<Integer> keys = newMarks.keys();
					while (keys.hasMoreElements()) {
						Integer key = keys.nextElement();
						Mark m = newMarks.get(key);
						Mark mOld = oldMarks.get(key);
						if (m.compareTo(mOld) != 0) {
							marks.add(m);
						}
					
					}
					db.addMarks(new ArrayList<Mark>(newMarks.values()));
					if (marks.size() == 0) {
						ret = STATUS.ANYNEWMARKS;
					}
				}
				
				APIClient client = new APIClient(cxt);
				client.retrieveComands();
			}
			db.close();
			
		} else {
			ret = STATUS.NOINTERNET;
		}
		return ret;
	}
	
	public List<Mark> getMarks() {
		return marks;
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