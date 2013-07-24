package com.example.ui;

import java.io.File;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageView;

import com.example.app_1.R;
import com.example.utils.BitmapCalc;
import com.example.utils.DiskLruImageCache;
import com.example.utils.Storage;

@SuppressLint("ValidFragment")
public class ImageDetailActivity extends FragmentActivity {
	private DiskLruImageCache mDiskLruCache;
	public static final String LOG_TAG = "ImageDetailActivity";
	private LruCache<String, Bitmap> mMemoryCache;
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "IDA_thumbnails";
	private static final int IMG_W = 500;
	private static final int IMG_H = 500;

	private final Object mDiskCacheLock = new Object();

	private static final File imagesDirectory = Storage.getImagesDirectory();
	public static final String EXTRA_IMAGE = null;

	// Get max available VM memory, exceeding this amount will throw an
	// OutOfMemory exception. Stored in kilobytes as LruCache takes an
	// int in its constructor.
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory / 8;


	private ImagePagerAdapter mAdapter;
	private ViewPager mPager;

	public void loadBitmap(File path, ImageView mImageView) {
		final String key = path.getName();

		final Bitmap bitmap = mMemoryCache.get(key);
		if (bitmap != null)
			mImageView.setImageBitmap(bitmap);
		else {
			mImageView.setImageResource(R.drawable.image_placeholder);
			BitmapWorkerTask task = new BitmapWorkerTask(mImageView);
			task.execute(path);
		}
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (mMemoryCache.get(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			RetainFragment mRetainFragment = RetainFragment
					.findOrCreateRetainFragment(getFragmentManager());
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
		} catch (Exception e) {
			Log.w(LOG_TAG,"old target");

			mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {/* INITIALIZE MEMORY CACHE */
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return bitmap.getByteCount() / 1024;
				}
			};
		} 
		/* INITIALIZE DISK CACHE */
		File cacheDir = Storage.getDiskCacheDir(DISK_CACHE_SUBDIR);
		new InitDiskCacheTask().execute(cacheDir);

		setContentView(R.layout.image_detail_pager); // Contains just a
														// ViewPager

		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
	}

	public static class ImagePagerAdapter extends FragmentStatePagerAdapter {
		public static File[] fileList;
		private final int mSize;

		public ImagePagerAdapter(
				android.support.v4.app.FragmentManager fragmentManager) {
			super(fragmentManager);
			
			
			fileList = Storage.getFilesList(imagesDirectory);
			mSize = fileList.length;
		
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {
			return ImageDetailFragment.newInstance(position);
		}
	}

	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private File file;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(File... params) {
			file = params[0];
			final String imageKey = file.getName();

			// Check disk cache in background thread
			Bitmap bitmap = getBitmapFromDiskCache(imageKey);
			if (bitmap == null) { // Not found in disk cache
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(
						file.getAbsolutePath(), IMG_W, IMG_H);
			}
			// add final bitmap to caches
			addBitmapToCache(imageKey, bitmap);
			return bitmap;
		}

		public void addBitmapToCache(String key, Bitmap bitmap) {
			// Add to memory cache as before
			if (mMemoryCache.get(key) == null) {
				mMemoryCache.put(key, bitmap);
			}

			// Also add to disk cache
			synchronized (mDiskCacheLock) {
				if (mDiskLruCache != null
						&& mDiskLruCache.getBitmap(key) == null) {
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
					} catch (InterruptedException e) {
					}
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
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (/* this == bitmapWorkerTask && */imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
		@Override
		protected Void doInBackground(File... params) {
			synchronized (mDiskCacheLock) {
				File cacheDir = params[0];
				mDiskLruCache = new DiskLruImageCache(cacheDir,
						DISK_CACHE_SIZE, CompressFormat.JPEG, 100);
				mDiskCacheStarting = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			return null;
		}
	}

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
