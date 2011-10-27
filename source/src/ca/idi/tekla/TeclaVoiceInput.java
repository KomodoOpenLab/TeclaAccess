/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla;

import java.util.ArrayList;

import ca.idi.tekla.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

public class TeclaVoiceInput extends Activity {

	private static final int VOICE_MODE_SEARCH = 0;
	private static final int VOICE_MODE_INPUT = 1;
	private static final int REQUEST_CODE = 0x55; //Arbitrary
	private int voice_mode;
	private String language_model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_voice);
		
		init();

	}
	
	private void init() {

		Intent intent = getIntent();
		
		String action = intent.getAction();
		
		if (action != null) { // Need to check for null first to avoid null pointer exception
			if (action.equals(Intent.ACTION_VOICE_COMMAND))
				voice_mode = VOICE_MODE_SEARCH;
			else
				voice_mode = VOICE_MODE_INPUT;
		} else {
			voice_mode = VOICE_MODE_INPUT;
		}

		if (voice_mode == VOICE_MODE_INPUT) {
			if (intent.hasExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL)) {
				language_model = intent.getExtras().getString(RecognizerIntent.EXTRA_LANGUAGE_MODEL);
				if (!(language_model.equals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM) || 
						language_model.equals(RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH))) {
					language_model = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
				}
			} else {
				// Use free from model if none was provided
				language_model = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
			}
		}
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (voice_mode == VOICE_MODE_INPUT) {
			startSpeechRecognizer();
		} else {
			startVoiceActions();
			finish();
		}

	}
	
	private void startVoiceActions() {
		TeclaApp.getInstance().startVoiceActions();
	}

	private void startSpeechRecognizer() {

		if (TeclaApp.getInstance().isVoiceInputSupported()) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language_model);
			try {
				startActivityForResult(intent, REQUEST_CODE);
			} catch (ActivityNotFoundException e) {
				// Extra check... should never be called
				Log.e(TeclaApp.TAG, "Voice input not supported");
			}
		} else {
            TeclaApp.getInstance().showToast(R.string.no_voice_input_available);
            finish();
		}
		
	}

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// Populate the wordsList with the String values the recognition engine thought it heard
			ArrayList<String> matches = data.getStringArrayListExtra(
			        RecognizerIntent.EXTRA_RESULTS);
			
			TeclaApp.getInstance().inputStringAvailable(matches.get(0));
			//TeklaApp.getInstance().showToast(matches.toString());
		}
		finish();
    }

}
