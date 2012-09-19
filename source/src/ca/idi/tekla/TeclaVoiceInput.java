/*
 * Copyright (C) 2010-2011, Inclusive Design Research Centre
 */

package ca.idi.tekla;

import java.util.ArrayList;

import ca.idi.tekla.R;
import ca.idi.tekla.util.TeclaDesktopClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TeclaVoiceInput extends Activity {

	private static final int VOICE_MODE_SEARCH = 0;
	private static final int VOICE_MODE_INPUT = 1;
	private static final int REQUEST_CODE = 0x55; //Arbitrary
	public static final int REQUEST_DICTATION=2;
	private int voice_mode;
	private String language_model;
	private boolean isDictation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_voice);
		
		init();

	}
	
	private void init() {

		Intent intent = getIntent();
		
		String action = intent.getAction();
		
		if(intent.hasExtra("isDictation"))
			isDictation=true;
		else
			isDictation=false;
		
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
		Log.v("voice","requestCode "+requestCode+" "+resultCode+" "+data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// Populate the wordsList with the String values the recognition engine thought it heard
			
			
			ArrayList<String> matches = data.getStringArrayListExtra(
			        RecognizerIntent.EXTRA_RESULTS);
			if(isDictation){
				if(matches.size()>0)
					createDialog(matches, this);
				
				/*synchronized(TeclaApp.dictation_lock){
					try {
						TeclaApp.dictation_lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
			
			}
			TeclaApp.getInstance().inputStringAvailable(matches.get(0));
			//TeklaApp.getInstance().showToast(matches.toString());
		}
		
		if(!isDictation){
			TeclaApp.mSendToPC=TeclaApp.dict_lock;
			finish();
		
		}
		
		
    }
    
    public String createDialog(final ArrayList<String> list,Context context){
		String dictated=null;
		
		final Dialog dictatedialog=new Dialog(context);
		Log.v("voice","creating Dialog");
		dictatedialog.setContentView(ca.idi.tekla.R.layout.dictationdialog);
		dictatedialog.setTitle("Select Text to send");
		ListView lv=(ListView) dictatedialog.findViewById(R.id.resultlister);
		Button nextwindow=(Button) dictatedialog.findViewById(R.id.button_next_window);
		Button nextfield=(Button) dictatedialog.findViewById(R.id.button_next_field);
		
		
		ArrayAdapter<String> listsadapter=new ArrayAdapter<String>(dictatedialog.getContext(),
				android.R.layout.simple_list_item_1,list);
		lv.setAdapter(listsadapter);
		listsadapter.notifyDataSetChanged();
		
		lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String Dictation=list.get((int)arg3);
				if(TeclaApp.desktop !=null)
				TeclaApp.desktop.send_dictation_data(Dictation);
				dictatedialog.dismiss();
			}
			
		});
		
		nextwindow.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(TeclaApp.desktop !=null)
					TeclaApp.desktop.send("command:"+TeclaDesktopClient.NEXT_WINDOW);
			}
		});
		
		nextfield.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(TeclaApp.desktop !=null)
					TeclaApp.desktop.send("command:"+TeclaDesktopClient.NEXT_FIELD);
			}
		});
		dictatedialog.setOnDismissListener(new OnDismissListener(){

			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				Log.v("voice","Dismissed");
				/*synchronized(TeclaApp.dictation_lock){
				TeclaApp.dictation_lock.notify();
				}*/
				
				TeclaApp.mSendToPC=TeclaApp.dict_lock;
				
				finish();
				
				
			}			
		});
		Log.v("voice","created dialog");
		dictatedialog.show();
		return dictated;
	}
    

}
