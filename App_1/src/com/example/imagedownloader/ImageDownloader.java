/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.imagedownloader;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.example.app_1.App_1;
import com.example.app_1.R;
import com.example.utils.BitmapCalc;
import com.example.utils.DiskLruImageCache;
import com.example.utils.Storage;

/**
 * This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * 
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * 
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
public class ImageDownloader {	
	private static final String LOG_TAG = "ImageDownloader";


	Context context = App_1.getAppContext();
	private Bitmap mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(context.getResources(), R.drawable.image_placeholder, 100, 100);
	
	private File imagesDirectory;
	
	private DiskLruImageCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB

	String ResDiskCacheDir;

	private static final int IMG_W = 500;
	private static final int IMG_H = 500;
	private static int JPG_QUALITY = 100;
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	final int cacheSize = maxMemory / 8;
	private LruCache<String, Bitmap> mMemoryCache;
	
	private static boolean withRetainFragment = true;
	
	
	private ImageDownloader(){
		
	}
	
	public ImageDownloader(Activity actv){
		imagesDirectory = Storage.getImagesDirectory();
		if(withRetainFragment){
			RetainFragment mRetainFragment = RetainFragment.findOrCreateRetainFragment(actv.getFragmentManager());
			mMemoryCache = RetainFragment.mRetainedCache;
			if (mMemoryCache == null) {
				mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
					@Override
					protected int sizeOf(String key, Bitmap bitmap) {
						return bitmap.getByteCount() / 1024;
					}
	
				};
				mRetainFragment.mRetainedCache = mMemoryCache;
			}
		}else{
			mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return bitmap.getByteCount() / 1024;
				}
			};
		}
		
	    File diskCacheDir = Storage.getDiskCacheDirectory(IMG_W, IMG_H);
	    Log.d(LOG_TAG, "DISK cache Dir: "+ diskCacheDir);
	    new InitDiskCacheTask().execute(diskCacheDir);
	    
//------------------------------------------------------ END OF INIT CACHE
	}



	
	/**
	 * Download the specified image from the Internet and binds it to the
	 * provided ImageView. The binding is immediate if the image is found in the
	 * cache and will be done asynchronously otherwise. A null bitmap will be
	 * associated to the ImageView if an error occurs.
	 * 
	 * @param url
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */
	public void download(String url, ImageView imageView) {
		if (url == null) {
			imageView.setImageDrawable(null);
			return;
		}
		if(cancelPotentialDownload(url, imageView)){
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(context.getResources(), mPlaceHolderBitmap,task);
			imageView.setImageDrawable(downloadedDrawable);
			imageView.setMinimumHeight(156);
			task.execute(url);			
		}
	}


	/**
	 * Returns true if the current download has been canceled or if there was no
	 * download in progress on this image view. Returns false if the download in
	 * progress deals with the same url. The download is not stopped in that
	 * case.
	 */
	private static boolean cancelPotentialDownload(String url,ImageView imageView) {
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active download task (if any) associated
	 *         with this imageView. null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(
			ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	Bitmap downloadBitmap(String url) {
		final int IO_BUFFER_SIZE = 4 * 1024;

		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client =  AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					// return BitmapFactory.decodeStream(inputStream);
					// Bug on slow connections, fixed in future release.
					return BitmapFactory.decodeStream(new FlushedInputStream(
							inputStream));
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Incorrect URL: " + url);
		} catch (Exception e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
		} finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}
		return null;
	}


	// INER CLASSES
	
	/*
	 * An InputStream that skips the exact number of bytes provided, unless it
	 * reaches EOF.
	 */
	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String url;
		private File extFile;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		/**
		 * Actual download method.
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap b;
			url = params[0];
			String imageName = url.substring( url.lastIndexOf('/')+1, url.length() );
			
			// Check image directory
			
			// Check disk cache in background thread
	        b = getBitmapFromDiskCache(imageName);	 
	        if(b==null){
		        	if((extFile =Storage.isfileExist(imageName, imagesDirectory))!=null){
						Log.d(LOG_TAG, imageName+" - file Length: "+extFile.length()/1024+"kB");
						b= BitmapCalc.decodeSampleBitmapFromFile(extFile.getAbsolutePath(), IMG_W, IMG_H);
					    addBitmapToCache(imageName, b);
						Log.d("cache", imageName+" - has been resized and add to cache, size:"+ b.getByteCount()/1024+"kB");
						return b;
						
		        	}
					else{ 
						b = downloadBitmap(url);
						if(b!=null){
							int saveStatus = Storage.saveToSD(b, imageName,imagesDirectory);
							switch (saveStatus) {
							case 0:
								Log.d("SD", imageName+" - saved successful");
								b = BitmapCalc.getResizedBitmap(b, IMG_W, IMG_H);
						        addBitmapToCache(imageName, b);
								Log.d("cache", imageName+" - has been resized and add to cache, size:"+ b.getByteCount()/1024+"kB");
								break;
							default:
								Log.e("SD", url+" - not saved");
								break;
							}
						}
						return b;
					}
		        }
	        
	        //addBitmapToCache(imageName, b);
	        Log.d(LOG_TAG,"bitmap gets" +imageName+ " from DiskCache, size:"+b.getByteCount()/1024+"kB");
	        return b; // bitmap form cache 
	      }
		
	    public void addBitmapToCache(String key, Bitmap bitmap) {
	        // Add to memory cache as before
	        if (mMemoryCache.get(key) == null) {
	            mMemoryCache.put(key, bitmap);
	        }

	        // Also add to disk cache
	        synchronized (mDiskCacheLock) {
	            if (mDiskLruCache != null && mDiskLruCache.getBitmap(key) == null) {
	                mDiskLruCache.put(key, bitmap);
	            }
	        }
	    }
	    public Bitmap getBitmapFromDiskCache(String key) {
	        synchronized (mDiskCacheLock) {
	            // Wait while disk cache is started from background thread
	            while (mDiskCacheStarting) {
	                try {
	                    mDiskCacheLock.wait();
	                } catch (InterruptedException e) {}
	            }
	            if (mDiskLruCache != null) {
	                return mDiskLruCache.getBitmap(key);
	            }
	        }
	        return null;
	    }	
			
		

		// Once the image is downloaded, associates it to the imageView
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null && bitmap!=null) {
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				if(this == bitmapDownloaderTask && imageView !=null){
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download
	 * is in progress.
	 * 
	 * <p>
	 * Contains a reference to the actual download task, so that a download task
	 * can be stopped if a new binding is required, and makes sure that only the
	 * last started download process can bind its result, independently of the
	 * download finish order.
	 * </p>
	 */
	static class DownloadedDrawable extends BitmapDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(Resources res, Bitmap bitmap, BitmapDownloaderTask bitmapDownloaderTask) {
			super(res,bitmap);
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}
	
	
	/* C A C H E */

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	            File cacheDir = params[0];
	            mDiskLruCache = new DiskLruImageCache(cacheDir, DISK_CACHE_SIZE, CompressFormat.JPEG, JPG_QUALITY);
	            mDiskCacheStarting = false; // Finished initialization
	            mDiskCacheLock.notifyAll(); // Wake any waiting threads
	        }
	        return null;
	    }
	}

	@SuppressLint("ValidFragment")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static class RetainFragment extends Fragment {
		private static final String TAG = "RetainFragment";
		public static LruCache<String, Bitmap> mRetainedCache;

		public RetainFragment() {
		}

		public static RetainFragment findOrCreateRetainFragment(
				FragmentManager fm) {
			RetainFragment fragment = (RetainFragment) fm
					.findFragmentByTag(TAG);
			if (fragment == null) {
				fragment = new RetainFragment();
			}
			return fragment;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

	}
}
