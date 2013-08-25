package com.example.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.app_1.App_1;
import com.example.models.MyCategoryObject;
import com.example.models.MyImageObject;
import com.example.models.MyObject;
import com.example.models.MyWordObject;

public class MyDBAdapter {
	private SQLiteDatabase db;		// Variable to hold the database instance
	private myDbHelper dbHelper; 	// Database open/upgrade helper
	
	
	private static final String DATABASE_NAME="myDatabase.db";
	private static MyDBAdapter instance = null;
	private static final String LOG_TAG = "MyDBAdapter";
	
	/* T A B L E S */
	private static final String TABLE1="mainTable";
	
	// The name and the column index of each column in your database
	// The index (key) column name for use in where clauses.
	private static final String KEY_ID="_id";
	
	private static final String TABLE_IMAGE = "image"; 					// KEY_ID, COL_PATH, COL_AUDIO_PATH, COL_DESC, COL_CAT
	private static final String COL_PATH = "path";
	private static final String COL_AUDIO_PATH = "audio";
	private static final String COL_DESC = "description";
	private static final String COL_CAT = "category";
	private static final Map<String, Integer> imgMap;
	static{
		Map<String, Integer> mImg = new HashMap<String, Integer>();
		mImg.put(KEY_ID, 0);
		mImg.put(COL_PATH,1);
		mImg.put(COL_AUDIO_PATH,2);
		mImg.put(COL_DESC, 3);
		mImg.put(COL_CAT, 4);
		imgMap = Collections.unmodifiableMap(mImg);
	}
	
	
	private static final String TABLE_CAT = "category";
	private static final String COL_NAME = "name";
	
    private static final String TABLE_DIC = "dictionary";
    private static final String COL_WORD = "word";
    private static final String COL_DEFINITION = "definition";
    
    private static final String TABLE_HTTP = "httpimages";
    private static final String COL_URL  = "url";
    
	
	private static final int DATABASE_VERSION = 1;
	
	
	private static final String TABLE_CAT_CREATE = "CREATE TABLE "+
	TABLE_CAT+" ("+
			KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
			COL_PATH+ " TEXT, "+
			COL_NAME+ " TEXT NOT NULL, "+
			COL_AUDIO_PATH + " TEXT NO NULL, "+
			COL_DESC+ " TEXT NO NULL"+
	");";	
	
	private static final String TABLE_IMAGES_CREATE = "CREATE TABLE "+
	TABLE_IMAGE+" ("+
			KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COL_PATH+ " TEXT, "+
			COL_AUDIO_PATH + " TEXT, "+
			COL_DESC+ " TEXT, "+
			COL_CAT+ " INTEGER"+
			///" FOREIGN KEY("+COL_CAT+") REFERENECES "+TABLE_CAT+"("+KEY_ID+")"+
	");";
	
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "+
    		TABLE_DIC+ " ("+
    		KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            COL_WORD + " TEXT, " +
            COL_DEFINITION + " TEXT" +
    ");";
 
    private static final String HTTP_IMG_TABLE_CREATE = "CREATE TABLE "+
    		TABLE_HTTP+" ("+
    		KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            COL_URL + " TEXT" +
    ");";
	

	public void recreateDB(){
		open();
		String drop_table = "DROP TABLE IF EXISTS ";
		db.execSQL(drop_table+TABLE_IMAGE);
		//db.execSQL(drop_table+TABLE_DESC);
		db.execSQL(drop_table+TABLE_CAT);
		db.execSQL(drop_table+TABLE_HTTP);
		dbHelper.onCreate(db);
	}
	
