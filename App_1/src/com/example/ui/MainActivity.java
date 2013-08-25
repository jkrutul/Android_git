package com.example.ui;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_1.R;
import com.example.imagedownloader.ImageListActivity;
import com.example.imageloader.ImageLoader;
import com.example.utils.BitmapCalc;
import com.example.utils.MyDBAdapter;
import com.example.utils.Storage;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.app_1.MESSAGE";
	public static final int PIC_CONATC_REQUEST = 1; // The request code
	public static final int TAKE_PIC_REQUEST = 2;
	private ShareActionProvider mShareActionProvider;
	private OnAudioFocusChangeListener afChangeListener;
	static final String STATE_MESSAGE = "userMessage";
	private int width, height;
	private String message = "";
	private Bitmap mImageBitmap;
	private String mCurrentPhotoPath; // œcie¿ka do pliku


	private ImageView mImageView, mImageView2;
	private TextView tw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		restoreMessage();
		EditText editText = (EditText) findViewById(R.id.edit_message);
		editText.setText(message);
		
		
		// Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		tw = (TextView) findViewById(R.id.console);
		tw.append("max memory:"+maxMemory+"kB\n"+maxMemory/1024+"MB");
		
		
		
		
		
		
		/*
		 * AudioManager am = (AudioManager)
		 * this.getSystemService(Context.AUDIO_SERVICE);
		 * 
		 * afChangeListener = new OnAudioFocusChangeListener(){ public void
		 * onAudioFocusChange(int focusChange){
		 * 
		 * } }
		 * 
		 * // Request audio focus for playback int result =
		 * am.requestAudioFocus(afChangeListener, // Use the music stream
		 * AudioManager.STREAM_MUSIC, // Request permanent focus
		 * AudioManager.AUDIOFOCUS_GAIN);
		 * 
		 * if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
		 * am.unregisterMediaButtonEventReceiver(afChangeListener); // Start
		 * playback }
		 * 
		 * // Abandon audio focus when playback complete
		 * am.abandonAudioFocus(afChangedLiester);
		 * 
		 * //Start listening for button presses
		 * //am.registerMediaButtonEventReceiver(RemoteControlReceiver);
		 */
		mImageView = (ImageView) findViewById(R.id.picture_image);
		ImageLoader il = new ImageLoader();
		il.loadBitmap(R.drawable.ic_launcher, mImageView);
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);

		// Inflate menu resource file.
		getMenuInflater().inflate(R.menu.main, menu);
		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.menu_item_share);
		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		return true;
	}
	

	// Call to update the share intent
	private void setShareIntent(Intent shareIntent) {
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(shareIntent);
		}
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
	public void gotoImageActv(View view){
		Intent intent = new Intent(this, ImageListActivity.class);
		startActivity(intent);
	}
	
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

	// ACTIVITIES
	public void startMapActivity(View view) {
		// Build the intent
		Uri location = Uri
				.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

		// Start an activity if it's safe
		if (verifyResolves(mapIntent))
			startActivity(mapIntent);

	}

	public void startTTSActivity(View view) {
		Intent intent = new Intent(this, AndroidTextToSpeechActivity.class);
		startActivity(intent);

	}

	public void shareIntent(View view) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send");
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent,
				getResources().getText(R.string.send_to)));
	}

	public void callActivity(View view) {
		Uri number = Uri.parse("tel:505878884");
		Intent callIntent = new Intent(Intent.ACTION_DIAL, number);

		if (verifyResolves(callIntent))
			startActivity(callIntent);

	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void createCalendarEvent(View view) {
		Intent calendarIntent = new Intent(Intent.ACTION_INSERT,
				Events.CONTENT_URI);
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(2012, 0, 19, 7, 30);
		Calendar endTime = Calendar.getInstance();
		endTime.set(2012, 0, 19, 10, 30);
		calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
				beginTime.getTimeInMillis());
		calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
				endTime.getTimeInMillis());
		calendarIntent.putExtra(Events.TITLE, "Ninja class");
		calendarIntent.putExtra(Events.EVENT_LOCATION, "Secret dojo");

		if (verifyResolves(calendarIntent))
			startActivity(calendarIntent);
	}

	public void gotoImageDetailActivity(View view){
		Intent intent = new Intent(this, ImageDetailActivity.class);
		startActivity(intent);
	}
	
	public void gotoImageGridActivity(View view){
		Intent intent = new Intent(this, com.example.ui.ImageGridActivity.class);
		startActivity(intent);
	}
	
	// ACTIVITIES FOR RESULT
	public void takePicture(View view) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (verifyResolves(takePictureIntent)) {
			File f = Storage.createImageFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			Storage.saveToPreferences(mCurrentPhotoPath, "photoPath", this,
					MODE_PRIVATE);
			Storage.galleryAddPic(this, mCurrentPhotoPath);
			takePictureIntent
					.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(takePictureIntent, TAKE_PIC_REQUEST);
			// Storage.galleryAddPic(this , mCurrentPhotoPath);
		}
	}

	public void pickContact(View view) {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK,
				Uri.parse("content://contacts"));
		pickContactIntent.setType(Phone.CONTENT_TYPE);
		if (verifyResolves(pickContactIntent))
			startActivityForResult(pickContactIntent, PIC_CONATC_REQUEST);
	}
	
	public void recreateDB(View view){
		MyDBAdapter db = MyDBAdapter.getInstance();
		db.recreateDB();
	}
	
	public void gotoTestDB(View view){
		Intent intent = new Intent(this, com.example.ui.DatabaseTestActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PIC_CONATC_REQUEST:
				Uri contactUri = data.getData();
				// We only need the NUMBER column, because there will be only
				// one row in the result
				String[] projection = { Phone.NUMBER };

				// Perform the query on the contact to get the NUMBER column
				// We don't need a selection or sort order (there's only one
				// result for the given URI)
				// CAUTION: The query() method should be called from a separate
				// thread to avoid blocking
				// your app's UI thread. (For simplicity of the sample, this
				// code doesn't do that.)
				// Consider using CursorLoader to perform the query.
				Cursor cursor = getContentResolver().query(contactUri,
						projection, null, null, null);
				cursor.moveToFirst();

				// Retrieve the phone number from the NUMBER column
				int column = cursor.getColumnIndex(Phone.NUMBER);
				String number = cursor.getString(column);
				Toast.makeText(getApplicationContext(), number,
						Toast.LENGTH_LONG).show();
				break;
			case TAKE_PIC_REQUEST:
				Bundle extras = data.getExtras();
				mImageBitmap = (Bitmap) extras.get("data");
				setPic();
				break;

			default:
				break;

			}
		}
	}

	// FUNCTIONS

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		width = mImageView.getWidth();
		height = mImageView.getHeight();
	}

	private boolean verifyResolves(Intent intent) {
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(
				intent, PackageManager.PERMISSION_GRANTED);
		return activities.size() > 0;
	}

	private void setPic() {
		if (mCurrentPhotoPath == null) {
			mCurrentPhotoPath = Storage.readFromPreferences(null, "photoPath",
					this, MODE_PRIVATE);
		}
		mImageView.setImageBitmap(BitmapCalc.decodeSampleBitmapFromFile(
				mCurrentPhotoPath, 100, 100));
	}

}
