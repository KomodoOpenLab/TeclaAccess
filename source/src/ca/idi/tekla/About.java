package ca.idi.tekla;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class About extends Activity implements OnClickListener {

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
		
		label1.setVisibility(View.GONE);
		label2.setVisibility(View.GONE);
		label3.setVisibility(View.GONE);
		label4.setVisibility(View.GONE);
		label5.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View view) {
		if (view.equals(title1)) {
			if (label1.getVisibility()==View.GONE){
				label1.setVisibility(View.VISIBLE);
			}else{
				label1.setVisibility(View.GONE);
			}
		} else if (view.equals(title2)) {
			if (label2.getVisibility()==View.GONE){
				label2.setVisibility(View.VISIBLE);
			}else{
				label2.setVisibility(View.GONE);
			}
		} else if (view.equals(title3)) {
			if (label3.getVisibility()==View.GONE){
				label3.setVisibility(View.VISIBLE);
			}else{
				label3.setVisibility(View.GONE);
			}
		} else if (view.equals(title4)) {
			if (label4.getVisibility()==View.GONE){
				label4.setVisibility(View.VISIBLE);
			}else{
				label4.setVisibility(View.GONE);
			}
		} else if (view.equals(title5)) {
			if (label5.getVisibility()==View.GONE){
				label5.setVisibility(View.VISIBLE);
			}else{
				label5.setVisibility(View.GONE);
			}
		}
		// TODO Auto-generated method stub

	}
}
