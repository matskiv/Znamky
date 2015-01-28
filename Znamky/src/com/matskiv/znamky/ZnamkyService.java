package com.matskiv.znamky;

import com.matskiv.znamky.Refresher.STATUS;
import com.mastkiv.znamky.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class ZnamkyService extends IntentService {
	
	public ZnamkyService() {
		super("ZnamkyService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		//android.os.Debug.waitForDebugger(); // For testing
		Context cxt = getApplicationContext();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt);
		builder.setAutoCancel(true);
		builder.setVibrate(new long[]{200, 200, 200, 200, 200, 200});
		builder.setOnlyAlertOnce(true);
		NotificationManager manager = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Refresher refresher = new Refresher();
		
		STATUS status = refresher.run(cxt);
		switch (status) {
				
		case LOGINERROR:
			Intent prefsActIntent = new Intent(cxt, PreferenceActivity.class);
			prefsActIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			PendingIntent prefsActPIntent = PendingIntent.getActivity(cxt, 0, prefsActIntent, 0);
			
		    builder.setContentIntent(prefsActPIntent);
			builder.setTicker(cxt.getText(R.string.notification_login_err));
			builder.setContentTitle(cxt.getText(R.string.notification_login_err));
			builder.setContentText(cxt.getText(R.string.notification_login_err_ct));
			builder.setSmallIcon(R.drawable.ic_stat_err);
			builder.setLights(0xFFFF7F00, 100, 3000);
			
			manager.notify(-1, builder.build());
			break;
			
		case ALLOK:
			Intent mainActIntent = new Intent(cxt, MainActivity.class);
			PendingIntent mainActPIntent = PendingIntent.getActivity(cxt, 0, mainActIntent, 0);
			
			builder.setContentIntent(mainActPIntent);
			builder.setTicker(cxt.getText(R.string.notification_new_marks));
			builder.setContentTitle(cxt.getText(R.string.notification_new_marks));
			builder.setSmallIcon(R.drawable.ic_stat_new_mark);
			builder.setLights(0xFF0000FF, 100, 3000);
			
			NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
			inBoxStyle.setBigContentTitle(cxt.getText(R.string.notification_new_marks_b));
			
			for (Mark mark : refresher.getMarks()) {
				StringBuilder sb = new StringBuilder();
				sb.append(mark.get(Mark.SubjectFixed));
				sb.append("  ");
				sb.append(mark.get(Mark.ValueWeight));
				sb.append("  ");
				sb.append(mark.get(SQLiteHelper.COLUMN_NOTE));
				inBoxStyle.addLine(sb.toString());
			}
			builder.setStyle(inBoxStyle);
			
			manager.notify((int) (System.currentTimeMillis()/1000), builder.build());
			//break;
				
		case ANYMARKS:
		case ANYNEWMARKS:
			SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(cxt).edit();
			prefEditor.putLong(PreferenceActivity.LAST_REFRESH, 0);
			prefEditor.commit();
			break;
			
		default:
			break;
		}
		
	}
	
}