package com.example.app_1;

import com.example.utils.Storage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.app_1.MESSAGE";

	static final String STATE_MESSAGE = "userMessage";
	private String message = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		restoreMessage();
		EditText editText = (EditText) findViewById(R.id.edit_message);
		editText.setText(message);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Restore user state */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		message = savedInstanceState.getString(STATE_MESSAGE);
		EditText editText = (EditText) findViewById(R.id.edit_message);
		editText.setText(message);
	}

	/** Save user sate */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current message
		savedInstanceState.putString(STATE_MESSAGE, message);

		super.onSaveInstanceState(savedInstanceState);
	}

	/** Restore saved message */
	private void restoreMessage() {
		message = Storage.readFromSharedPreferences(
				getString(R.string.saved_message_default),
				getString(R.string.pref1), getString(R.string.saved_message),
				this, Context.MODE_PRIVATE);
	}

	/** Called when user click the Send button */
	public void sendMessage(View view) {
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		EditText editText = (EditText) findViewById(R.id.edit_message);
		message = editText.getText().toString();
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}

	public void finishApp(View view) {
		finish();
	}

	public void articles(View view) {
		Intent intent = new Intent(this, Article_HeadlineActivity.class);
		startActivity(intent);

	}

	public void saveMessageToFile(View view) {
		Intent intent = new Intent(this, StorageActivity.class);
		EditText editText = (EditText) findViewById(R.id.edit_message);
		message = editText.getText().toString();
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}
	
	public void database(View view) {
		Intent intent = new Intent(this, DatabaseActivity.class);
		startActivity(intent);

	}
}
