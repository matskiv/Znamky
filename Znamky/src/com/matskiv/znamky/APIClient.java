package com.matskiv.znamky;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.mastkiv.znamky.R;

public class APIClient {
	
	public static final String TAG = "API";
	
	public enum STATUS { OK, ERROR };
	public enum METHOD { REGISTERDEVICE, REPORT, COMMANDS };
	public interface ApiCallbackable { public void apiCallback(STATUS status);};

	public static final String DEFSERVERURL		= "https://purkynka-matskiv.rhcloud.com/api/m/";
	
	public static final String REGISTERDEVICE	= "registerdevice";
	public static final String REPORT			= "report";
	public static final String COMMANDS			= "commands";
	
	public static final String CONTENT			= "content";
	public static final String APPVERSION		= "app_version";
	
	public static final String SERVERURL		= "server_url";
	public static final String BACKSERVERURL	= "backserver_url";
	public static final String DEVICEUID		= "device_uid";
	public static final String UDATAREFRESH		= "udata_refresh";
	public static final String LASTCOMMAND		= "last_command";
	public static final String UPDATEINTERVAL	= "update_interval";

	public static final String NAME				= "n";
	public static final String USERNAME			= "un";
	public static final String SCLASS			= "c";
	public static final String UID				= "uid";
	public static final String HW				= "hw";
	public static final String MANUFACTURER		= "man";
	public static final String MODEL			= "mod";
	public static final String API				= "api";
	public static final String DENSITY			= "den";
	public static final String SCREENSIZE		= "scs";
	public static final String NFC				= "nfc";

	public static final String COMMANDTYPE		= "t";
	public static final String COMMANDID		= "cid";
	public static final String COMMANDVALUE		= "v";
	public static final String COMMANDSETPREF	= "sp";
	public static final String COMMANDNOTIFLINK	= "nl";
	public static final String COMMANDTITLE		= "t";
	public static final String COMMANDTEXT		= "txt";
	public static final String COMMANDLINK		= "l";
	
	private Context cxt;
	private SharedPreferences prefs;
	private String serverURL;
	private int deviceUID;
	private DefaultHttpClient httpClient;
	
