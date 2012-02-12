package org.gpkit.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GPKitActivity extends Activity {
	WebView mWebView;
	boolean loadingFinished = true;
	boolean redirect = false;
	String myResult = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		String url = new String("https://accounts.google.com/o/oauth2/auth?scope=https://www.googleapis.com/auth/plus.me&state=profile&redirect_uri=http://localhost&response_type=code&client_id=463447962780.apps.googleusercontent.com");

		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(url);

		mWebView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
				if (!loadingFinished) {
					redirect = true;
				}

				loadingFinished = false;
				mWebView.loadUrl(urlNewString);
				return true;
			}

			public void onPageFinished(WebView view, String url) {
				if(!redirect) {
					loadingFinished = true;
				}

				if(loadingFinished && !redirect) {
					String[] str = null;
					if (url.startsWith("http://localhost")) {
						str = url.split("code=");
						Log.d("GPKit","" + str[1]);
						httpPostData(str[1]);
					} else {
						Log.d("GPKit", "=====Error===");
					}
				} else {
					redirect = false; 
				}
			}
		});
	}

	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) { 
			mWebView.goBack(); 
			return true; 
		} 
		return super.onKeyDown(keyCode, event);
	}

	private void httpPostData(String code) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("https://accounts.google.com/o/oauth2/token");

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(10);
			nameValuePairs.add(new BasicNameValuePair("code", code));
			nameValuePairs.add(new BasicNameValuePair("client_id", "463447962780.apps.googleusercontent.com"));
			nameValuePairs.add(new BasicNameValuePair("client_secret", "dQa-hV3r-NsTOC7PYZXRTNXN"));
			nameValuePairs.add(new BasicNameValuePair("redirect_uri", "http://localhost"));
			nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(httppost);

			//--------------------------
			//   서버에서 전송받기
			//--------------------------
			InputStreamReader tmp = new InputStreamReader(response.getEntity().getContent(), "UTF-8"); 
			BufferedReader reader = new BufferedReader(tmp);
			StringBuilder builder = new StringBuilder();
			String str = "";
			while ((str = reader.readLine()) != null) {
				builder.append(str + "\n");
			}
			myResult = builder.toString();
			Log.d("GPKit", "result => " + myResult);
			fromJSON(myResult);
		} catch (MalformedURLException e) {} catch (IOException e) {} 
	}

	private void fromJSON(String str) {
		try {
			JSONObject obj = new JSONObject(str);
			String access_token = obj.getString("access_token");
			Log.d("GPKit", "json result " + access_token);
		} catch (Exception e){}
	}
}