package com.example.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.app_1.R;
import com.example.utils.Storage;

public class DisplayMessageActivity extends Activity {
	
	private static final String TAG = "DisplayMessageActivity";

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);

		// Show the Up button in the action bar.
		setupActionBar();

		// Get the message from the intent
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

		// Initialize member TextView so we can manipulate it later
		TextView textView = (TextView) findViewById(R.id.text_message);
		textView.setTextSize(20);
		textView.setText(message);
		
		// Save receive message
		
		Storage.saveToSharedPreferences(getString(R.string.pref1), message, getString(R.string.saved_message), this, Context.MODE_PRIVATE);
		Log.v(TAG, "message saved");
	}
	
	/**
	 * - start from stopped state
	 */
	@Override
	protected void onStart(){
		super.onStart();
		TextView textView = (TextView) findViewById(R.id.text_message);
		String m = (String) textView.getText();
		m +="\n-start";
		textView.setText(m);

	}
	
	@Override
	protected void onResume(){
		super.onResume();
		TextView textView = (TextView) findViewById(R.id.text_message);
		String m = (String) textView.getText();
		m +="\n-resume";
		textView.setText(m);

	}
	
	/**
	 * - stop animations or other actions consume CPU
	 * - commit unsaved changes, but only if user expects such changes to be save
	 * - release system resources, such as broadcast recivers, handles to sensor(like GSP)...
	 */
	@Override
	protected void onPause(){
		super.onPause();
		TextView textView = (TextView) findViewById(R.id.text_message);
		String m = (String) textView.getText();
		m +="\n-pause";
		textView.setText(m);

	}

	/**
	 * - perform heavy-load shutdown operations
	 * - should essentially clean up all your activity's resources
	 */
	@Override
	protected void onStop(){
		super.onStop();
		TextView textView = (TextView) findViewById(R.id.text_message);
		String m = (String) textView.getText();
		m +="\n-stop";
		textView.setText(m);

	}
	
	/**
	 * - calls only from stop
	*/
	@Override
	protected void onRestart(){
		super.onRestart();
		TextView textView = (TextView) findViewById(R.id.text_message);
		String m = (String) textView.getText();
		m +="\n-restart";
		textView.setText(m);

	}
	
	/**
	 * - you should be sure that additional threads are destroyed and other
	 * 	 long-running actions like method tracing are also stopped
	 * - users rotate screen
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		TextView textView = (TextView) findViewById(R.id.text_message);
		String m = (String) textView.getText();
		m +="\n-destroy";
		textView.setText(m);
		
	    // Stop method tracing that the activity started during onCreate()
	    android.os.Debug.stopMethodTracing();

	}
	
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* button click */
    public void finishApp(View view){
    	finish();
    }
}
