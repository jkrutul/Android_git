package com.example.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
	
    public FeedReaderContract() {}
    
    public static abstract class FeedEntry implements BaseColumns{
    	public static final String TABLE_NAME = "entry";
    	public static final String COLUMN_NAME_ENTRY_ID = "entryid";
    	public static final String COLUMN_NAME_TITLE = "title";
    	public static final String COLUMN_NAME_SUBTITLES = "subtitle";
		public static final String COLUMN_NAME_NULLABLE = null;

    	private static final String TEXT_TYPE= " TEXT";
    	private static final String COMMA_SEP = ",";
    	
    	private static final String SQL_CREATE_ENTRIES=
    			"CREATE TABLE" +FeedEntry.TABLE_NAME+" ("+
    	FeedEntry._ID					+" INTEGER PRIMARY KEY,"+
    	FeedEntry.COLUMN_NAME_ENTRY_ID 	+ TEXT_TYPE + COMMA_SEP	+
    	FeedEntry.COLUMN_NAME_TITLE 	+ TEXT_TYPE + COMMA_SEP	+
    	FeedEntry.COLUMN_NAME_SUBTITLES + TEXT_TYPE + COMMA_SEP	+
    	" )";
    	
    	private static final String SQL_DELETE_ENTRIES=
    		"DROP TABLE IF EXISTS "+FeedEntry.TABLE_NAME;

    	
    	public class FeedReaderDbHelper extends SQLiteOpenHelper {
    		public static final int DATABASE_VERSION = 1;
    		public static final String DATABASE_NAME = "FeedReader.db";
    		
    		public FeedReaderDbHelper(Context context){
    			super(context, DATABASE_NAME, null, DATABASE_VERSION);
    		}

    		@Override
    		public void onCreate(SQLiteDatabase db) {
    			db.execSQL(SQL_CREATE_ENTRIES);
    			
    		}

    		@Override
    		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    			// This database is only a cache for online data, so it's upgrade policy is 
    			// to simply to discard the data and start over
    			db.execSQL(SQL_DELETE_ENTRIES);
    			onCreate(db);
    		}
    		
    		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    			onUpgrade(db, oldVersion, newVersion);
    		}

    	}
    }
}
