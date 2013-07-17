package com.example.imageloader;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.example.app_1.R;
import com.example.utils.BitmapCalc;
import com.example.utils.DiskLruImageCache;
import com.example.utils.Storage;

public class ImageLoader {
	private Context context;
	private Bitmap mPlaceHolderBitmap;
	
	private DiskLruImageCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	
	
	
	// Get max available VM memory, exceeding this amount will throw an
	// OutOfMemory exception. Stored in kilobytes as LruCache takes an
	// int in its constructor.
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);


	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory / 8;

	private LruCache<String, Bitmap> mMemoryCache;
	
	private ImageLoader(){
		
	}

	public ImageLoader(Context c) {
		this.context = c;
		mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(context.getResources(), R.drawable.image_placeholder, 100, 100);
		
		
		/* INITIALIZE MEMORY CACHE */
		
	    // Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = maxMemory / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};
		
		
		/* INITIALIZE DISK CACHE */
	    File cacheDir = Storage.getDiskCacheDir(context, DISK_CACHE_SUBDIR);
	    new InitDiskCacheTask().execute(cacheDir);

		
	

	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	public void loadBitmap(int resId, ImageView imageView) {
	    if (cancelPotentialWork(resId, imageView)) {
	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        final AsyncDrawable asyncDrawable =   new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(resId);
	    }
	}
	
	public static boolean cancelPotentialWork(int data, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final int bitmapData = bitmapWorkerTask.data;
	        if (bitmapData != data) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		   if (imageView != null) {
		       final Drawable drawable = imageView.getDrawable();
		       if (drawable instanceof AsyncDrawable) {
		           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
		           return asyncDrawable.getBitmapWorkerTask();
		       }
		    }
		    return null;
		}
	
	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private int data = 0;

	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(Integer... params) {
	        data = params[0];
	        final String imageKey = String.valueOf(data);
	        
	        // Check disk cache in background thread
	        Bitmap bitmap = getBitmapFromDiskCache(imageKey);
	        if (bitmap == null) { // Not found in disk cache
	        bitmap = BitmapCalc.decodeSampleBitmapFromResources(context.getResources(), data, 100, 100);
	        }
	        // add final bitmap to caches
	        addBitmapToCache(imageKey,bitmap);
	        return bitmap;
	    }
	    public void addBitmapToCache(String key, Bitmap bitmap) {
	        // Add to memory cache as before
	        if (getBitmapFromMemCache(key) == null) {
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


	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (isCancelled()) {
	            bitmap = null;
	        }

	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            final BitmapWorkerTask bitmapWorkerTask =
	                    getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	                imageView.setImageBitmap(bitmap);
	            }
	        }
	    }
	}
	static class AsyncDrawable extends BitmapDrawable {
	    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap bitmap,
	            BitmapWorkerTask bitmapWorkerTask) {
	        super(res, bitmap);
	        bitmapWorkerTaskReference =
	            new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
	
	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	            File cacheDir = params[0];
	            mDiskLruCache = new DiskLruImageCache(context,cacheDir.getAbsolutePath(), DISK_CACHE_SIZE, CompressFormat.PNG, 100);
	            mDiskCacheStarting = false; // Finished initialization
	            mDiskCacheLock.notifyAll(); // Wake any waiting threads
	        }
	        return null;
	    }
	}

	
}