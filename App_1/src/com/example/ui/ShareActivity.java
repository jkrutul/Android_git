package com.example.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_1.R;

public class ShareActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		// Get the intent that started this activity
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		// Figure out what to do based on the intent type
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				handleSendImage(intent);
			} else if ("text/plain".equals(type)) {
				handleSendText(intent);
				// Create intent to deliver some kind of result data
				Intent result = new Intent("com.example.RESULT_ACTION",
						Uri.parse("content://result_uri"));
				setResult(Activity.RESULT_OK, result);
				finish();
			} else if (Intent.ACTION_SEND_MULTIPLE.equals(action)
					&& type != null) {
				if (type.startsWith("image/")) {
					handleSendMultipleImages(intent);
				}
			} else {
				;
			}
		}
	}

	void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			ImageView iv = (ImageView) findViewById(R.id.shared_image);
			iv.setImageURI(imageUri);
		}

	}

	void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (sharedText != null) {
			TextView tv = (TextView) findViewById(R.id.shared_message);
			tv.setText(sharedText);
		}
	}

	void handleSendMultipleImages(Intent intent) {
		ArrayList<Uri> imageUris = intent
				.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (imageUris != null) {
			;
		}
	}

}
