package com.matskiv.znamky;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Sas715MarksDownloader extends BasicMarksDownloader {
	
	//private static final String TAG = "Sas715_Log"; //For Log
	private Document doc;
	
	Sas715MarksDownloader(Context cxt) {
		super(cxt);		
	}
	
	@Override
	protected Hashtable<Integer, Mark> run () throws LoginException, HttpException {
		Hashtable<Integer, Mark> marks = new Hashtable<Integer, Mark>();
		//long time = System.currentTimeMillis();
		
		cookie = login();

		//Log.w(TAG, "login(): "+(System.currentTimeMillis()-time));
		//time = System.currentTimeMillis();
		
		String page = downloadMarks();
		
		//Log.w(TAG, "downloadMarks(): "+(System.currentTimeMillis()-time));
		//time = System.currentTimeMillis();
		
		if(checkLogin(page)) {
		//	Log.w(TAG, "checkLogin(): "+(System.currentTimeMillis()-time));
		//	time = System.currentTimeMillis();
			
			marks = extract(page);
			
		//	Log.w(TAG, "extract(): "+(System.currentTimeMillis()-time));
		} else {
			throw new LoginException();
		}

		httpClient.getConnectionManager().shutdown();
		return marks;
	}
	
	public String login() throws HttpException {
		SSLSocketFactory sslf = null;
		KeyStore trusted = null;
		
		HttpPost httpPost = new HttpPost(server+"prihlasit.php");
		
		if (server.startsWith("https")) {
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
			SingleClientConnManager cm = new SingleClientConnManager(httpPost.getParams(), schemeRegistry);
			httpClient = new DefaultHttpClient(cm, httpPost.getParams());
		} else {
			httpClient = new DefaultHttpClient();
		}
		
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("login-isas-username", userName));
		nameValuePairs.add(new BasicNameValuePair("login-isas-password", password));
		nameValuePairs.add(new BasicNameValuePair("login-isas-send", "isas-send"));
		
		try{
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		}catch (UnsupportedEncodingException e){
			e.printStackTrace();
			throw new HttpException("UnsupportedEncodingException");
		}
		
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch(IOException e) {
			e.printStackTrace();
			throw new HttpException("httpClient.execute error");
		}
		
		if (response.getStatusLine().getStatusCode() < 400) {
			if (response.getFirstHeader("Set-Cookie") == null) {
				return "";
			}
			return response.getFirstHeader("Set-Cookie").getValue();
		} else {
			throw new HttpException("Response error");
		}
	}
	
	protected Boolean checkLogin(String html) {
		doc = Jsoup.parse(html);
		Element obsah = doc.select("div[id=isas-obsah]").first();
		
		if (!obsah.text().startsWith("Nemáte oprávnění")) {
			return true;
		} else {
			return false;
		}
	}
	
	protected Hashtable<Integer, Mark> extract(String html) {	
		Hashtable<Integer, Mark> output = new Hashtable<Integer, Mark>();
		String[] items = new String[7];
		String[] a;
		
		Element table = doc.select("table[class=isas-tabulka]").first();
		
		Iterator<Element> rows = table.select("tr").iterator();
		rows.next(); // skip first, its table header
		int order = 1;
		
		while(rows.hasNext()) {
			Element row = rows.next();
			Iterator<Element> cells = row.select("td").iterator();
			Element cell = cells.next();
			float floValue = 0f;
			int weight = 1;
			
			//Geting id
				String link = cell.select("a").first().attr("href");
				a = link.split("zaznam=");
				int id = Integer.parseInt(a[1]);
			
			//Geting add_time
				// starting from 1 because of earlier fail 
				String date = cell.select("a").first().text();
				items[1] = "";
				int count = 0;
				for (int i=0; i<date.length(); i++){
					if (date.charAt(i)=='.') {
						count++;
						if (count == 2) break;
					}
					items[1] += date.charAt(i);
				}
			
			//Getting subject
				cell = cells.next();
				items[2] = cell.text();
				
			//Getting mark
				cell = cells.next();
				items[3] = cell.text();
				
			//Getting flo value
				cell = cells.next();
				floValue = Float.parseFloat(cell.text());
				items[4] = cell.text();
				
			//Getting type
				cell = cells.next(); // skip
			
			//Getting flo value
				cell = cells.next();
				weight = Integer.parseInt(cell.text());
			
			//Getting note
				cell = cells.next();
				items[5] = cell.text();		
			
			//Getting teacher
				cell = cells.next();
				items[6] = cell.text();	
				
				
				float weighted = floValue * weight;
				Mark m = new Mark(id, items[3], weight, weighted, items[2], items[1], items[4], items[5], items[6], order);
				order++;
				
			output.put(id, m);
		}
		return output;
	}
	
	protected String downloadMarks() throws HttpException {
		HttpResponse response = null;
		InputStream ins = null;
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
		String line = "";
		
		HttpGet getPage = new HttpGet(server+"prubezna-klasifikace.php?pololeti="+this.which_half+"&zobraz=datum");
		getPage.setHeader("Cookie", this.cookie);
		
		try {
			response = this.httpClient.execute(getPage);
			ins = response.getEntity().getContent();
			in = new BufferedReader(new InputStreamReader(ins, "windows-1250"));
			
			while ((line = in.readLine()) != null){
				sb.append(line);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			throw new HttpException("httpClient.execute error");
		}
		
		try {
			in.close();
		} catch (IOException e) {
			// Who cares ?
		}
		return sb.toString();
	}
	
	@Override
	protected int getWeight(String examType) {
		for (int i = examType.length()-1; i>=0; i--) {
			if (Character.isDigit(examType.charAt(i))) {
				return Integer.parseInt(examType.substring(i, i+1));
			}
		}
		return 1;
	}
	
}