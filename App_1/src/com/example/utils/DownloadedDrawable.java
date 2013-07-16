package com.example.utils;

import java.lang.ref.WeakReference;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.utils.ImageDownloader.BitmapDownloaderTask;

public class DownloadedDrawable extends ColorDrawable{
	private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;
	
	public DownloadedDrawable(BitmapDownloaderTask bdt){
		super(Color.BLACK);
		bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bdt);
	}
	
	public BitmapDownloaderTask getBitmapDownloaderTask(){
		return bitmapDownloaderTaskReference.get();
	}

}
