package com.loyid.grammarbook;

import java.util.Locale;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

public class GrammarBookApplication extends Application implements OnInitListener {
	private TextToSpeech mTTS;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mTTS = new TextToSpeech(this, this);
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		boolean isInit = status == TextToSpeech.SUCCESS;
		int msg = isInit ? R.string.msg_init_tts_success : R.string.msg_init_tts_fail;
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	public void playTTS(Locale locale, String text) {
		mTTS.setLanguage(locale);
		mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onTerminate() {
		if (mTTS != null) {
			mTTS.shutdown();
		}
		
		super.onTerminate();
	}
	
}
