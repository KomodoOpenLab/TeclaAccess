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

public class MapSwitchAction extends Activity {
	
	private int currentCounter = -1;
	private Handler mHandler;
	/*Resources*/
	private static String[] action_array;
	private TextView currentTextView;	
	private ImageView switchImageView;
	private LinearLayout actionDisplayLayout;
	private Button done_button;
	private Button cancel_button;
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
	/*for debugging purposes*/
	private static final boolean DEBUG = false;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED)){
				SwitchEvent newSwitchEvent = new SwitchEvent(intent.getExtras());
				handleSwitchEvent(newSwitchEvent, currentCounter);
				abortBroadcast();
			}
		}
	};
	
		
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
	 * Changes the switchEvent according to the currently defined mapping
	*/
	public static SwitchEvent getMappedSwitchEvent(SwitchEvent switchEvent){
		//Log.d("MapSwitchAction","binary of 2 " + Integer.toBinaryString(2));
		//Log.d("MapSwitchAction","integer of binary string 011 " + Integer.parseInt("011",2));
		if(DEBUG) Log.d("MapSwitchAction : lowest bit last","old SwitchChanges : " + Integer.toBinaryString(switchEvent.getSwitchChanges()) +" old SwichStates : " + Integer.toBinaryString(switchEvent.getSwitchStates()));
		String[] defaultMapResource = TeclaApp.getInstance().getResources().getStringArray(R.array.switch_actions);
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
				newSwitchChangesBuilder.setCharAt(oldSwitchIndex, switchChangesBuilder.charAt(newSwitchIndex));
				newSwitchStatesBuilder.setCharAt(oldSwitchIndex, switchStatesBuilder.charAt(newSwitchIndex));
			}
		}
		if(DEBUG) Log.d("MapSwitchAction : lowest bit last","new SwitchChanges : " + newSwitchChangesBuilder.reverse() +" new SwichStates : " + newSwitchStatesBuilder.reverse());
		//TeclaApp.getInstance().showToast("new SwitchChanges : " + newSwitchChangesBuilder.reverse() +" new SwichStates : " + newSwitchStatesBuilder.reverse());
		//reversing so that the lowest bit is at the highest index
		//TeclaApp.getInstance().showToast("switch states : " + TeclaApp.getInstance().byte2Hex(switchEvent.getSwitchStates()) + "," + TeclaApp.getInstance().byte2Hex(Integer.parseInt(newSwitchStatesBuilder.reverse().toString(), 2)));
		//TeclaApp.getInstance().showToast("switch changes : " + TeclaApp.getInstance().byte2Hex(switchEvent.getSwitchChanges()) + "," + TeclaApp.getInstance().byte2Hex(Integer.parseInt(newSwitchChangesBuilder.reverse().toString(), 2)));
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
		if(switchEvent.isPressed(SwitchEvent.SWITCH_E1))
			return 4;
		if(switchEvent.isPressed(SwitchEvent.SWITCH_E2))
			return 5;
		else return -1;
	}
	
	/*
	 * @param newSwitchEvent switch event received by the broad case receiver
	 * @param currCounter the current counter being displayed
	 * @param direction the key event generated for testing purposes
	 * @param test whether testing is being done
	 */	
	protected void handleSwitchEvent(SwitchEvent newSwitchEvent, int currCounter) {
		round = 0;
		int switch_index = getSwitchIndex(newSwitchEvent);
		//if the current item being displayed is Done button
		if(currCounter == action_array.length && switch_index != -1){//to ensure that the switch has been released
			TeclaApp.persistence.setSwitchActionMap(mappings);
			TeclaApp.getInstance().showToast(R.string.configure_input_changes_saved);
			finish();
		}
		//if the current item being displayed is Cancel button
		else if(currCounter > action_array.length && switch_index != -1){//to ensure that the switch has been released
			TeclaApp.getInstance().showToast(R.string.configure_input_changes_discarded);
			finish();
		}
		//if the current item being displayed is an action
		else if(switch_index != -1){
			//exchange the mappings
			int mappedTo = isMappedTo(action_array[currCounter]);
			if(mappedTo != -1){
				mappings[mappedTo] = mappings[switch_index];
				mappings[switch_index] = action_array[currCounter];
				switchImageView.setImageResource(getCorresspondingDrawable(action_array[currCounter]));
			}
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
		mHandler = new Handler();
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
		super.onResume();
		mappings =  TeclaApp.persistence.getSwitchActionMap();
		currentCounter = -1;
		round = 0;
		IntentFilter action_switch_event_received_filter = new IntentFilter(SwitchEvent.ACTION_SWITCH_EVENT_RECEIVED);
		action_switch_event_received_filter.setPriority(2);
		registerReceiver(mReceiver, action_switch_event_received_filter);
		mHandler.postDelayed(mSwitchActionRunnable, 0);
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
			//Checking if no action resource has been defined
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