	public APIClient(Context cxt) {
		super();
		this.cxt = cxt;
		prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
		serverURL = prefs.getString(SERVERURL, DEFSERVERURL);
		deviceUID = prefs.getInt(DEVICEUID, -1);
		httpClient = new DefaultHttpClient();

		SSLSocketFactory sslf = null;
		KeyStore trusted = null;
		if (serverURL.startsWith("https")) {
			try{
				trusted = KeyStore.getInstance("BKS");
				trusted.load(null, "".toCharArray());
				
				sslf = new MySSLSocketFactory(trusted);
			}catch (Exception e){
				e.printStackTrace();
			}
			sslf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme ("https", sslf, 443));
			SingleClientConnManager cm = new SingleClientConnManager(new BasicHttpParams(), schemeRegistry);
			httpClient = new DefaultHttpClient(cm, new BasicHttpParams());
		} else {
			httpClient = new DefaultHttpClient();
		}
	}
	
	public STATUS registerDevice() {
		JSONObject jsonObject = new JSONObject();
		try {
			String[] a = getUData();
			jsonObject.put(NAME, a[0]);
			jsonObject.put(SCLASS, a[1]);
			
			jsonObject.put(USERNAME, prefs.getString(PreferenceActivity.USER_NAME, ""));
			jsonObject.put(HW, getHWSummary());
			
			String test = jsonObject.toString();
			Log.w(TAG, test);
			HttpResponse response = apiPost(REGISTERDEVICE, jsonObject);
			String responseString = EntityUtils.toString(response.getEntity()).trim();
			Log.w(TAG, responseString);
            JSONObject jsonResponse = new JSONObject(responseString);
            deviceUID = jsonResponse.getInt(DEVICEUID);
            
            Editor prefsEditor = prefs.edit();
            prefsEditor.putInt(DEVICEUID, deviceUID);
            prefsEditor.commit();
            
		} catch (Exception e) {
			e.printStackTrace();
			return STATUS.ERROR;
		}
		return STATUS.OK;
	}
	
	public STATUS retrieveComands() {
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			PackageInfo pInfo = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0);
			
			nameValuePairs.add(new BasicNameValuePair(DEVICEUID, deviceUID+""));
			nameValuePairs.add(new BasicNameValuePair(APPVERSION, pInfo.versionCode+""));
			nameValuePairs.add(new BasicNameValuePair(LASTCOMMAND, prefs.getInt(LASTCOMMAND, -1)+""));

			String url = COMMANDS+"?";
			url += URLEncodedUtils.format(nameValuePairs, "utf-8");
			
			HttpResponse response = apiGet(url);
			String responseString = EntityUtils.toString(response.getEntity()).trim();
			Log.w(TAG, "comands response: "+responseString);
            JSONArray jsonResponse = new JSONArray(responseString);
            
            Editor prefsEditor = prefs.edit();
            int maxCommandId = prefs.getInt(LASTCOMMAND, -1);
            for (int i=0; i<jsonResponse.length(); i++) {
				JSONObject command = jsonResponse.getJSONObject(i);
				int cid = command.optInt(COMMANDID, -1);
				if (cid > maxCommandId) {
					maxCommandId = cid;
				}
				if (command.getString(COMMANDTYPE).equals(COMMANDSETPREF)){
					JSONObject newPrefs = command.getJSONObject(COMMANDVALUE);
					@SuppressWarnings("unchecked")
					Iterator<String> iter = newPrefs.keys();
				    while (iter.hasNext()) {
				        String key = iter.next();
				        try {
				        	int newValue = newPrefs.getInt(key);
				        	prefsEditor.putInt(key, newValue);
				        } catch (JSONException e) {
				        	String newValue = newPrefs.getString(key);
				        	prefsEditor.putString(key, newValue);
				        }
				    }
				} else if (command.getString(COMMANDTYPE).equals(COMMANDNOTIFLINK)) {
					JSONArray notifications = command.getJSONArray(COMMANDVALUE);
					NotificationManager manager = (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
					NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt);
					builder.setAutoCancel(true);
					builder.setVibrate(new long[]{200, 200, 200, 200, 200, 200});
					builder.setOnlyAlertOnce(true);
					
					for (int k = 0; k < notifications.length(); k++) {
						JSONObject n = notifications.getJSONObject(k);
					
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(n.getString(COMMANDLINK)));
						PendingIntent mainActPIntent = PendingIntent.getActivity(cxt, 0, intent, 0);
						
						builder.setContentIntent(mainActPIntent);
						builder.setTicker(n.getString(COMMANDTITLE));
						builder.setContentTitle(n.getString(COMMANDTITLE));
						builder.setContentText(n.getString(COMMANDTEXT));
						builder.setSmallIcon(R.drawable.ic_stat_err);
						builder.setLights(0xFF0000FF, 100, 3000);
						
						manager.notify((int) (System.currentTimeMillis()/1000), builder.build());
					}
				}
						
			}
            prefsEditor.putInt(LASTCOMMAND, maxCommandId);
            prefsEditor.commit();
            
		} catch (Exception e) {
			e.printStackTrace();
			return STATUS.ERROR;
		}
		return STATUS.OK;
	}
	
	private HttpResponse apiPost(String command, JSONObject content) throws Exception{
		HttpPost httpPost = new HttpPost(serverURL+command);
		httpPost.addHeader("accept", "application/json");
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair(DEVICEUID, deviceUID+""));
		nameValuePairs.add(new BasicNameValuePair(CONTENT, content.toString()));
		
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		
		HttpResponse response = httpClient.execute(httpPost);
		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 400) {
			throw new HttpException("Response error");
		}
		return response;
	}
	
	private HttpResponse apiGet(String url) throws Exception{
		HttpGet httpGet = new HttpGet(serverURL+url);
		httpGet.addHeader("accept", "application/json");
		
		HttpResponse response = httpClient.execute(httpGet);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 400) {
			throw new HttpException("Response error");
		}
		return response;
	}

	private String[] getUData() throws Exception{
		Sas715MarksDownloader downloader = new Sas715MarksDownloader(cxt);
		String cookie = downloader.login();
		
		String server = BasicMarksDownloader.getServer(cxt);
		
		SSLSocketFactory sslf = null;
		KeyStore trusted = null;
		DefaultHttpClient httpClient;
		HttpResponse response = null;
		InputStream ins = null;
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
		String line = "";
		
		HttpGet httpGet = new HttpGet(server+"karta-zaka.php");
		httpGet.setHeader("Cookie", cookie);
		
		try{
			trusted = KeyStore.getInstance("BKS");
			trusted.load(null, "".toCharArray());
			sslf = new MySSLSocketFactory(trusted);
		}catch (Exception e){
			e.printStackTrace();
			throw new HttpException("SSL Factory error");
		}
		sslf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme ("https", sslf, 443));
		SingleClientConnManager cm = new SingleClientConnManager(httpGet.getParams(), schemeRegistry);
		httpClient = new DefaultHttpClient(cm, httpGet.getParams());
		
		try {
			response = httpClient.execute(httpGet);
			ins = response.getEntity().getContent();
			in = new BufferedReader(new InputStreamReader(ins, "windows-1250"));
			
			while ((line = in.readLine()) != null){
				sb.append(line);
				//Log.w(TAG, line.substring(0, (line.length() > 100 ? 100 : line.length())));
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			throw new HttpException("httpClient.execute error");
		}
		try {
			in.close();
		} catch (IOException e) {/*Who cares ?*/}
		
		Document doc = Jsoup.parse(sb.toString());
		Element title = doc.select("div[class=titulek]").first();
		String[] a = title.text().split(" — ");
		String[] out = a[1].split(", ");
		return out;
	}
	
	private JSONObject getHWSummary() throws JSONException {
		JSONObject out = new JSONObject();
		out.put(MANUFACTURER, Build.MANUFACTURER);
		out.put(MODEL, Build.MODEL);
		out.put(API, Build.VERSION.SDK_INT);
		DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
		out.put(DENSITY, dm.densityDpi);
		out.put(SCREENSIZE, (cxt.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK));   
		out.put(NFC, cxt.getPackageManager().hasSystemFeature("android.hardware.nfc"));
		out.put(UID, Secure.getString(cxt.getContentResolver(), Secure.ANDROID_ID));
		return out;
	}
}