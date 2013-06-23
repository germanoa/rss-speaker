package com.example.rssspeaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

public class RadioActivity extends Activity implements TextToSpeech.OnInitListener{

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
            }
    
        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

	
	public class Story {
		public Story(String content) {
			this.content = content;
		}
		
		public String name;
		public String content;
	}
	
	public class Feed {
		public Feed(String id) {
			this.id = id;
		}
		public String id;
		public String name;
		public List<Story> stories;
	}
	
	public class FeedsFolder {
		
		public FeedsFolder(String name, List<Feed> feeds) {
			this.name = name;
			this.feeds = feeds;
		}
		
		public String name;
		public List<Feed> feeds;
		public String geolocation; 
	}
	
	
	protected static final int RESULT_SPEECH = 1;
 	
    private TextToSpeech tts;
    private EditText txtText;
        
    private void speakOut(String speak_this) {
        //String text = txtText.getText().toString();
        tts.speak(speak_this, TextToSpeech.QUEUE_FLUSH, null);
    }

    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        tts = new TextToSpeech(this, this);

 
        
        Bundle extras = getIntent().getExtras();
        //if (extras != null) {
            //String cookieArray = extras.getString("cookieArray");
        	String[] cookieArray = new String[4];
            cookieArray = extras.getStringArray("cookieArray");
            //System.out.println(cookieArray.length);
        //}
        
       
        txtText = (EditText) findViewById(R.id.txtText);
        //txtText.setText(cookieText);

        BasicClientCookie cookie = new BasicClientCookie(cookieArray[0],cookieArray[1]);
        cookie.setDomain(cookieArray[2]);
        cookie.setPath(cookieArray[3]);
        
        String readCloudFeed = readNewsBlurFeed(cookie);
        try {
        	String result = "";
        	JSONObject object = new JSONObject(readCloudFeed);
        	Log.i(RadioActivity.class.getName(),object.getString("folders").toString());

        	List<FeedsFolder> feedsfolder = new ArrayList<FeedsFolder>();
        	
        	JSONArray folders = new JSONArray(object.getString("folders"));
        	for (int i=0; i< folders.length();i++) {
        		JSONObject j = folders.getJSONObject(i);
        		JSONArray names = j.names();
        		for (int j1=0; j1<names.length();j1++) {
        			String name = names.getString(j1);
        			List<Feed> feeds = new ArrayList<Feed>();
        			
        			List<String> feeds_id = new ArrayList<String>(Arrays.asList(j.get(name).toString().split(",")));
        			String feed_id,story;
        			Iterator<String> it = feeds_id.iterator();
        			while(it.hasNext()) {
        				List<Story> stories = new ArrayList<Story>();
        				feed_id = it.next();
        				feed_id = feed_id.replace("[","");
        				feed_id = feed_id.replace("]","");
        				story = readNewsBlurStories(feed_id);
        				stories.add(new Story(story));
                		feeds.add(new Feed(feed_id));
        	    	}
        			//Adiciona um folder
        			feedsfolder.add(new FeedsFolder(name,feeds));
        		}
        	}
        	
        	// debugging
        	Iterator<FeedsFolder> iterator = feedsfolder.iterator();
        	while (iterator.hasNext()) {
        		FeedsFolder f = iterator.next();
        		System.out.println(f.name);
        		speakOut(f.name);
        		for (int i=0;i<f.feeds.size();i++) {
        			System.out.println(f.feeds.get(i).id);
        			speakOut(f.feeds.get(i).id);
        		}
        	}
        	
        } catch (Exception e) {
          e.printStackTrace();
        }
        
        
        };
        
        
        public String readNewsBlurFeed(Cookie cookie) {
            StringBuilder builder = new StringBuilder();
            DefaultHttpClient client = new DefaultHttpClient();
            client.getCookieStore().addCookie(cookie);
            HttpGet httpGet = new HttpGet("http://www.newsblur.com/reader/feeds");
            try {
              HttpResponse response = client.execute(httpGet);
              StatusLine statusLine = response.getStatusLine();
              int statusCode = statusLine.getStatusCode();	
              if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                  builder.append(line);
                }
              } else {
                Log.e(RadioActivity.class.toString(), "Failed to download file");
              }
            } catch (ClientProtocolException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }
            //System.out.println(builder.toString());
            return builder.toString();
          }

        public String readNewsBlurStories(String feed_id) {
            StringBuilder builder = new StringBuilder();
            DefaultHttpClient client = new DefaultHttpClient();
            //client.getCookieStore().addCookie(cookie);
            HttpGet httpGet = new HttpGet("http://www.newsblur.com/reader/feed/" + feed_id);
            try {
              HttpResponse response = client.execute(httpGet);
              StatusLine statusLine = response.getStatusLine();
              int statusCode = statusLine.getStatusCode();	
              if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                  builder.append(line);
                }
              } else {
                Log.e(RadioActivity.class.toString(), "Failed to download file");
              }
            } catch (ClientProtocolException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }
            //System.out.println(builder.toString());
            return builder.toString();
          }

        
        
}
