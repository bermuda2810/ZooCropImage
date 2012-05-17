package com.zoostudio.cropimage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zoostudio.cache.DiskLruCache;
import com.zoostudio.cache.MemoryStatus;
import com.zoostudio.imagelayer.LayerCrop;

public class CropImageActivity extends Activity {

	/** Called when the activity is first created. */
	private DiskLruCache mDiskCache;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 5; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";

	private LayerCrop mLayerCrop;
	public static DisplayMetrics metrics;
	public static float DENSITY;
	public static int WIDTH_SCREEN;
	public static int HEIGHT_SCREEN;
	private ImageView mCropImage;

	private Button mBtnCrop;
	private Button mBtnDone;
	private Button mBtnCancel;
	private Button mBtnReCrop;
	private Bitmap mbitmapCroped;

	private Button mBtnRotateLeft;
	private Button mBtnRotateRight;

	private FrameLayout mParent;
	private int minWidth;
	private int minHeight;
	private Bitmap bitmapOrgi;

	public final static int FROM_GALLERY = 0;
	public final static int FROM_CAMERA = 1;
	private static final int INVALID_DIMENSION = 2;
	private static final String IMAGE_KEY = "Image Crop";
	private int mSource;
	private Uri mImageUri;
	private ImageInfo mInfoImage;
	public Uri mCurrentUri;

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Intent intent = new Intent();
			if(msg.what == RESULT_OK){
				intent.setData(mImageUri);
				CropImageActivity.this.setResult(RESULT_OK, intent);
				CropImageActivity.this.finish();
			}else{
				CropImageActivity.this.setResult(RESULT_CANCELED, intent);
				CropImageActivity.this.finish();
			}
		}
	};
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mSource = this.getIntent().getIntExtra("SOURCE", -1);
		minHeight = 256;
		minWidth = 256;
		if (mCurrentUri == null) {
			mCurrentUri = this.getIntent().getData();
		}
		// if (mSource == FROM_CAMERA) {
		// getBitMapFromCamera(this.getIntent().getData());
		// } else if (mSource == FROM_GALLERY) {
		// getBitmapFromGallery(this.getIntent().getData());
		// } else {
		// this.finish();
		// }
		File cacheDir = getCacheDir(this, DISK_CACHE_SUBDIR);
		mDiskCache = DiskLruCache.openCache(this, cacheDir, DISK_CACHE_SIZE);

		// try {
		// InputStream is =
		// this.getResources().getAssets().open("background.jpg");
		// bitmapOrgi = BitmapFactory.decodeStream(is);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// mInfoImage = new ImageInfo();

		mCropImage = (ImageView) this.findViewById(R.id.resultCrop);
		mBtnDone = (Button) this.findViewById(R.id.btnDone);
		mBtnReCrop = (Button) this.findViewById(R.id.btnReCrop);
		mBtnCancel = (Button) this.findViewById(R.id.btnCancel);
		mBtnCrop = (Button) this.findViewById(R.id.btnCrop);

		mBtnRotateLeft = (Button) this.findViewById(R.id.btnRotateLeft);
		mBtnRotateRight = (Button) this.findViewById(R.id.btnRotateRight);

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		WIDTH_SCREEN = getWindowManager().getDefaultDisplay().getWidth();
		HEIGHT_SCREEN = getWindowManager().getDefaultDisplay().getHeight();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Check disk cache in background thread
		Bitmap bitmap = getBitmapFromDiskCache(IMAGE_KEY);
		if (bitmap == null) {
			if (mSource == FROM_CAMERA) {
				bitmap = getBitMapFromCamera(mCurrentUri);
			} else if (mSource == FROM_GALLERY) {
				bitmap = getBitmapFromGallery(mCurrentUri);
			}
		}

		bitmapOrgi = bitmap;
		addBitmapToCache(IMAGE_KEY, bitmapOrgi);
		validData();

		mLayerCrop = new LayerCrop(this.getBaseContext(), bitmapOrgi,
				mInfoImage);
		ImageConfig config = new ImageConfig();
		config.minWidth = minWidth;
		config.maxWidth = 0;
		mLayerCrop.setConfig(config);

		mParent = (FrameLayout) this.findViewById(R.id.parent);
		mParent.addView(mLayerCrop, 1);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mParent.removeViewAt(1);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDiskCache.clearCache();
	}

	private void validData() {
		if (bitmapOrgi.getWidth() < minWidth
				|| bitmapOrgi.getHeight() < minHeight) {
			this.setResult(INVALID_DIMENSION);
			this.finish();
		}
	}

	private Bitmap getBitMapFromCamera(Uri selectedImage) {
		this.getContentResolver().notifyChange(selectedImage, null);
		ContentResolver cr = this.getContentResolver();
		try {
			Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
					cr, selectedImage);
			String filePath = selectedImage.getPath();
			String ext = getExtension(filePath);
			if (ext.equalsIgnoreCase("JPEG") || ext.equalsIgnoreCase("JPG")) {
				mInfoImage = new ImageInfo(filePath);
			} else {
				mInfoImage = new ImageInfo();
			}
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Bitmap getBitmapFromGallery(Uri selectedImage) {
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String filePath = cursor.getString(columnIndex);
		cursor.close();
		String ext = getExtension(filePath);
		if (ext.equalsIgnoreCase("JPEG") || ext.equalsIgnoreCase("JPG")) {
			mInfoImage = new ImageInfo(filePath);
		} else {
			mInfoImage = new ImageInfo();
		}
		Bitmap bitmapOrgi = BitmapFactory.decodeFile(filePath);
		return bitmapOrgi;
	}

	private String getExtension(String file) {
		int dotposition = file.lastIndexOf(".");
		return file.substring(dotposition + 1, file.length());
	}

	public void doCrop(View view) {
		mbitmapCroped = mLayerCrop.cropImage();
		mBtnCrop.setVisibility(View.GONE);
		mBtnDone.setVisibility(View.VISIBLE);
		mBtnReCrop.setVisibility(View.VISIBLE);
		mBtnCancel.setVisibility(View.GONE);
		mLayerCrop.setVisibility(View.GONE);
		mCropImage.setVisibility(View.VISIBLE);
		mBtnRotateLeft.setVisibility(View.VISIBLE);
		mBtnRotateRight.setVisibility(View.VISIBLE);
		mCropImage.setImageBitmap(mbitmapCroped);
	}

	public void doCancel(View view) {
		this.setResult(RESULT_CANCELED);
		this.finish();
	}

	public void doFinish(View view) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Processing...");
		progressDialog.show();
		saveCropToUri();
	}
	
	private void saveCropToUri(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					File photo = createTemporaryFile("picture", ".png");
					photo.delete();
					BufferedOutputStream outputStream = new BufferedOutputStream(
							new FileOutputStream(photo));
					mbitmapCroped
							.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
					mImageUri = Uri.fromFile(photo);
					outputStream.flush();
					outputStream.close();
					handler.sendEmptyMessage(RESULT_OK);
				} catch (IOException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(RESULT_CANCELED);
				}
				progressDialog.dismiss();
			}
		}).start();
		
	}
	
	public void doReCrop(View view) {
		mBtnCancel.setVisibility(View.VISIBLE);
		mBtnReCrop.setVisibility(View.GONE);
		mBtnCrop.setVisibility(View.VISIBLE);
		mBtnDone.setVisibility(View.GONE);
		mCropImage.setVisibility(View.GONE);
		mbitmapCroped.recycle();
		mLayerCrop.setVisibility(View.VISIBLE);
		mBtnRotateLeft.setVisibility(View.GONE);
		mBtnRotateRight.setVisibility(View.GONE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLayerCrop.releaseBitmap();
	}

	public static File createTemporaryFile(String part, String ext)
			throws IOException {
		File tempDir = Environment.getExternalStorageDirectory();
		tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		return File.createTempFile(part, ext, tempDir);
	}

	// Creates a unique subdirectory of the designated app cache directory.
	// Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
				|| MemoryStatus.externalMemoryAvailable() ? context
				.getExternalCacheDir().getPath() : context.getCacheDir()
				.getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	public void addBitmapToCache(String key, Bitmap bitmap) {

		// Also add to disk cache
		if (!mDiskCache.containsKey(key)) {
			mDiskCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromDiskCache(String key) {
		return mDiskCache.get(key);
	}

	public void rotateLeft(View view) {
		Bitmap mPreBitmap = mbitmapCroped.copy(Bitmap.Config.ARGB_8888, true);
		Matrix transform = new Matrix();
		transform.setTranslate(mPreBitmap.getWidth() / 2,
				mPreBitmap.getHeight() / 2);
		transform.preRotate(-90, mPreBitmap.getWidth() / 2,
				mPreBitmap.getHeight() / 2);
		mbitmapCroped = Bitmap.createBitmap(mPreBitmap, 0, 0,
				mPreBitmap.getWidth(), mPreBitmap.getHeight(), transform, true);
		mCropImage.setImageBitmap(mbitmapCroped);
		mCropImage.invalidate();
		mPreBitmap.recycle();
	}

	public void rotateRight(View view) {
		Bitmap mPreBitmap = mbitmapCroped.copy(Bitmap.Config.ARGB_8888, true);
		Matrix transform = new Matrix();
		transform.setTranslate(mPreBitmap.getWidth() / 2,
				mPreBitmap.getHeight() / 2);
		transform.preRotate(90, mPreBitmap.getWidth() / 2,
				mPreBitmap.getHeight() / 2);
		mbitmapCroped = Bitmap.createBitmap(mPreBitmap, 0, 0,
				mPreBitmap.getWidth(), mPreBitmap.getHeight(), transform, true);
		mCropImage.setImageBitmap(mbitmapCroped);
		mCropImage.invalidate();
		mPreBitmap.recycle();
	}
}