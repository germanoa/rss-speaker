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
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

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
	
	public List<FeedsFolder> feedsfolder;
	
	private TextToSpeech tts;
    private boolean feedsInitializated;
	//private EditText txtText;
        
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        //txtText = (EditText) findViewById(R.id.txtText);
        //txtText.setText(cookieText);

        //tts = new TextToSpeech(this, this);
        feedsInitializated = false;
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int arg0) {
           if(arg0 == TextToSpeech.SUCCESS) 
               {
        	   	while (!feedsInitializated) {
        	   		System.out.println("waiting");
        	   		try {
        	   		Thread.sleep(1000);
        	   		}
        	   		catch(Exception e) {
        	   			e.printStackTrace();
        	   		}
        	   	}
        	   		//tts.setLanguage(Locale.US);
        	   		speakOut("tts inicializado");
        	   		System.out.println("fase 2");
        	   		speakOut("agora eh a fase 2");
        	   		System.out.println("fase 3");
        	   		speakOut("agora eh a fase 3");
                	Iterator<FeedsFolder> iterator = feedsfolder.iterator();
                	while (iterator.hasNext()) {
                		FeedsFolder f = iterator.next();
                		speakOut(f.name);
                		for (int i=0;i<f.feeds.size();i++) {
                    		System.out.println(f.feeds.get(i).id);
                			speakOut(f.feeds.get(i).id);
                		}
                	}

               }
            }
        });
        
        
        //recebendo dados da activity anterior(Login)
    	String[] cookieArray = new String[4];
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cookieArray = extras.getStringArray("cookieArray");
        }
        
        // buscando feeds      
        BasicClientCookie cookie = new BasicClientCookie(cookieArray[0],cookieArray[1]);
        cookie.setDomain(cookieArray[2]);
        cookie.setPath(cookieArray[3]);        
        String readCloudFeed = readNewsBlurFeed(cookie);
        
        //criando os folders de feeds
        feedsfolder = createFeedsFolder(readCloudFeed);
    	feedsInitializated = true;        	
        	// Lendo feeds.
/*        	Iterator<FeedsFolder> iterator = feedsfolder.iterator();
        	while (iterator.hasNext()) {
        		FeedsFolder f = iterator.next();
        		//speakOut(f.name);
        		for (int i=0;i<f.feeds.size();i++) {
            		System.out.println(f.feeds.get(i).id);
        			//speakOut(f.feeds.get(i).id);
        		}
        	}
*/        	
     };
        
    @Override
    public void onResume() {
        super.onResume();
    	//speakOut("agora eu estou pronto para falar");
    }
     
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
 
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            } else {
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
        
    private void speakOut(String text) {
/*    	while(!ttsInitializated) {
    		try {
        		Thread.sleep(5000);        			
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
*/        //tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    	tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }
    
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

	public List<FeedsFolder> createFeedsFolder(String readCloudFeed) {
    	List<FeedsFolder> feedsfolder = new ArrayList<FeedsFolder>();
		try {
			JSONObject object = new JSONObject(readCloudFeed);
        	Log.i(RadioActivity.class.getName(),object.getString("folders").toString());
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
		}catch(Exception e) {
			e.printStackTrace();
		}
        return feedsfolder;
	}


}
