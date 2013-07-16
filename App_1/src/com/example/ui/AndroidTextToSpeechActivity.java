package com.example.ui;

import java.util.Arrays;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.app_1.R;

public class AndroidTextToSpeechActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private TextToSpeech tts;
	private Button btnSpeak;
	private EditText txtText;
	public static final int MY_DATA_CHECK_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tts);

		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		// tts = new TextToSpeech(this, this);
		btnSpeak = (Button) findViewById(R.id.bntSpeak);
		txtText = (EditText) findViewById(R.id.txtText);

	}

	@Override
	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		int result;
		if (status == TextToSpeech.SUCCESS) {
	        Locale[] AvalLoc = Locale.getAvailableLocales();

	        Log.i("TTS","Available locales " + Arrays.toString(AvalLoc));
	        
			Locale pol_loc = new Locale("pl", "PL");
			if(TextToSpeech.LANG_AVAILABLE ==tts.isLanguageAvailable(pol_loc)){
				result = tts.setLanguage(pol_loc);
				}
			else{
				result=tts.setLanguage(Locale.ITALIAN);
			}
				
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "LANG_NOT_SUPPORTED");
			} else {
				btnSpeak.setEnabled(true);
				speakOut();
			}

		} else {
			Log.e("TTS", "Initialization Failed");
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, crate the TTS instance
				tts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	public void speakOut(View view) {
		String text = txtText.getText().toString();
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	public void speakOut() {
		String text = txtText.getText().toString();
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
}
