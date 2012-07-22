package ca.idi.tekla.ime;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import ca.idi.tekla.R;
import ca.idi.tekla.TeclaApp;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MorseChart {
	
	private Context mContext;
	private TeclaMorse mTeclaMorse;
	
	private int NB_COLUMNS;
	private int index;
	private boolean mUpdated = false;
	
	public LinearLayout layout;
	private LinearLayout ll_save;
	private TableLayout[] mTableLayout;
	
	private TableRow.LayoutParams trParams = new TableRow.LayoutParams(
			TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
	
	private TableLayout.LayoutParams tlParams = new TableLayout.LayoutParams(
			TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
	
	private LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	
	public MorseChart(Context context, TeclaMorse teclaMorse) {
		mContext = context;
		mTeclaMorse = teclaMorse;
		
		updateColumnsNb();
		llParams.setMargins(0, 0, 25, 0);
		
		layout = new LinearLayout(mContext);
		layout.setLayoutParams(llParams);
		layout.setOrientation(LinearLayout.HORIZONTAL);
	}
	
	/**
	 * Initializes the view containers
	 */
	private void setViews() {
		mTableLayout = new TableLayout[NB_COLUMNS];
		for (int i = 0; i < NB_COLUMNS; i++) {
			mTableLayout[i] = new TableLayout(mContext);
			mTableLayout[i].setLayoutParams(tlParams);
			layout.addView(mTableLayout[i], llParams);
			index = 0;
		}
	}
	
	/**
	 * Updates the state of the HUD according to
	 * the current Morse sequence
	 */
	public void update() {
        String s = mTeclaMorse.getCurrentChar();
        if ((s.equals("•") || s.equals("-")) && !mUpdated && 
        		TeclaApp.persistence.getMorseKeyMode() != TeclaIME.SINGLE_KEY_MODE) {
        	layout.removeAllViews();
        	setViews();
        	mUpdated = true;
        	
        	//Populate the HUD according to the 1st typed Morse character
        	LinkedHashMap<String,String> map = mTeclaMorse.getMorseDictionary().getChartStartsWith(s);
        	fillHUD(map);
        }
        else if (s.equals("")) {
        	layout.removeAllViews();
        	setViews();
        	mUpdated = false;
        	
        	//Show hints and command sequences
        	LinkedHashMap<String,String> commands = mTeclaMorse.getMorseDictionary().getCommandsSet();
        	fillHUD(commands);
        	
        	
        }
	}
	
	/**
	 * Hides the HUD display
	 */
	public void hide() {
		ll_save = new LinearLayout(mContext);
		ll_save.setLayoutParams(llParams);
		
		//Retrieve the child views
		TableLayout temp[] = new TableLayout[NB_COLUMNS];
		for (int i = 0; i < NB_COLUMNS; i++)
			temp[i] = (TableLayout) layout.getChildAt(i);
		
		layout.removeAllViews();
		
		//Save the views
		for (int i = 0; i < NB_COLUMNS; i++)
			ll_save.addView(temp[i], llParams);
	}
	
	/**
	 * Restores the HUD display
	 */
	public void restore() {
		if (ll_save != null)
			layout = ll_save;
	}
	
	/**
	 * Populates the HUD with the relevant data
	 * @param chart
	 */
	private void fillHUD(LinkedHashMap<String,String> chart) {
		Iterator<Entry<String,String>> it = chart.entrySet().iterator();
		
    	while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			
    		TableRow tr = new TableRow(mContext);
        	tr.setId(100 + index);
        	tr.setLayoutParams(trParams);
        	tr.setBaselineAligned(true);
        	
        	TextView charTV = new TextView(mContext);
        	charTV.setId(200 + index);
        	
        	if (entry.getValue().equals("\\n"))
        		charTV.setText(entry.getValue());
        	else if (entry.getValue().equals("space")) {
        		Resources res = mContext.getResources();
        		charTV.setBackgroundDrawable(res.getDrawable(R.drawable.sym_keyboard_space_2));
        	}
        	else
        		charTV.setText(entry.getValue().toUpperCase());
        	
        	charTV.setTextSize(16.0f);
        	if (entry.getValue().equals("DEL") || entry.getValue().equals("↵") || 
        		entry.getValue().equals("\\n") || entry.getValue().equals("✓") ||
        		entry.getValue().equals("space") || entry.getValue().equals("⇪") ||
        		entry.getValue().equals("↶"))
        		charTV.setTextColor(0xFFFA8E4B);
        	else
        		charTV.setTextColor(0xFF77A8D4);
        	tr.addView(charTV);
        	
        	TextView morseTV = new TextView(mContext);
        	morseTV.setId(300 + index);
        	morseTV.setText(entry.getKey());
        	morseTV.setTextSize(16.0f);
        	morseTV.setTextColor(Color.WHITE);
        	tr.addView(morseTV);
        	
        	addViewToNextTable(tr, tlParams);
        	index++;
    	}
	}

	/**
	 * Used to equally fill the TableLayouts
	 * @param v
	 * @param tlParams
	 */
	private void addViewToNextTable(View v, TableLayout.LayoutParams tlParams) {
		mTableLayout[index % NB_COLUMNS].addView(v, tlParams);
	}
	
	/**
	 * Used to redraw the HUD when changing device orientation
	 * @param conf
	 */
	public void configChanged(Configuration conf) {
		updateColumnsNb();
		layout.removeAllViews();
    	setViews();
    	mUpdated = false;
	}
	
	/**
	 * Updates the NB_COLUMNS variable
	 */
	private void updateColumnsNb() {
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		
		//Temporal hack, pending layout configuration for different screen sizes
		if(dm.densityDpi == DisplayMetrics.DENSITY_LOW)
			NB_COLUMNS = Math.round(dm.widthPixels / 55);
		else if(dm.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
			NB_COLUMNS = Math.round(dm.widthPixels / 67);
		else
			NB_COLUMNS = Math.round(dm.widthPixels / 95);
	}
	
}
