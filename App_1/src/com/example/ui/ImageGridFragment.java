package com.example.ui;

import java.io.File;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.app_1.App_1;
import com.example.app_1.R;

import com.example.ui.ImageDetailActivity.AsyncDrawable;
import com.example.ui.ImageDetailActivity.BitmapWorkerTask;
import com.example.utils.BitmapCalc;
import com.example.utils.DiskLruImageCache;
import com.example.utils.Storage;

public class ImageGridFragment extends Fragment implements
		AdapterView.OnItemClickListener {

	private DiskLruImageCache mDiskLruCache;
	public static final String LOG_TAG = "ImageGridFragment";
	private LruCache<String, Bitmap> mMemoryCache;
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "IGF_thumbnails";
	private static int IMG_W = 300;
	private static int IMG_H = 300;
    private int mImageThumbSize= 300;
    private int mImageThumbSpacing= 0;
	private final Object mDiskCacheLock = new Object();

	private static final File imagesDirectory = Storage.getImagesDirectory();

	// Get max available VM memory, exceeding this amount will throw an
	// OutOfMemory exception. Stored in kilobytes as LruCache takes an
	// int in its constructor.
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory / 8;

	private ImageGridAdapter mAdapter;

	// A static dataset to back the GridView adapter
	public final static Integer[] imageResIds = new Integer[] {};

	// Empty constructor as per Fragment docs
	public ImageGridFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new ImageGridAdapter();
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
			Log.w(LOG_TAG, "old target");

			mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {/*
																	 * INITIALIZE
																	 * MEMORY
																	 * CACHE
																	 */
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return bitmap.getByteCount() / 1024;
				}
			};
		}
		/* INITIALIZE DISK CACHE */
		File cacheDir = Storage.getDiskCacheDir(DISK_CACHE_SUBDIR);
		new InitDiskCacheTask().execute(cacheDir);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_grid_fragment,
				container, false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
        
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(columnWidth);
                                    Log.d(LOG_TAG, "onCreateView - numColumns set to " + numColumns);  
                            }
                        }
                    }
                });
		
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, position);
		startActivity(i);
	}

	private class ImageGridAdapter extends BaseAdapter {
		private final Context mContext;
		public File[] fileList;
		private final int mSize;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
        private int mActionBarHeight = 0;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageGridAdapter() {
			super();
			fileList = Storage.getFilesList(imagesDirectory);
			mSize = fileList.length;
			mContext = App_1.getAppContext();
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		@Override
		public int getCount() {
			return mSize + mNumColumns;
		}

		@Override
		public Object getItem(int position) {
			if (position < mNumColumns) {
				return null;
			}
			return fileList[position-mNumColumns];
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
            if (position < mNumColumns) {
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                // Set empty view with height of ActionBar
                convertView.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
                return convertView;
            }
            
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else {
				imageView = (ImageView) convertView;
			}
			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			loadBitmap(fileList[position - mNumColumns], imageView);

			return imageView;
		}

		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			IMG_H = height;
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}

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

	@SuppressLint("ValidFragment")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static class RetainFragment extends Fragment {
		private static final String TAG = "RetainFragment";
		public static LruCache<String, Bitmap> mRetainedCache;

		public RetainFragment() {
		}

		public static RetainFragment findOrCreateRetainFragment(
				android.support.v4.app.FragmentManager fragmentManager) {
			RetainFragment fragment = (RetainFragment) fragmentManager
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

}