package com.example.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.app_1.R;
import com.example.models.MyObject;
import com.example.utils.MyDBAdapter;

public class DatabaseActivity extends Activity{
	MyDBAdapter myDBAdapter;
	Cursor myObjectCursor;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//mDbHelper = new FeedReaderDbHelper(App_1.getAppContext());
		
		setContentView(R.layout.activity_database);

		// Show the Up button in the action bar.
		setupActionBar();
		
		myDBAdapter = MyDBAdapter.getInstance();
		myDBAdapter.open();
		/*
		myDBAdapter.insertEntry(new MyObject("jacek1"));
		myDBAdapter.insertEntry(new MyObject("jacek2"));
		myDBAdapter.insertEntry(new MyObject("jacek3"));
		*/
		
		EditText editText = (EditText) findViewById(R.id.db_console);
		
		/*myObjectCursor = myDBAdapter.getAllImages();
		myObjectCursor.requery();
		String key_id, path, audio, desc, cat;
		if(myObjectCursor.moveToFirst()){
			do{//KEY_ID, COL_PATH, COL_AUDIO_PATH, COL_DESC, COL_CAT
				key_id =  myObjectCursor.getString(0);
				editText.append("\n"+key_id);
				path =  myObjectCursor.getString(1);
				editText.append("\n"+path);
				audio =  myObjectCursor.getString(2);
				editText.append("\n"+audio);
				desc =  myObjectCursor.getString(3);
				editText.append("\n"+desc);
				cat =  myObjectCursor.getString(4);
				editText.append("\n"+cat);
				
				editText.append("\n");
			}while(myObjectCursor.moveToNext());
			myObjectCursor.close();
			
		}
		*/
		
		//populateMyObjectList();
	}
	
	@Override
	public void onDestroy(){
		// Close the database
		super.onDestroy();
		myDBAdapter.close();
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

	private void populateMyObjectList(){
		//myObjectCursor = myDBAdapter.getAllEntries();
		//startManagingCursor(myObjectCursor);
		
		updateArray();
	}
	
	private void updateArray(){
		//myObjectCursor.requery();
		
		//if(myObjectCursor.moveToFirst()){
		//	do{
		//		String name =  myObjectCursor.getString(MyDBAdapter.NAME_COLUMN);
		//		MyObject myObject = new MyObject(name);
				
		//	}while(myObjectCursor.moveToNext());
			
		}
		
	
	
	/***
 * 	public void putIntoDb(String id, String title, String subtitle){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(FeedEntry.COLUMN_NAME_ENTRY_ID, id);
		values.put(FeedEntry.COLUMN_NAME_TITLE, title);
		values.put(FeedEntry.COLUMN_NAME_SUBTITLES, subtitle);
		
		long newRowId;
		newRowId = db.insert(FeedEntry.TABLE_NAME,
						     FeedEntry.COLUMN_NAME_NULLABLE,
						     values);
	}
	
	public void readFromDb(){
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		
		String[] projection = {
				FeedEntry._ID,
				FeedEntry.COLUMN_NAME_TITLE,
				FeedEntry.COLUMN_NAME_SUBTITLES,
		};
		
		// How you want the result sorted in the resulting Cursor
		String sortOrder = FeedEntry.COLUMN_NAME_TITLE + "DESC";
		
		Cursor c = db.query(
				FeedEntry.TABLE_NAME,	// The table to query
				projection,				// The columns to return
				null,					// The columns for the WHERE clause
				null,					// The values for the WHERE clause
				null,					// don't group the rows
				null,					// don't filter by row groups
				sortOrder				// The sort order
				);
		
		c.moveToFirst();
		c.getString(c.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE));

	}
	
	public void deleteFromDb(){
		// Define 'where' part of query.
		String selection = FeedEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
		// Specify arguments in placeholder order.
		String[] selectionArgs = { String.valueOf(rowId) };
		// Issue SQL statement.
		db.delete(table_name, selection, selectionArgs);
	}
	
 * 
 */

}
