package com.example.rssspeaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
//import org.apache.commons.httpclient.NameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

public class LoginActivity extends Activity {

	/* Called when the activity is first created. */

	private Button btnLogin;
    private EditText txtUser;
    private EditText txtPass;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Just for testing, allow network access in the main thread
        // NEVER use this is productive code
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        
        
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtUser = (EditText) findViewById(R.id.txtUser);
        txtUser.setText("germanoa");
        txtPass = (EditText) findViewById(R.id.txtPass);
        //txtPass.setText("germanoa");
        
        OnClickListener menuOnClickListener = new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
            	 switch(arg0.getId()){
                 	case R.id.btnLogin:
                 		loginCloudNewsFeeder(txtUser.getText().toString(), txtPass.getText().toString());
                 		break;
            	 }
            }
        };
        
        // button on click event
        btnLogin.setOnClickListener(menuOnClickListener);       
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
        
    private void loginCloudNewsFeeder(String User, String Pass) {
    	List<Cookie> cookies = NewsBlurLogin(User,Pass);
    	if (!cookies.isEmpty()) {
    		String[] cookieArray = new String[4];
            //copy your List of Strings into the Array 
            int i=0;
            for(Cookie c : cookies ){
                cookieArray[i]=c.getName();
                i++;
                cookieArray[i]=c.getValue();
                i++;
                cookieArray[i]=c.getDomain();
                i++;
                cookieArray[i]=c.getPath();
            }
    		Intent myIntent = new Intent(LoginActivity.this, RadioActivity.class);
    		//myIntent.putExtra("key", cookies.get(0).toString()); //Optional parameters
    		myIntent.putExtra("cookieArray", cookieArray);
    		LoginActivity.this.startActivity(myIntent);
    	}
    }
    	
    private List<Cookie> NewsBlurLogin(String User, String Pass) {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.newsblur.com/api/login");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username",User));
            //nameValuePairs.add(new BasicNameValuePair("password", Pass));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
        	System.out.println("sending post");
            HttpResponse response = httpclient.execute(httppost);
        	System.out.println("sended post");
            Log.v("response code", response.getStatusLine().getStatusCode() + ""); 

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        	System.out.println("catch client protocol");
        } catch (IOException e) {
            // TODO Auto-generated catch block
        	System.out.println("catch io except");
        }
        
        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }
        return cookies;
    } 

}
