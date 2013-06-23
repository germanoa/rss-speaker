package com.example.rssspeaker;

import java.util.ArrayList;
import java.util.Locale;
 
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.content.ActivityNotFoundException;
import android.widget.Toast;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener{

	/* Called when the activity is first created. */

	protected static final int RESULT_SPEECH = 1;
	 	
    private TextToSpeech tts;
    private Button btnSpeak;
    private ImageButton btnCapture;
    private EditText txtText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View root = this.getWindow().getDecorView();
        root.setBackgroundColor(17170450);
        
        tts = new TextToSpeech(this, this);
    
        btnSpeak = (Button) findViewById(R.id.btnSpeak);
        btnCapture = (ImageButton) findViewById(R.id.btnCapture);    
        txtText = (EditText) findViewById(R.id.txtText);

        
        OnClickListener menuOnClickListener = new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
            	 switch(arg0.getId()){
                 	case R.id.btnSpeak:
                 		speakOut();
                 		break;
                 	case R.id.btnCapture:
                 		 Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
          
                         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
          
                         try {
                             startActivityForResult(intent, RESULT_SPEECH);
                             txtText.setText("");
                         } catch (ActivityNotFoundException a) {
                             Toast t = Toast.makeText(getApplicationContext(),
                                     "Opps! Your device doesn't support Speech to Text",
                                     Toast.LENGTH_SHORT);
                             t.show();
                         }
                     break;
            	 }
            }
        };
        
        // button on click event
        btnSpeak.setOnClickListener(menuOnClickListener);
        btnCapture.setOnClickListener(menuOnClickListener);
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case RESULT_SPEECH: {
            if (resultCode == RESULT_OK && null != data) {
 
                ArrayList<String> text = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
                txtText.setText(text.get(0));
            }
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
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut();
            }
    
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    
    private void speakOut() {
        String text = txtText.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
