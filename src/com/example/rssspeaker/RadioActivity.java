package com.example.rssspeaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

public class RadioActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	
	public class Story {
		public Story(String name, String content) {
			this.content = content;
			this.name = name;
		}
		
		public String name;
		public String content;
	}
	
	public class Feed {
		public Feed(String id, String name) {
			this.id = id;
			this.name = name;
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
	protected static final int RESULT_SPEECH = 1;
	private TextToSpeech tts;
    private boolean feedsInitializated,tts_finished,command_received;
	//private EditText txtText;
    private String comandoVoz;
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
			if (utteranceId.equals("conteudo")) {
				tts_finished=true;
			}
	}

	private Button btnExit;
    
	
	@Override
    public void onInit(int arg0) {
		if(arg0 == TextToSpeech.SUCCESS) {
 	    tts.setOnUtteranceCompletedListener(this);
 	   	while (!feedsInitializated) {
 	   		try {
 	   		Thread.sleep(1000);
 	   		}
 	   		catch(Exception e) {
 	   			e.printStackTrace();
 	   		}
 	   	}
 	   	tts_finished=false;
 	   	//tts.setLanguage(Locale.US);
 	   	speakOut("Iniciando leitura de notícias","conteudo");
 	   	}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        
        OnClickListener menuOnClickListener = new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
            	 switch(arg0.getId()){
                 	case R.id.btnExit:
                 		Intent intent = new Intent(Intent.ACTION_MAIN); finish();
                 		break;
            	 }
            }
        };
        
        btnExit = (Button) findViewById(R.id.btnExit);
        // button on click event
        btnExit.setOnClickListener(menuOnClickListener);
        
        
        
        feedsInitializated = false;
        tts = new TextToSpeech(this, this);
        
        //recebendo dados da activity anterior(Login)
    	String[] cookieArray = new String[4];
        Bundle extras = getIntent().getExtras();
        //if (extras != null) {
            cookieArray = extras.getStringArray("cookieArray");
        //}
        
        // buscando feeds      
        BasicClientCookie cookie = new BasicClientCookie(cookieArray[0],cookieArray[1]);
        cookie.setDomain(cookieArray[2]);
        cookie.setPath(cookieArray[3]);        
        String readCloudFeed = readNewsBlurFeed(cookie);
        
        //criando os folders de feeds
        feedsfolder = createFeedsFolder(readCloudFeed);

        //BLOCO PARA TESTE OFFLINE
        /*
		//story1
		//Story story1 = new Story("Cidade alemã pretende distribuir CDs de Linux para prevenir lixo eletrônico pós windows XP","Com o fim do suporte da Microsoft ao Windows XP, o Conselho Municipal de Munique, na Alemanha, teme que milhares de computadores ainda em condições de operação, mas incompatíveis com o Windows 7 ou posteriores, virem lixo eletrônico.");
		Story story1 = new Story("Cidade alemã pós windows XP","Com o fim do suporte da Microsoft ao Windows XP, o Conselho Municipal de Munique, na Alemanha, teme que milhares.");
		//story2
		Story story2 = new Story("Vaga para Desenvolvedor PHP em São Bernardo do Campo","A UNILOGIC, detentora dos sites ultradownloads.com.br e canaltech.com.br, está contratando programadores PHP, com sólidos conhecimento na linguagem, para integrar sua equipe. O ambiente de desenvolvimento conta com máquinas Linux e MacOS.");
		//story3
		Story story3 = new Story("Até que funciona este negócio","Impressionante que mão para fazer funcionar esta noticia. dalhe colorado!");		
		// stories
		List<Story> stories1 = new ArrayList<Story>();
		stories1.add(story1);
		stories1.add(story2);
		stories1.add(story3);
		//feed1
		Feed feed1 = new Feed("9345","br-linux");
		feed1.stories = stories1;
		//lista de feeds
		List<Feed> feeds = new ArrayList<Feed>();		
		feeds.add(feed1);
		//lista de folders
        feedsfolder = new ArrayList<FeedsFolder>();
		String namefolder = "Trabalho";
		feedsfolder.add(new FeedsFolder(namefolder,feeds));
		*/
        
        feedsInitializated = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
            	while(!tts_finished){}
            	playRadio();
            }
        }).start();
        
       
     };
    
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	 super.onActivityResult(requestCode, resultCode, data);
         System.out.println("entrando em onactivityresult");
    	 switch (requestCode) {
         case RESULT_SPEECH: {
             System.out.println("estamos aqui");
             if (resultCode == RESULT_OK && null != data) {  
                 ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                 System.out.println("voz capturada: " + text.get(0));
                 comandoVoz=text.get(0);
             }
             else {
            	 comandoVoz="pr";
            	 finishActivity(RESULT_SPEECH);
            	 //speakOut("ó mestre, preciso que me cliques e mande um novo comando. obrigado","conteudo");
             }
 			 tts_finished=false;
 			 command_received=true;
             break;
         } 
         }
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
 
    private void playRadio() {
    	int folder = 1;
    	//Iterator<FeedsFolder> iterator = feedsfolder.iterator();
        //while (iterator.hasNext()) {
    	while (true) {
        	//FeedsFolder f = iterator.next();
        	FeedsFolder f = feedsfolder.get(folder);
        	speakOut(f.name,"folder");
        	for (int i=0;i<f.feeds.size();i++) {
        		Feed feed = f.feeds.get(i);
        		speakOut(feed.name, "feed");
        		command_received=true;
        		System.out.println(feed.stories.toString());
        		for (int j=0; j<feed.stories.size();j++) {
        			command_received=false;
        			Story story = feed.stories.get(j);
        			speakOut(story.name, "conteudo");
         			tts_finished=false;
             		while (!command_received) {
             			while(tts_finished) {
             				tts_finished=false;
             				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);         
                			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                			try {
                				startActivityForResult(intent, RESULT_SPEECH);
                			} catch (ActivityNotFoundException a) {
                				Toast t = Toast.makeText(getApplicationContext(),"Opps! Your device doesn't support Speech to Text",Toast.LENGTH_SHORT);
                				t.show();
                			}
             			}
             		}
             		if (comandoVoz.contains("de")) { //detalhes
             			speakOut(story.content, "cont");
             		}
             		else if (comandoVoz.contains("pr")) { //proximo
             			//nop
             		}
             		else if (comandoVoz.contains("re")) { //repetir
             			j--;             			
             		}
             		else if (comandoVoz.contains("an")) { //anterior
             			if (j-1>=0) {
             				System.out.println("j>=1");
             				j = j -2;	
             			}
             			else {
             				System.out.println("j<1");
             				j--;
             			}
             		}
              		else if (comandoVoz.contains("ca")) { //casa
              			System.out.println("indo para o folder casa");
              			i=f.feeds.size();
              			j=feed.stories.size();
              			folder=1;
              		}
              		else if (comandoVoz.contains("tra")) { //trabalho
              			System.out.println("indo para o folder trabalho");
              			i=f.feeds.size();
              			j=feed.stories.size();
              			folder=0;
              		}
              		else if (comandoVoz.contains("novo")) { //muda feed
              			System.out.println("pulando feed");
              			j=feed.stories.size();
              		}

        		}
         	}
         }
    }
    
    private void speakOut(String text, String name) {
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, name);
    	tts.speak(text, TextToSpeech.QUEUE_ADD, myHashAlarm);
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
        				List<String> story_title = extractStoryTitle(story);
        				List<String> story_content = extractStoryContent(story);
        				for (int k=0;k<story_title.size();k++) {
        					stories.add(new Story(story_title.get(k), story_content.get(k)));	
        				}
        				Feed feed = new Feed(feed_id,"nome");
        				feed.stories = stories;
                		feeds.add(feed);
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

	public List<String> extractStoryTitle(String story) {
		List<String> title = new ArrayList<String>();
		try {
			System.out.println(story);
			JSONObject object = new JSONObject(story);
			JSONArray stories = new JSONArray(object.getString("stories"));
        	for (int i=0; i< stories.length();i++) {
        		JSONObject j = stories.getJSONObject(i);
        		title.add(j.get("story_title").toString());
        		System.out.println(j.get("story_title").toString());
        	}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return title;
	}
	
	public List<String> extractStoryContent(String story) {
		List<String> content = new ArrayList<String>();
		try {
			System.out.println(story);
			JSONObject object = new JSONObject(story);
			JSONArray stories = new JSONArray(object.getString("stories"));
        	for (int i=0; i< stories.length();i++) {
        		JSONObject j = stories.getJSONObject(i);
        		String content_s = j.get("story_content").toString().replaceAll("\\<.*?>","");
        		content.add(content_s);
        		System.out.println(content_s);
        	}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return content;
	}
	
}