	private MyDBAdapter(){
		dbHelper = myDbHelper.getInstance( DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static MyDBAdapter getInstance(){
		if (instance == null){
			instance = new MyDBAdapter();
			return instance;
		}else
			return instance;
	}
	
	public MyDBAdapter open() throws SQLException{
				try{
					db = dbHelper.getWritableDatabase();
				}catch(SQLException ex){
					db = dbHelper.getReadableDatabase();
				}
			return this;
	}
	
	public void close(){
		dbHelper.close();
	}
	
	
	
	/* I M A G E */
	/* image - insert */
	public MyImageObject insertImage(MyImageObject mio){
		ContentValues cv = new ContentValues();
		cv.put(COL_PATH, mio.getImagePath() );
		cv.put(COL_CAT, mio.getCategory_fk());
		cv.put(COL_DESC, mio.getDescription());
		if(db == null)
			open();
		long l = db.insert(TABLE_IMAGE, null, cv);
	    if(l==-1){
	    	return null;
	    }
		Cursor c = db.query(TABLE_IMAGE,  null, KEY_ID+" = "+l, null,null, null,null);
		c.moveToFirst();
		mio = cursorToImage(c);
		c.close();
		return mio;
	}
	
	/* image - remove */
	public boolean deleteImage(MyImageObject mio){
		long row_id = mio.getId();
		return db.delete(TABLE_IMAGE, KEY_ID+" = "+row_id, null)>0;
	}
	
	/* image - update */
	public int updateImage(long _rowIndex, MyImageObject mio){
		String where = KEY_ID + "=" + _rowIndex;
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID,mio.getId());
		cv.put(COL_PATH, mio.getImagePath() );
		cv.put(COL_CAT, mio.getCategory_fk());
		cv.put(COL_DESC, mio.getDescription());
		return db.update(TABLE1, cv, where, null);
	}
	
	/* image - get */
	public MyImageObject getImage(long _rowIndex){
		Cursor c = db.query(TABLE_IMAGE,  null, KEY_ID+" = "+_rowIndex, null,null, null,null);
		return cursorToImage(c);
	}
	
	public List<MyImageObject> getAllImages(){
		MyImageObject mio;
		List<MyImageObject> images = new LinkedList<MyImageObject>();
		Cursor c = db.query(TABLE_IMAGE, null,null, null,null, null,null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			mio = cursorToImage(c);
			images.add(mio);
			c.moveToNext();
		}
		c.close();
		
		return images;
	}
	
	/* image - cursor */
	private MyImageObject cursorToImage(Cursor cursor){
		MyImageObject mio = new MyImageObject();
		Log.d(LOG_TAG, "cursor row count"+cursor.getCount());
		
		mio.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
		mio.setImagePath(cursor.getString(cursor.getColumnIndex(COL_PATH)));
		mio.setAudioPath(cursor.getString(cursor.getColumnIndex(COL_AUDIO_PATH)));
		mio.setDescription(cursor.getString(cursor.getColumnIndex(COL_DESC)));
		mio.setCategory_fk(cursor.getLong(cursor.getColumnIndex(COL_CAT)));
		
		
		/*
		mio.setId(cursor.getLong(imgMap.get(KEY_ID)));
		mio.setImagePath(cursor.getString(imgMap.get(COL_PATH)));
		mio.setAudioPath(cursor.getString(imgMap.get(COL_AUDIO_PATH)));
		mio.setDescription(cursor.getString(imgMap.get(COL_DESC)));
		mio.setCategory_fk(cursor.getLong(imgMap.get(COL_CAT)));
		*/
		return mio;
	}
	
	/* C A T E G O R Y */
	/* -insert */
	/* -remove */
	/* -update */
	
	
	/* C U R S O R S */

	public void insertHttpTable(String[] http_img){
		ContentValues cv = new ContentValues();
		for(int i=0; i<http_img.length; i++){
			cv.put(COL_URL, http_img[i]);
			if(db.insert(TABLE_HTTP, null, cv)==-1){
				Log.w(LOG_TAG, "Value: "+http_img[i] + " not save in DB");
			}
		}

	}
	
	public long insertCategory(MyCategoryObject mco){
		ContentValues cv = new ContentValues();	
		cv.put(COL_CAT, mco.getName());
		cv.put(COL_AUDIO_PATH, mco.getAudiopath());
		cv.put(COL_NAME, mco.getName());
		return db.insert(TABLE_CAT, null, cv);
	}
	

	public long insertWord(MyWordObject mwo){
		ContentValues cv = new ContentValues();
		cv.put(COL_WORD,mwo.getKey_word());
		cv.put(COL_DEFINITION, mwo.getKey_definition());
		return db.insert(TABLE_DIC, null,cv);
		
	}
	
	
	//----
	private static class myDbHelper extends SQLiteOpenHelper{
		
		private static myDbHelper instance = null;
		
		private myDbHelper(String name, CursorFactory factory, int version){
			super(App_1.getAppContext(),name,factory,version);
		}
		
		public static myDbHelper getInstance(String name, CursorFactory factory, int version){
			if(instance==null){
				instance = new myDbHelper(name, factory,version);
				return instance;
			}
			else return instance;
		}
		
		// Called when no database exists in
		// disk and the helper class needs
		// to create a new one.
		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(TABLE_IMAGES_CREATE);
			_db.execSQL(TABLE_CAT_CREATE);
			//_db.execSQL(TABLE1_CREATE);
			//_db.execSQL(DICTIONARY_TABLE_CREATE);
			_db.execSQL(HTTP_IMG_TABLE_CREATE);
			
		}

		// Called when there is a database version mismatch meaning that
		// the version of the database on disk needs to be upgraded to
		// the current version.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String drop_table = "DROP TABLE IF EXISTS ";
			// Log the version upgrade/
			Log.w("TaskDBAdapter", "Upgrading from version "+
					oldVersion + " to "+
					newVersion + ", witch will destroy all old data");
			
			// Upgrade the existing database to conform to the new version.
			// Multiple previous version can be handled by comparing
			// oldVersion and newVersion values.
			db.execSQL(drop_table+TABLE_IMAGE);
			//db.execSQL(drop_table+TABLE_DESC);
			db.execSQL(drop_table+TABLE_CAT);
			db.execSQL(drop_table+TABLE_HTTP);
			onCreate(db);
			
		}
	}
}
