package ca.idi.tekla;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;

public class About extends Activity implements OnFocusChangeListener,
		OnClickListener {

	TextView title1, label1, title2, label2, title3, label3, title4, label4,
			title5, label5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		title1.setClickable(true);
		title2.setClickable(true);
		title3.setClickable(true);
		title4.setClickable(true);
		title5.setClickable(true);

		title1.setOnClickListener(this);
		title2.setOnClickListener(this);
		title3.setOnClickListener(this);
		title4.setOnClickListener(this);
		title5.setOnClickListener(this);

		title1.setFocusable(true);
		title2.setFocusable(true);
		title3.setFocusable(true);
		title4.setFocusable(true);
		title5.setFocusable(true);

		title1.setFocusableInTouchMode(true);
		title2.setFocusableInTouchMode(true);
		title3.setFocusableInTouchMode(true);
		title4.setFocusableInTouchMode(true);
		title5.setFocusableInTouchMode(true);

		title1.setOnFocusChangeListener(this);
		title2.setOnFocusChangeListener(this);
		title3.setOnFocusChangeListener(this);
		title4.setOnFocusChangeListener(this);
		title5.setOnFocusChangeListener(this);

		label1.setVisibility(View.GONE);
		label2.setVisibility(View.GONE);
		label3.setVisibility(View.GONE);
		label4.setVisibility(View.GONE);
		label5.setVisibility(View.GONE);
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
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

	@Override
	public void onClick(View view) {
		if (view.equals(title1)) {
			title1.requestFocus();
		} else if (view.equals(title2)) {
			title2.requestFocus();
		} else if (view.equals(title3)) {
			title3.requestFocus();
		} else if (view.equals(title4)) {
			title4.requestFocus();
		} else if (view.equals(title5)) {
			title5.requestFocus();
		}

	}

}
