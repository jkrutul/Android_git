package com.example.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.app_1.R;
import com.example.utils.Storage;

public class StorageActivity extends Activity {

	//private static final String TAG = "StorageActivity";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_storage);

		// Show the Up button in the action bar.
		setupActionBar();
		
		// Get the message from the intent
		Intent intent = getIntent();
		String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

		// Initialize member EditView so we can manipulate it later
		EditText editText = (EditText) findViewById(R.id.file_content);
		editText.setText(message);

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
		// getMenuInflater().inflate(R.menu.display_message, menu);
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
	public void saveToFile(View view) {
		EditText editText = (EditText) findViewById(R.id.file_name);
		EditText editText2 = (EditText) findViewById(R.id.file_content);
		TextView textView = (TextView) findViewById(R.id.storage_detail);
		
		String filename = editText.getText().toString();
		String filecontent = editText2.getText().toString();
		
		if(Storage.writeToExternalFile(filename, filecontent.getBytes(), Environment.DIRECTORY_DOWNLOADS)){
			textView.setText("save sucessfull");
		}
	}
	
	public void openFile(View view) {
		EditText editText = (EditText) findViewById(R.id.file_name);
		TextView textView = (TextView) findViewById(R.id.storage_detail);
		String file_content;
		
		
		String filename = editText.getText().toString();
		file_content= Storage.readFromExternalFile(filename, Environment.DIRECTORY_DOWNLOADS);
		if(file_content!= null)
			textView.setText(file_content);
		else
			textView.setText("FILE NOT FOUND");
			
	}
	
	public void findFile(View view){
		EditText editText = (EditText) findViewById(R.id.file_name);
		TextView textView = (TextView) findViewById(R.id.storage_detail);
		String filename = editText.getText().toString();
		String file_content;
		
		file_content= Storage.readFromExternalFile(filename, Environment.DIRECTORY_DOWNLOADS);
		if(file_content!= null)
			textView.setText("FILE EXIST");
		else
			textView.setText("FILE NOT FOUND");
		
	}
	
	public void deleteFile(View view){
		EditText editText = (EditText) findViewById(R.id.file_name);
		TextView textView = (TextView) findViewById(R.id.storage_detail);
		String filename = editText.getText().toString();
		
		String file_content= Storage.readFromExternalFile(filename, Environment.DIRECTORY_DOWNLOADS);
		if(file_content== null)
			textView.setText("FILE NOT FOUND");
		
		if(Storage.deleteExtFile(filename, Environment.DIRECTORY_DOWNLOADS))
			textView.setText("\""+filename+"\" was deleted");
		else
			textView.append("\""+filename+"\" wasn't deleted");
	}
}
