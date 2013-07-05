package com.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.example.utils.FeedReaderContract.FeedEntry.FeedReaderDbHelper;

public class Storage {

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
	public static void deleteInternalFile(String filename, Context c) {
		c.deleteFile(filename);
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
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	private static File getExternalStorageDir(Context context, String filename) {
		// Get the directory for the app's private directory
		File file = new File(context.getExternalFilesDir(null), filename);
		if (!file.mkdirs()) {
			Log.e("TAG", "Directory not created");
		}
		return file;
	}

	private static File getExternalPublicStorageDir(String filename,
			String state) {
		File sdpath = Environment.getExternalStoragePublicDirectory(state);
		// String sdpath=
		// Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(sdpath + File.separator + filename);
		if (!file.getParentFile().mkdirs()) {
			Log.e("TAG", "Directory not created");
		}
		return file;
	}

	public static boolean deleteExtFile(String filename, Context context) {
		if (isExternalSotrageReadable()) {
			File file = getExternalStorageDir(context, filename);
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
	
	public static boolean writeToExternalFile(String filename, byte[] data,	Context context) {
		if (isExternalStorageWirtable()) {
			File file = getExternalStorageDir(context, filename);
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
			File file = getExternalStorageDir(context, filename);
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

	
}
