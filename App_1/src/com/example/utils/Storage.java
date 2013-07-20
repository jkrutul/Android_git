package com.example.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.example.app_1.App_1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class Storage {
	private static final String LOG_TAG = "Storage";
    public static final int IO_BUFFER_SIZE = 8 * 1024;

	/* INTERNAL ------------------------------------------------------------------------------------------------------*/
	/**
	 * Write to the internal file byte array
	 * 
	 * @param bytes
	 *            byte array to write in file
	 * @param filename
	 *            file name
	 * @param actv
	 *            activity
	 * @return true if bytes saved, otherwise false
	 */
	public static boolean writeToInternalFile(byte[] bytes, String filename,
			Activity actv) {
		FileOutputStream outputstream;
		try {
			outputstream = actv.openFileOutput(filename, Context.MODE_PRIVATE);
			outputstream.write(bytes);
			outputstream.close();
		} catch (Exception e) {
			Log.w("InternalStorage", "Error writing", e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Reads from the internal file byte array
	 * 
	 * @param filename
	 *            file name
	 * @param actv
	 *            activity
	 * @return byte array, else null
	 */
	public static byte[] readFromInternalFile(String filename, Activity actv) {
		byte[] bytes = new byte[100];
		byte b = -1;
		int i = 0;

		FileInputStream fi;
		try {
			fi = actv.openFileInput(filename);

			do {
				b = (byte) fi.read();
				bytes[i] = b;
				i++;
			} while (b != -1);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return bytes;
	}

	/**
	 * Delete internal file specified by filename and context
	 * 
	 * @param filename
	 *            file name
	 * @param c
	 *            context
	 */
	public static void deleteInternalFile(String filename) {
		App_1.getAppContext().deleteFile(filename);
	}

	/* EXTERNAL----------------------------------------------------------------------------------------------------- */

	private static boolean isExternalStorageWirtable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private static boolean isExternalSotrageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)	|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}
	
	private static File getExternalStorageDir(String filename) {
		// Get the directory for the app's private directory
		File file = new File(App_1.getAppContext().getExternalFilesDir(null), filename);
		if (!file.mkdirs()) {
			Log.e("TAG", "Directory not created");
		}
		return file;
	}

	private static File getExternalPublicStorageDir(String filename,
			String state) {
		File sdpath = Environment.getExternalStoragePublicDirectory(state);
		File file = new File(sdpath + File.separator + filename);
		if (!file.getParentFile().mkdirs()) {
			Log.e("TAG", "Directory not created");
		}
		return file;
	}

	public static File createImageFile(){
		
		String JPEG_FILE_PREFIX = "img";
		String JPEG_FILE_SUFIX= ".jpg";
		// create a image file name
		String timeStamp = new SimpleDateFormat("yyyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp+"_";
		try {
			File image = File.createTempFile(imageFileName, JPEG_FILE_SUFIX, getAlbumDir());
			//mCurrentPhotoPath = image.getAbsolutePath();
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static File createImageFile(String imageName){
		
		File image = new File(getAlbumDir(), imageName);
		return image;
	}
	
	public static void galleryAddPic(Activity a, String path){
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(path);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		a.sendBroadcast(mediaScanIntent);
	}
	
	public static File getAlbumDir(){
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"");
	}
	
	public static File getAppRootDir(){
    	Context c=App_1.getAppContext();
    	if(isExternalSotrageReadable()|| hasExternalCacheDir()){
    		    	return c.getExternalFilesDir(null);
    	}
    	else{
            final String cacheDir = "/Android/data/" + c.getPackageName() + "/files/";
            return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    	}
    }
	
	public static boolean deleteExtFile(String filename) {
		if (isExternalSotrageReadable()) {
			File file = getExternalStorageDir(filename);
			return file.delete();
		}
		return false;
	}
	
	public static boolean deleteExtFile(String filename, String state) {
		if (isExternalSotrageReadable()) {
			File file= getExternalPublicStorageDir(filename, state);
				return file.delete();
			}
		return false;
	}
	
	public static boolean writeToExternalFile(String filename, byte[] data) {
		if (isExternalStorageWirtable()) {
			File file = getExternalStorageDir(filename);
			try {
				OutputStream os = new FileOutputStream(file);
				os.write(data);
				os.close();
				return true;
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
				e.printStackTrace();
			}

		}
		return false;
	}

	public static boolean writeToExternalFile(String filename, byte[] data,	String state) {
		if (isExternalStorageWirtable()) {
			File file = getExternalPublicStorageDir(filename, state);
			try {
				OutputStream os = new FileOutputStream(file);
				os.write(data);
				os.close();
				return true;
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
				e.printStackTrace();
			}

		}
		return false;
	}

	/**
	 * Reads from the external private file String
	 * 
	 * @param filename
	 *            file name
	 * @param context
	 *            context
	 * @return String text, else null
	 */
	public static String readFromExternalFile(String filename, Context context) {
		if (isExternalSotrageReadable()) {
			File file = getExternalStorageDir(filename);
			try {
				FileInputStream is = new FileInputStream(file);
				return Conversions.convertStreamToString(is);
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error reading " + file, e);
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Reads from the external public file String
	 * 
	 * @param filename
	 *            file name
	 * @param state
	 *            state e.g. Environment.DIRECTORY_DOWNLOADS
	 * @return String text, else null
	 */
	public static String readFromExternalFile(String filename,String state) {
		if (isExternalSotrageReadable()) {
			File file= getExternalPublicStorageDir(filename, state);
			try {
				FileInputStream is = new FileInputStream(file);
				return Conversions.convertStreamToString(is);
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error reading " + file, e);
				e.printStackTrace();
			}
		}
		return null;
	}

	/* SHARED PREFERENCES ------------------------------------------------------------------------------------------ */
	public static void saveToSharedPreferences(String prefName, String value,
			String key, Context context, int mode) {
		SharedPreferences sharedPref = context.getSharedPreferences(prefName,
				mode);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void saveToPreferences(String value, String key,
			Activity activity, int mode) {
		SharedPreferences sharedPref = activity.getPreferences(mode);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String readFromSharedPreferences(String defValue,
			String prefName, String key, Context context, int mode) {
		SharedPreferences sharedPref = context.getSharedPreferences(prefName,
				mode);
		String value = sharedPref.getString((key), defValue);
		return value;
	}

	public static String readFromPreferences(String defValue, String key,
			Activity activity, int mode) {
		SharedPreferences sharedPref = activity.getPreferences(mode);
		String value = sharedPref.getString((key), defValue);
		return value;
	}

	/* DATABASE ----------------------------------------------------------------------------------------------------- */


	
	
	
	/* CACHE --------------------------------------------------------------------------------------------------------*/
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File getExternalCacheDir() {
    	Context context = App_1.getAppContext();
    	
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }
        Log.w(LOG_TAG, "ON FROYO");
        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
    
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
	
	// Creates a unique subdirectory of the designated app cache directory. Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getDiskCacheDir(String subDir){
	    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
	    // otherwise use internal cache dir
	    final String cachePath =
	            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
	                    !Environment.isExternalStorageRemovable() ? getExternalCacheDir().getPath() :
	                            App_1.getAppContext().getCacheDir().getPath();

	    return new File(cachePath + File.separator + subDir);
	}
	
	
	// functions -----------------------------------------------------------------------
	/**
	 * Saves bitmap to the sd card
	 * @param bitmap
	 * @param imageName
	 * @return 0- save successful, -1 - not saved, 1 - aleready exist 
	 */
	public static int saveToSD(Bitmap bitmap,String imageName, File directory){
		ByteArrayOutputStream  bytes= new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
		if(isfileExist(imageName, directory)!=null){
			return 1;
		}
		File f = new File(directory,imageName);
		FileOutputStream fo;
		try {
				fo = new FileOutputStream(f);
				fo.write(bytes.toByteArray());
				fo.close();	
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		return -1;
	}
	
	public static File isfileExist(String filename, File path){
		File file = new File(path.getAbsoluteFile()+"/"+filename);
		if(file.exists())
			return file;
		else
			return null;
	}

	
}
