package ca.idi.tekla.util;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.idi.tecla.sdk.SwitchEvent;
import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;
import ca.idi.tekla.TeclaPrefs;

public class MapSwitchAction extends Activity {
	
	private static String[] action_array;
	private TextView currentTextView;	
	private ImageView switchImageView;
	private LinearLayout actionDisplayLayout;
	private Button done_button;
	private Button cancel_button;
	private int currentCounter = -1;
	//string resource
	private String no_action_string ;
	//loops looped since the last user
	//input was received
	private int round = 0;
	//the current switch to action mapping
	//each index in the array represents a switch index
	private String[] mappings;
	//the number of times we can loop without 
	//receiving any event from the user
	private static final int MAX_LOOP_COUNT = 2;
	private static String extra_zeros = "00000000";
	private static final boolean DEBUG = false;
	Intent mSwitchIntent;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED)){
				SwitchEvent newSwitchEvent = new SwitchEvent(intent.getExtras());
				handleSwitchEvent(newSwitchEvent, currentCounter, -1, false);
				abortBroadcast();
			}
		}
	};
	
	private Handler mHandler = new Handler();
		
	//the switch index to which this action maps
	private static int isMappedTo(String action, String[] mapping_array){
		if(mapping_array!=null){
			for(int i=0;i<mapping_array.length;i++){
				if(mapping_array[i].equals(action))
					return i;
			}
		}
		return -1;
	}
	
	//the switch index to which this action maps
	private int isMappedTo(String action){
		return isMappedTo(action, mappings);
	}
	
	/*
	 * @param switchEvent the switchEvent to be transformed
	 * @param defaultMapResource the string-array switch_action_map defined
	 * Changes the switchEvent according to the currently defined mapping
	*/
	public static SwitchEvent getMappedSwitchEvent(SwitchEvent switchEvent, String[] defaultMapResource){
		//Log.d("MapSwitchAction","binary of 2 " + Integer.toBinaryString(2));
		//Log.d("MapSwitchAction","integer of binary string 011 " + Integer.parseInt("011",2));
		Log.d("MapSwitchAction : lowest bit last","old SwitchChanges : " + Integer.toBinaryString(switchEvent.getSwitchChanges()) +" old SwichStates : " + Integer.toBinaryString(switchEvent.getSwitchStates()));
		//reversing so that the lowest bit is at the 0th index
		StringBuilder switchChangesBuilder = new StringBuilder(Integer.toBinaryString(switchEvent.getSwitchChanges())).reverse().append(extra_zeros) ;
		StringBuilder switchStatesBuilder = new StringBuilder(Integer.toBinaryString(switchEvent.getSwitchStates())).reverse().append(extra_zeros);
		StringBuilder newSwitchChangesBuilder = new StringBuilder(Integer.toBinaryString(switchEvent.getSwitchChanges())).reverse().append(extra_zeros);
		StringBuilder newSwitchStatesBuilder = new StringBuilder(Integer.toBinaryString(switchEvent.getSwitchStates())).reverse().append(extra_zeros);
		if(defaultMapResource != null){
			String[] action_map = TeclaApp.persistence.getSwitchActionMap();
			for(int oldSwitchIndex=0;oldSwitchIndex<defaultMapResource.length;oldSwitchIndex++){
				int newSwitchIndex = isMappedTo(defaultMapResource[oldSwitchIndex], action_map);
				//Log.d("MapSwitchAction",oldSwitchIndex + " maps to " + newSwitchIndex);
				newSwitchChangesBuilder.setCharAt(newSwitchIndex, switchChangesBuilder.charAt(oldSwitchIndex));
				newSwitchStatesBuilder.setCharAt(newSwitchIndex, switchStatesBuilder.charAt(oldSwitchIndex));
			}
		}
		Log.d("MapSwitchAction : lowest bit last","new SwitchChanges : " + newSwitchChangesBuilder.reverse() +" new SwichStates : " + newSwitchStatesBuilder.reverse());
		//reversing so that the lowest bit is at the highest index
		return new SwitchEvent(Integer.parseInt(newSwitchChangesBuilder.reverse().toString(), 2), Integer.parseInt(newSwitchStatesBuilder.reverse().toString(), 2));
	}
		
	//the array index to which this switchEvent maps to
	private int getSwitchIndex(SwitchEvent switchEvent){
		if(switchEvent.isPressed(SwitchEvent.SWITCH_J1))
			return 0;
		if(switchEvent.isPressed(SwitchEvent.SWITCH_J2))
			return 1;
		if(switchEvent.isPressed(SwitchEvent.SWITCH_J3))
			return 2;
		if(switchEvent.isPressed(SwitchEvent.SWITCH_J4))
			return 3;
		else return -1;
	}
	
	/*
	 * @param newSwitchEvent switch event received by the broad case receiver
	 * @param currCounter the current counter being displayed
	 * @param direction the key event generated for testing purposes
	 * @param test whether testing is being done
	 */	
	protected void handleSwitchEvent(SwitchEvent newSwitchEvent, int currCounter, int direction, boolean test) {
		round = 0;
		//if the current item being displayed is Done button
		if(currCounter == action_array.length){
			TeclaApp.persistence.setSwitchActionMap(mappings);
			TeclaApp.getInstance().showToast(R.string.configure_input_changes_saved);
			finish();
		}
		//if the current item being displayed is Cancel button
		else if(currCounter > action_array.length){
			TeclaApp.getInstance().showToast(R.string.configure_input_changes_discarded);
			finish();
		}
		//if the current item being displayed is an action
		else{
			//exchange the mappings
			int mappedTo = isMappedTo(action_array[currCounter]);
			if(!test){
				mappings[mappedTo] = mappings[getSwitchIndex(newSwitchEvent)];
				mappings[getSwitchIndex(newSwitchEvent)] = action_array[currCounter];
			}
			//for testing purposes
			else{
				Log.d("mapping", "old mappedTo = "+mappedTo + " and value = " + mappings[direction] + " new mapping dir = " + direction + " and value = " + action_array[currCounter]);
				mappings[mappedTo] = mappings[direction];
				mappings[direction] = action_array[currCounter];
			}
			switchImageView.setImageResource(getCorresspondingDrawable(action_array[currCounter]));
		}
	}

	/*Used while debugging on emulator*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(!DEBUG){
			return super.onKeyDown(keyCode, event);
		}
		else{
			mSwitchIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_CHANGES);
			mSwitchIntent.removeExtra(SwitchEvent.EXTRA_SWITCH_STATES);
			if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_CHANGES, (int)Math.pow(2, 0));
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_STATES, 0);
				sendOrderedBroadcast(mSwitchIntent, null);
			}
			else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_CHANGES, (int)Math.pow(2, 1));
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_STATES, 0);
				sendOrderedBroadcast(mSwitchIntent, null);
			}
			else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_CHANGES, (int)Math.pow(2, 2));
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_STATES, 0);
				sendOrderedBroadcast(mSwitchIntent, null);
			}
			else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_CHANGES, (int)Math.pow(2, 3));
				mSwitchIntent.putExtra(SwitchEvent.EXTRA_SWITCH_STATES, 0);
				sendOrderedBroadcast(mSwitchIntent, null);
			}
			else{
				return super.onKeyDown(keyCode, event);
			}
			return false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_map_switch_action);
		action_array = getResources().getStringArray(R.array.switch_actions);
		no_action_string = getResources().getString(R.string.no_switch_action);
		currentTextView = (TextView) findViewById(R.id.current_switch_action);
		switchImageView = (ImageView) findViewById(R.id.mapped_switch_image);
		done_button = (Button) findViewById(R.id.switch_action_mapping_done);
		cancel_button = (Button) findViewById(R.id.switch_action_mapping_cancel);
		actionDisplayLayout = (LinearLayout) findViewById(R.id.action_display_layout);
		getWindow().setLayout((int) (getDisplay().getWidth()*0.95), LayoutParams.WRAP_CONTENT);
		mSwitchIntent = new Intent(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED);
	}
	
	private Display getDisplay(){
		return ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		finish();
	}

	@Override
	public void onResume(){
		mappings =  TeclaApp.persistence.getSwitchActionMap();
		currentCounter = -1;
		round = 0;
		IntentFilter action_switch_event_received_filter = new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED);
		action_switch_event_received_filter.setPriority(2);
		registerReceiver(mReceiver, action_switch_event_received_filter);
		mHandler.postDelayed(mSwitchActionRunnable, 1500);
		super.onResume();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mReceiver);
		mHandler.removeCallbacks(mSwitchActionRunnable);
	}

	//Given an action which button's image should be displayed
	//depend on the current button action mapping
	private int getCorresspondingDrawable(String action){
		switch(isMappedTo(action)){
		case 0:
			return R.drawable.nav_keyboard_up;
		case 1:
			return R.drawable.nav_keyboard_down;
		case 2:
			return R.drawable.nav_keyboard_left;
		case 3:
			return R.drawable.nav_keyboard_right;
		default:
			return -1;
		}
	}
	
	private Runnable mSwitchActionRunnable = new Runnable(){
		public void run(){
			//Checking if no actions have been defined
			if(action_array == null){
				TeclaApp.getInstance().showToast(R.string.no_switch_action);
				finish();
				return;
			}
			//jumping to the next action
			currentCounter++;
			//Wrapping the counter
			if(currentCounter == action_array.length + 2){
				round++;
				if(round > MAX_LOOP_COUNT){
					TeclaApp.getInstance().showToast(R.string.no_switch_input_received);
					TeclaApp.getInstance().showToast(R.string.configure_input_changes_discarded);
					finish();
					return;
				}
				currentCounter = 0;
			}
			//When the current counter signifies an action
			if(currentCounter < action_array.length){
				done_button.setVisibility(View.GONE);
				cancel_button.setVisibility(View.GONE);
				actionDisplayLayout.setVisibility(View.VISIBLE);

				currentTextView.setText(currentCounter == -1? no_action_string : action_array[currentCounter]);
				switchImageView.setBackgroundResource(R.drawable.btn_keyboard_key);
				
				int resId = -1;
				if(currentCounter != -1 && (resId = getCorresspondingDrawable(action_array[currentCounter])) !=-1 ){
					switchImageView.setImageResource(resId);
				}
			}
			//when the current counter signifies Done buttom
			else if(currentCounter == action_array.length){
				actionDisplayLayout.setVisibility(View.GONE);
				cancel_button.setVisibility(View.GONE);
				done_button.setVisibility(View.VISIBLE);
			}
			//when the current counter signifies is Cancel button
			else{
				actionDisplayLayout.setVisibility(View.GONE);
				done_button.setVisibility(View.GONE);
				cancel_button.setVisibility(View.VISIBLE);
			}
			//wait and then display the next action
			mHandler.removeCallbacks(mSwitchActionRunnable);
			mHandler.postDelayed(mSwitchActionRunnable, 1500);
		}
	};

}
