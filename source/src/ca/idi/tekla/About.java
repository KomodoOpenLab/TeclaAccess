package ca.idi.tekla;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;

public class About extends Activity implements OnFocusChangeListener
		 {

	TextView title1, label1, title2, label2, title3, label3, title4, label4,
			title5, label5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initialize views of the layout
		setContentView(R.layout.about);
		title1 = (TextView) findViewById(R.id.title1);
		title2 = (TextView) findViewById(R.id.title2);
		title3 = (TextView) findViewById(R.id.title3);
		title4 = (TextView) findViewById(R.id.title4);
		title5 = (TextView) findViewById(R.id.title5);
		label1 = (TextView) findViewById(R.id.label1);
		label2 = (TextView) findViewById(R.id.label2);
		label3 = (TextView) findViewById(R.id.label3);
		label4 = (TextView) findViewById(R.id.label4);
		label5 = (TextView) findViewById(R.id.label5);

		// Allows for textviews to be clickable
		title1.setClickable(true);
		title2.setClickable(true);
		title3.setClickable(true);
		title4.setClickable(true);
		title5.setClickable(true);

		// Allows for focus to be set on the textview
		title1.setFocusable(true);
		title2.setFocusable(true);
		title3.setFocusable(true);
		title4.setFocusable(true);
		title5.setFocusable(true);

		// Allows for focus to be changed if textview is clicked
		title1.setFocusableInTouchMode(true);
		title2.setFocusableInTouchMode(true);
		title3.setFocusableInTouchMode(true);
		title4.setFocusableInTouchMode(true);
		title5.setFocusableInTouchMode(true);

		// Sets the listener for a change of focus in each textview
		title1.setOnFocusChangeListener(this);
		title2.setOnFocusChangeListener(this);
		title3.setOnFocusChangeListener(this);
		title4.setOnFocusChangeListener(this);
		title5.setOnFocusChangeListener(this);

		// Defaults to hide all the descriptions on start of activity
		label1.setVisibility(View.GONE);
		label2.setVisibility(View.GONE);
		label3.setVisibility(View.GONE);
		label4.setVisibility(View.GONE);
		label5.setVisibility(View.GONE);
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		// Shows the corrected view and hides all others when focus is changed
		if (view.equals(title1)) {
			label1.setVisibility(View.VISIBLE);
			label2.setVisibility(View.GONE);
			label3.setVisibility(View.GONE);
			label4.setVisibility(View.GONE);
			label5.setVisibility(View.GONE);
		} else if (view.equals(title2)) {
			label1.setVisibility(View.GONE);
			label2.setVisibility(View.VISIBLE);
			label3.setVisibility(View.GONE);
			label4.setVisibility(View.GONE);
			label5.setVisibility(View.GONE);
		} else if (view.equals(title3)) {
			label1.setVisibility(View.GONE);
			label2.setVisibility(View.GONE);
			label3.setVisibility(View.VISIBLE);
			label4.setVisibility(View.GONE);
			label5.setVisibility(View.GONE);
		} else if (view.equals(title4)) {
			label1.setVisibility(View.GONE);
			label2.setVisibility(View.GONE);
			label3.setVisibility(View.GONE);
			label4.setVisibility(View.VISIBLE);
			label5.setVisibility(View.GONE);
		} else if (view.equals(title5)) {
			label1.setVisibility(View.GONE);
			label2.setVisibility(View.GONE);
			label3.setVisibility(View.GONE);
			label4.setVisibility(View.GONE);
			label5.setVisibility(View.VISIBLE);
		}
	}
}
