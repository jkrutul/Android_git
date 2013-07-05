package com.example.app_1;

import android.app.Application;
import android.content.Context;


public class App_1 extends Application{
	private static Context context;
	
	public void onCreate(){
		super.onCreate();
		App_1.context = getApplicationContext();
	}
	
	public static Context getAppContext(){
		return App_1.context;
	}

}
