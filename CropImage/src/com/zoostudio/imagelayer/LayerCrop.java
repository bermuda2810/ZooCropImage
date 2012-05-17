package com.zoostudio.imagelayer;

import java.io.IOException;

import com.zoostudio.cropimage.ImageConfig;
import com.zoostudio.cropimage.ImageInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


public class LayerCrop extends View {

	private static final int DRAG = 1;
	private static final int NONE = 0;
	private static final int ZOOM = 2;

	private static final int ZOOM_IN = 0;
	private static final int ZOOM_OUT = 1;

	private static final int DIRECT_RIGHT = 0;
	private static final int DIRECT_LEFT = 1;
	private static final int DIRECT_NONE = -1;

	private static final boolean IMAGE_ZOOM_MAX = false;
	private static float DEFAULT_WIDHT_RECT = 300;
	private static final float DEFAULT_HEIGHT_RECT = 300;

	private static final int TOP_CLICKED = 0;
	private static final int BOTTOM_CLICKED = 1;
	private static final int RIGHT_CLICKED = 2;
	private static final int LEFT_CLICKED = 3;

	private static float MAX_IMAGE_WIDTH = 0;
	private static float MAX_BOUND_ZOOM = 0;
	
	private static float MIN_SIZE;
	
	private float mWidthRect;
	private float mHeightRect;

	private float mWithImage;
	private int mHeightImage;
	/**
	 * BIEN LUU TRU HINH VUONG BOUND
	 */
	private RectF mRectBound;
	/**
	 * BIEN LUU TRU GIA TRI TOP CUA BOUND
	 */
	private float mTop;
	/**
	 * BIEN LUU TRU GIA TRI LEFT CUA BOUND
	 */
	private float mLeft;
	/**
	 * BITMAP GOC CUA ANH CAN CROP
	 */
	private Bitmap mOriginalImage;
	/**
	 * BITMAP CUA CAC HINH TRON TREN BOUND
	 */
	private Bitmap mCircle;
	/**
	 * BIEN LUU TRU CONFIG DE VE BACKGROUND
	 */
	private Paint mPaintBackGround;
	/**
	 * BIEN LUU TRU CONFIG PAINT DE VE HINH VUONG
	 */
	private Paint mPaintBound;
	/**
	 * LUU CONFIG DE VE CAC MASK (4 MASK)
	 */
	private Paint mMaskPaint;
	/**
	 * BIEN LUU TRU TRANG THAI HIEN TAI CUA HANH DONG (TOUND , DRAP, ZOOM)
	 */
	private int mMode;
	private float mLastDistanceX;
	private float mlastDistanceY;
	private Paint mPaintCircle;
	private int radiusCircle;

	private RectF mRectTop;
	private RectF mRectBottom;
	private RectF mRectLeft;
	private RectF mRectRight;

	private float mLastX;
	private float mLastY;
	
	private float mDistanceX;
	private float mDistanceY;
	
	private float currentY;
	private float currentX;
	private float mCurrentDistance;

	private int mCurrentZoom;
	private int mCurrentDirect;
	private RectF desImage;

	private float mRatioZoom = 1;

	private static float MIN_ZOOM  = 156;
	private static float MAX_ZOOM = 156;
	
	private float mNewWithRect;
	private float mOrgRatio;
	private int heightScreen;
	private float MIN_IMAGE_WIDTH;
	private RectF temp;
	private int mCurrentClicked;
	private boolean mResultGeZoomType;

	private boolean mHasBoundTopImage = false;
	private boolean mHasBoundBottomImage;
	private int mCurrentMoveByVer;
	private Handler handler;

	private final static int MOVE_UP = 0;
	private final static int MOVE_DOWN = 1;
	private static final int MOVE_LEFT = 2;
	private static final int MOVE_RIGHT = 3;
	private static final int MOVE_NONE = -1;
	private static float MIN_DISTANCE = 150;
	
	private int mCurrentImageZoom;
	private float mWidthBoundBeforeZoom;
	private float mNewImageWith;
	private float mNewImageHight;
	private float widthScreen;
	private float mTempWidth;
	private float mTempHeight;

	// Khoang cach giua top cua bound va toop cua Image
	private float distanceY;
	// Khoang cach giua left cua bound va left cua Image
	private float distanceX;
	private int mCurrentMoveByHor;
	private boolean mHasBoundLeft;
	private boolean mHasBoundRight;

	private RectF mRectBlurTop;
	private RectF mRectBlurBottom;
	private RectF mRectBlurLeft;
	private RectF mRectBlurRight;

	// Vi tri cua hinh vuong mask top
	private float mTopArea1;
	private float mLeftArea1;
	private float mWidthArea1;
	private float mHeightArea1;

	// Vi tri cua hinh vuong mask left
	private float mTopArea2;
	private float mLeftArea2;
	private float mWidthArea2;
	private float mHeightArea2;

	// Vi tri cua hinh vuong mask right
	private float mTopArea3;
	private float mLeftArea3;
	private float mWidthArea3;
	private float mHeightArea3;
	private float mTopArea4;
	private float mLeftArea4;
	private float mWidthArea4;
	private float mHeightArea4;
	private float mWidthTempImage;
	
	//Bien luu so pixel dich chuyen cua bound
	private float offsetDistance = 0;
	
	//
	private ImageInfo mImageInfo;
	private Bitmap mPreBitmap;
	private int mTempWidthCrop;
	private int mTempHeightCrop;
	private boolean mHasBoundTopScreen;
	private boolean mHasBoundBottomScreen;
	
	public LayerCrop(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initVariables();
	}
	
	public void setConfig(ImageConfig config){
		MIN_ZOOM = config.minWidth;
		MAX_ZOOM = config.maxWidth;
	}
	
	public LayerCrop(Context context,Bitmap bitmap,ImageInfo imageInfo){
		super(context);
		this.mPreBitmap = bitmap;
		this.mImageInfo = imageInfo;
		initImageRotate();
		initVariables();
	}
	public LayerCrop(Context context, AttributeSet attrs) {
		super(context, attrs);
		initVariables();
	}

	public LayerCrop(Context context) {
		super(context);
		initVariables();
	}
	private void initImageRotate(){
		float turnDegrees = 0;

		if(mImageInfo.getOrientation() == ExifInterface.ORIENTATION_NORMAL){
			mOriginalImage = mPreBitmap;
			return;
		}else if(mImageInfo.getOrientation() == ExifInterface.ORIENTATION_ROTATE_90){
			turnDegrees = 90;
		}else if(mImageInfo.getOrientation() == ExifInterface.ORIENTATION_ROTATE_180){
			turnDegrees = 180;
		}else if(mImageInfo.getOrientation() == ExifInterface.ORIENTATION_ROTATE_270){
			turnDegrees = 270;
		}
		Matrix transform = new Matrix();
	    transform.setTranslate(mPreBitmap.getWidth()/2, mPreBitmap.getHeight()/2);
	    transform.preRotate(turnDegrees, mPreBitmap.getWidth()/2, mPreBitmap.getHeight()/2);
		mOriginalImage = Bitmap.createBitmap(mPreBitmap, 0, 0, mPreBitmap.getWidth(), mPreBitmap.getHeight(), transform, true);
		mPreBitmap.recycle();
	}
	private void initVariables() {
		
		handler = new Handler();
		mPaintBackGround = new Paint();
		mPaintBackGround.setAntiAlias(true);

		mPaintBound = new Paint();
		mPaintBound.setAntiAlias(true);

		mPaintCircle = new Paint();
		mPaintCircle.setAntiAlias(true);

		mMaskPaint = new Paint();
		mMaskPaint.setAntiAlias(true);
		mMaskPaint.setColor(Color.BLACK);
		mMaskPaint.setAlpha(100);

		mRectBound = new RectF();

		mTop = 0;
		mLeft = 0;

		mWidthRect = DEFAULT_WIDHT_RECT;
		mHeightRect = DEFAULT_HEIGHT_RECT;
		
		MIN_SIZE = 156;
		
		mRectTop = new RectF();
		mRectBottom = new RectF();
		mRectRight = new RectF();
		mRectLeft = new RectF();

		mRectBlurTop = new RectF();
		mRectBlurLeft = new RectF();
		mRectBlurRight = new RectF();
		mRectBlurBottom = new RectF();

		try {
			mWithImage = mOriginalImage.getWidth();
			mHeightImage = mOriginalImage.getHeight();
			MAX_IMAGE_WIDTH = mWithImage;

			calSizeImage();

			mCircle = BitmapFactory.decodeStream(this.getResources()
					.getAssets().open("circle.png"));
			radiusCircle = mCircle.getWidth() + 15;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		drawBackground(canvas);
		drawBound(canvas);
		drawMask(canvas);
		if (mMode == NONE) {
			drawCircle(canvas);
		}
	}

	private void drawBound(Canvas canvas) {
		mRectBound.set(mLeft, mTop, mLeft + mWidthRect, mTop + mHeightRect);
		mPaintBound.setColor(Color.BLUE);
		mPaintBound.setStyle(Style.STROKE);
		mPaintBound.setStrokeWidth(3);
		canvas.drawRect(mRectBound, mPaintBound);
	}

	private void drawMask(Canvas canvas) {
		canvas.drawRect(mRectBlurTop, mMaskPaint);
		canvas.drawRect(mRectBlurLeft, mMaskPaint);
		canvas.drawRect(mRectBlurRight, mMaskPaint);
		canvas.drawRect(mRectBlurBottom, mMaskPaint);
	}

	private void calSizeImage() {
		float scaleWidth = mWithImage;
		float scaleHeight = mHeightImage;

		mOrgRatio = scaleWidth / scaleHeight;
		
		
		
		WindowManager manager = (WindowManager) this.getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		widthScreen = manager.getDefaultDisplay().getWidth();
		heightScreen = manager.getDefaultDisplay().getHeight();

		if (scaleWidth >= widthScreen) {
			scaleWidth = widthScreen;
			scaleHeight = (scaleWidth / mOrgRatio);
			scaleHeight = (float) Math.floor(scaleHeight);
		} else if (scaleHeight >= heightScreen) {
			scaleHeight = heightScreen;
			scaleWidth = (scaleHeight * mOrgRatio);
			scaleWidth = (float) Math.floor(scaleWidth);
		}
//		MIN_ZOOM = MIN_SIZE / mOrgRatio;
		MIN_IMAGE_WIDTH = scaleWidth;

		float currentWidth = Math.min(scaleWidth, scaleHeight);
		MAX_BOUND_ZOOM = currentWidth;

		MIN_DISTANCE = MAX_BOUND_ZOOM * 2 / 3;

		float top = (heightScreen / 2 - scaleHeight / 2);
		desImage = new RectF(0, top, scaleWidth, top + scaleHeight);

		DEFAULT_WIDHT_RECT = MAX_BOUND_ZOOM;

		mTop = desImage.top;
		mLeft = widthScreen / 2 - DEFAULT_WIDHT_RECT / 2;

		mWidthRect = DEFAULT_WIDHT_RECT;
		mHeightRect = mWidthRect;

		calSizeMask();
	}

	private void drawBackground(Canvas canvas) {
		canvas.drawBitmap(mOriginalImage, null, desImage, mPaintBackGround);
	}
	
	private void drawCircle(Canvas canvas) {
		float left = mLeft + mWidthRect / 2 - mCircle.getWidth() / 2;
		float top = mTop - mCircle.getHeight() / 2;

		// Draw top
		canvas.drawBitmap(mCircle, left, top, mPaintCircle);
		mRectTop.set(left, top, left + radiusCircle, top + radiusCircle);

		// Draw bottom
		top = mTop + mHeightRect - mCircle.getHeight() / 2;
		canvas.drawBitmap(mCircle, left, top, mPaintCircle);
		mRectBottom.set(left, top, left + radiusCircle, top + radiusCircle);

		// Draw left
		left = mLeft - mCircle.getWidth() / 2;
		top = mTop + mHeightRect / 2 - mCircle.getHeight() / 2;
		canvas.drawBitmap(mCircle, left, top, mPaintCircle);
		mRectLeft.set(left, top, left + radiusCircle, top + radiusCircle);

		// Draw right
		left = mLeft + mWidthRect - mCircle.getWidth() / 2;
		canvas.drawBitmap(mCircle, left, top, mPaintCircle);
		mRectRight.set(left, top, left + radiusCircle, top + radiusCircle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mCurrentDirect = checkClickCicler(event);
			if (mRectBound.contains(event.getX(), event.getY())
					|| mCurrentDirect != DIRECT_NONE) {
				if (mCurrentDirect == DIRECT_NONE) {
					mMode = DRAG;
					Log.i("LayerCrop", "DRAG BOUND");
					mLastY = event.getY();
					mLastX = event.getX();
					mlastDistanceY = (mTop - event.getY());
					mLastDistanceX = (mLeft - event.getX());
					return true;
				} else {
					mMode = ZOOM;
					mWidthBoundBeforeZoom = mWidthRect;
					mDistanceX = mRectBound.left - event.getX();
					mDistanceY = mRectBound.top - event.getY();
					mLastX = event.getX();
					mLastY = event.getY();
					return true;
				}
			} else {
				mMode = NONE;
				return true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			Log.i("LayerCrop", "Moving");
			currentX = event.getX();
			currentY = event.getY();

			if (mMode == DRAG) {
				getCurrentMoving();

				temp = new RectF(mRectBound);
				temp.left = currentX + mLastDistanceX;
				temp.top = currentY + mlastDistanceY;
				temp.bottom = temp.top + mHeightRect;
				temp.right = temp.left + mWidthRect;

				validTempBound();

				// Neu da cham vao TOP cua Image dong thoi dang di chuyen len
				// thi chi dich dc sang phai hoac trai
				// Neu khong thi kiem tra xem vung bound co thuoc inside cua
				// image hay khong
				checkMoving();

				mLastY = currentY;
				mLastX = currentX;
				
				Log.i("Drag", "LEFT  =" + mLeft);
				calSizeMask();
				updateView();
				return false;
			} else if (mMode == ZOOM && !IMAGE_ZOOM_MAX) {
				mResultGeZoomType = getCurrentZoom();

				if (mResultGeZoomType == false)
					return false;

				mDistanceX = currentX - mLastX;
				mDistanceY = currentY - mLastY;

				mLastX = currentX;
				mLastY = currentY;

				mCurrentDistance = Math.max(Math.abs(mDistanceX),
						Math.abs(mDistanceY));

				if (mCurrentZoom == ZOOM_IN) {
					mCurrentDistance = -mCurrentDistance;
				}
				// Tinh ti le zoom
				mNewWithRect = mWidthRect + mCurrentDistance;
				
				if(validSize(mNewWithRect)) return true;

				Log.i("LayerCrop", "Ratio Zoom = " + mRatioZoom);

				// Xac dinh lai kich thuoc bound sau khi zoom\

				temp = new RectF();
				temp.left = mRectBound.centerX() - (mNewWithRect / 2);
				temp.top = mRectBound.centerY() - (mNewWithRect / 2);
				
				temp.right = temp.left + mNewWithRect;
				temp.bottom = temp.top + mNewWithRect;
				
				validTempBound();
				
				// Neu bound van con nam trong bound cua Image thi moi ve lai
				if (desImage.contains(temp)) {
					mTop = temp.top;
					mLeft = temp.left;

					mWidthRect = mNewWithRect;
					mHeightRect = mNewWithRect;

					calSizeMask();
					updateView();
				}

			}

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mMode == ZOOM) {
				updateImage();
				calSizeMask();
			}
			mDistanceX = 0;
			mDistanceY = 0;
			mMode = NONE;
			updateView();
		}
		return true;
	}

	private void validTempBound() {
		if (temp.top < desImage.top) {
			temp.top = desImage.top;
		}
		if (temp.left < 0) {
			temp.left = 0;
		}

		if (temp.bottom > desImage.bottom) {
			temp.bottom = desImage.bottom;
			temp.top = temp.bottom - mWidthRect;
		}
		if (temp.right > widthScreen) {
			temp.right = widthScreen;
			temp.left = widthScreen - mWidthRect;
		}
	}
	
	private float validBound(){		
		if(mLeft < 0) {
			offsetDistance = 0-mLeft;
			mLeft = 0;
			return offsetDistance;
		}
		
		if(mLeft + mWidthRect > widthScreen){
			offsetDistance = widthScreen - (mLeft + mWidthRect) ;
			mLeft = widthScreen - mWidthRect;
		}else {
			offsetDistance = 0;
		}
		return offsetDistance;
	}
	
	private void cameraMoveTop(float offset){
		if (desImage.top >= 0)
			return;
		Log.i("MOVING","IMAGE TOP = " + desImage.top + " | offset =  "+ offset);
		if (desImage.top + offset >= 0) {
			offset = Math.abs(desImage.top);
			desImage.top = 0;
		} else {
			desImage.top += offset;
		}
		desImage.bottom += offset;
	}
	
	private void cameraMoveDown(float offset){
		if(desImage.bottom <= heightScreen)
			return;
		
		if(desImage.bottom - offset < heightScreen){
			offset = desImage.bottom - heightScreen;
			desImage.bottom = heightScreen;
		}else{
			desImage.bottom -= offset;
		}
		desImage.top -= offset;
	}
	
	//Dich chuyen man hinh sang ben trai
	private void cameraMoveLeft(float offset) {
		Log.i("ImageMove", "CAMERA MOVING LEFT = " + offset);
		if (desImage.left == 0)
			return;
		if (desImage.left + offset > 0) {
			offset = Math.abs(desImage.left);
			desImage.left = 0;
		} else {
			desImage.left += offset;
		}
		desImage.right += offset;
	}
	//Dich chuyen man hinh sang ben phai
	private void cameraMoveRight(float offset) {
		Log.i("ImageMove", "CAMERA MOVING RIGHT = " + offset);
		if (desImage.right == widthScreen)
			return;
		mWidthTempImage = desImage.width();
		desImage.left -= offset;

		if (desImage.left + mWidthTempImage < widthScreen) {
			desImage.left = widthScreen - mWidthTempImage;
		}
		desImage.right = desImage.left + mWidthTempImage;
	}

	// TODO : Can phai kiem tra lai truong hop ELSE . Dang bi loi logic
	private void checkMoving() {
		
		if (mHasBoundLeft && mCurrentMoveByHor == MOVE_LEFT) {
			cameraMoveLeft(mLastX - currentX);
		} else if (mHasBoundRight && mCurrentMoveByHor == MOVE_RIGHT) {
			cameraMoveRight(currentX - mLastX);
		}
		
		if(mHasBoundTopScreen && mCurrentMoveByVer == MOVE_UP){
			mLeft = temp.left;
			mTop = 0;
			cameraMoveTop(mLastY - currentY);
		}else if(mHasBoundBottomScreen && mCurrentMoveByVer == MOVE_DOWN){
			mLeft = temp.left;
			mTop = heightScreen - mHeightRect;
			cameraMoveDown(currentY- mLastY);
		}
		else if (mHasBoundTopImage && mCurrentMoveByVer == MOVE_UP) {
			mLeft = temp.left;
		} else if (mHasBoundBottomImage && mCurrentMoveByVer == MOVE_DOWN) {
			mLeft = temp.left;
		} else if (desImage.contains(temp)) {
			Log.i("Drag", "INSIDE");
			mLeft = temp.left;
			mTop = temp.top;
			Log.i("Drag", "mLeft = " + mLeft + " | DesImage" + desImage.left);
			// Tinh lai khoang cach tu Left, Top bound cho den x,y hien tai.
			mLastDistanceX = mLeft - currentX;
			mlastDistanceY = mTop - currentY;

			checkCollide();
		}
		mLastDistanceX = mLeft - currentX;
		mlastDistanceY = mTop - currentY;

	}
	
	/**
	 * Kiem tra xem bound da cham vao screen hay cham vao image
	 */
	private void checkCollide() {
		// Kiem tra cham top cua Image hay chua
		if (mTop - 0.1 < desImage.top) {
			mHasBoundTopImage = true;
		} else {
			mHasBoundTopImage = false;
		}
		//Kiem tra cham top cua man hinh hay chua
		
		if(mTop - 0.1 < 0){
			mHasBoundTopScreen = true;
		}else{
			mHasBoundTopScreen = false;
		}
		
		// Kiem tra cham Bottom chua
		if (mTop + 0.1 + mHeightRect > desImage.bottom) {
			mHasBoundBottomImage = true;
		} else {
			mHasBoundBottomImage = false;
		}
		
		//Kiem tra cham bottom cua man hinh hay chua
		if (mTop + 0.1 + mHeightRect >= heightScreen) {
			mHasBoundBottomScreen = true;
		} else {
			mHasBoundBottomScreen = false;
		}
		
		// Kiem tra cham Left
		if (mLeft - 1 <= 0) {
			mHasBoundLeft = true;
		} else {
			mHasBoundLeft = false;
		}

		// Kiem tra cham Right
		if (mLeft + 1 + mWidthRect >= widthScreen) {
			mHasBoundRight = true;
		} else {
			mHasBoundRight = false;
		}
	}

	/**
	 * Xac dinh dang di chuyen theo huong nao
	 */
	private void getCurrentMoving() {
		if (currentY > mLastY) {
			mCurrentMoveByVer = MOVE_DOWN;
		} else if (currentY < mLastY) {
			mCurrentMoveByVer = MOVE_UP;
		} else {
			mCurrentMoveByVer = MOVE_NONE;
		}

		if (currentX < mLastX) {
			mCurrentMoveByHor = MOVE_LEFT;
		} else if (currentX > mLastX) {
			mCurrentMoveByHor = MOVE_RIGHT;
		} else {
			mCurrentMoveByHor = MOVE_NONE;
		}
	}

	// Lay ra current zoom cua Image va Bound
	private boolean getCurrentZoom() {
		switch (mCurrentClicked) {
		case TOP_CLICKED:
			if (currentY < mLastY) {
				Log.i("LayerCrop", "TOP_CLICKED -- ZOOM_OUT");
				mCurrentZoom = ZOOM_OUT;
				mCurrentImageZoom = ZOOM_IN;
				return true;
			} else if (currentY > mLastY) {
				Log.i("LayerCrop", "TOP_CLICKED -- ZOOM_IN");
				mCurrentZoom = ZOOM_IN;
				mCurrentImageZoom = ZOOM_OUT;
				return true;
			}
			return false;
		case BOTTOM_CLICKED:
			if (currentY < mLastY) {
				mCurrentZoom = ZOOM_IN;
				mCurrentImageZoom = ZOOM_OUT;
				Log.i("LayerCrop", "BOTTOM_CLICKED -- ZOOM_IN");
				return true;
			} else if (currentY > mLastY) {
				mCurrentZoom = ZOOM_OUT;
				mCurrentImageZoom = ZOOM_IN;
				Log.i("LayerCrop", "BOTTOM_CLICKED -- ZOOM_OUT");
				return true;
			}
			return false;
		case RIGHT_CLICKED:
			if (currentX > mLastX) {
				mCurrentZoom = ZOOM_OUT;
				mCurrentImageZoom = ZOOM_IN;
				Log.i("LayerCrop", "RIGHT_CLICKED -- ZOOM_OUT");
				return true;
			} else if (currentX < mLastX) {
				mCurrentZoom = ZOOM_IN;
				mCurrentImageZoom = ZOOM_OUT;
				Log.i("LayerCrop", "RIGHT_CLICKED -- ZOOM_IN");
				// Sap xong roi chuan bi dc ve roi`:( co len nao..
				return true;
			}
			return false;
		case LEFT_CLICKED:
			if (currentX < mLastX) {
				mCurrentZoom = ZOOM_OUT;
				mCurrentImageZoom = ZOOM_IN;
				Log.i("LayerCrop", "LEFT_CLICKED -- ZOOM_OUT");
				return true;
			} else if (currentX > mLastX) {
				mCurrentZoom = ZOOM_IN;
				mCurrentImageZoom = ZOOM_OUT;
				Log.i("LayerCrop", "LEFT_CLICKED -- ZOOM_IN");
				return true;
			}
			return false;
		}
		return false;
	}

	private void updateView() {
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				LayerCrop.this.invalidate();
			}
		});
	}

	/**
	 * Update lai image khi zoom bound
	 */
	private void updateImage() {
		if (mCurrentImageZoom == ZOOM_IN && desImage.width() == MIN_IMAGE_WIDTH) {
			return;
		} else if (mCurrentImageZoom == ZOOM_OUT
				&& desImage.width() == MAX_IMAGE_WIDTH) {
			return;
		}
		// Neu Zoom IN ma khoang cach > MIN_DISTANCE thi bo qua khong zoom anh
		if (mCurrentZoom == ZOOM_IN && mWidthRect >= MIN_DISTANCE)
			return;

		// Neu bound zoom in > Image zoom out
		mRatioZoom = mWidthRect / mWidthBoundBeforeZoom;
		
		//Tinh ti le zoom cua bound suy ra ti le zoom cu anh
		Log.i("ZOOM", "FIRST ratio zoom = " + mRatioZoom);
		mRatioZoom = 1f / mRatioZoom;
		Log.i("ZOOM", "LAST ratio zoom = " + mRatioZoom);

		mNewImageWith = desImage.width() * mRatioZoom;
		
//		MIN_ZOOM = MIN_SIZE / mOrgRatio;
//		Log.i("Data","MIN SIZE = " + MIN_SIZE + "| mOrgRatio = " + mOrgRatio + " | MIN ZOOM = " + MIN_ZOOM);
		
		//
		if (mCurrentZoom == ZOOM_OUT && mNewImageWith < MIN_IMAGE_WIDTH) {
			mNewImageWith = MIN_IMAGE_WIDTH;
		} else if (mCurrentZoom == ZOOM_IN && mNewImageWith > MAX_IMAGE_WIDTH) {
			mNewImageWith = MAX_IMAGE_WIDTH;
		}

		mNewImageHight = mNewImageWith / mOrgRatio;
		//
		temp = new RectF();
		float newLeftDistance = (mNewImageWith - desImage.width()) / 2;
		float tempLeft = desImage.left - newLeftDistance;
		
		//Neu sau khi zoom ma left cua image > 0 thi set lai left = 0.
		if (tempLeft > 0)
			tempLeft = 0;
		//Neu sau khi zoom ma width cua image nho hon man hinh thi tinh lai left cua image
		if(tempLeft + mNewImageWith < widthScreen){
			tempLeft = widthScreen - mNewImageWith;
		}
		
		temp.left = tempLeft;
		temp.top = heightScreen / 2 - mNewImageHight / 2;
		temp.right = temp.left + mNewImageWith;
		temp.bottom = temp.top + mNewImageHight;

		// kiem tra sau khi zoom ma left > 0 thi fai dich lai.

		// Tinh khoang cach tu top cua Bound so voi anh truoc khi zoom anh
		distanceY = mRectBound.top - desImage.top;
		// Tinh khoang cach tu Left cua Bound so voi Left cua anh trc khi zoom
		distanceX = mRectBound.left - desImage.left;

		desImage.set(temp);
		// Tinh top hien tai cua Bound doi voi anh sau khi zoom . Nhan voi ti le
		// zoom
		distanceX = distanceX * mRatioZoom;
		distanceY = distanceY * mRatioZoom;
		//
		calMaxBound();
		{
			// Thiet lap lai Top , Left cho Bound
			// Chieu dai moi cua WidthRect nhan voi ti le zoom
			mWidthRect = mWidthRect * mRatioZoom;
			mHeightRect = mWidthRect;
			mTop = temp.top + distanceY;
			mLeft = temp.left + distanceX;
			validBound();
			//Neu offset > 0 tuc la can phai dich anh sang phai nguoc lai dich anh sang trai
			if(offsetDistance > 0) {
				cameraMoveRight(Math.abs(offsetDistance));
				Log.i("ImageMove","Camera need to move right");
			}
			else if(offsetDistance < 0) {
				Log.i("ImageMove","Camera need to move left");
				cameraMoveLeft(Math.abs(offsetDistance));
			}
			//
		}
	}

	private void calMaxBound() {
		mTempWidth = Math.min(mNewImageWith, widthScreen);
		mTempHeight = Math.min(mNewImageHight, heightScreen);
		MAX_BOUND_ZOOM = Math.min(mTempWidth, mTempHeight);
		MIN_DISTANCE = MAX_BOUND_ZOOM * 2 / 3;
	}

	private int checkClickCicler(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (mRectTop.contains(x, y)) {
			mCurrentClicked = TOP_CLICKED;
			return DIRECT_LEFT;
		} else if (mRectBottom.contains(x, y)) {
			mCurrentClicked = BOTTOM_CLICKED;
			return DIRECT_RIGHT;
		} else if (mRectRight.contains(x, y)) {
			mCurrentClicked = RIGHT_CLICKED;
			return DIRECT_RIGHT;
		} else if (mRectLeft.contains(x, y)) {
			mCurrentClicked = LEFT_CLICKED;
			return DIRECT_LEFT;
		}
		return DIRECT_NONE;
	}

	private void calSizeMask() {
		// Xac dinh vi tri mask cua top
		mTopArea1 = desImage.top;
		mLeftArea1 = desImage.left;
		mWidthArea1 = desImage.width();
		mHeightArea1 = mTop - desImage.top;
		mRectBlurTop.set(mLeftArea1, mTopArea1, mLeftArea1 + mWidthArea1,
				mTopArea1 + mHeightArea1);

		// Xac dinh vi tri mask cua left
		mTopArea2 = mTopArea1 + mHeightArea1 - 0.15f; // 1 la kich thuoc cua
														// bound
		mLeftArea2 = mLeftArea1;
		mWidthArea2 = mLeft - mLeftArea2;
		mHeightArea2 = mHeightRect;
		mRectBlurLeft.set(mLeftArea2, mTopArea2, mLeftArea2 + mWidthArea2,
				mTopArea2 + mHeightArea2);

		// Xac dinh vi tri mask cua right
		mTopArea3 = mTopArea2; // 1 la kich thuoc cua bound
		mLeftArea3 = mRectBlurLeft.right + mWidthRect;
		mWidthArea3 = widthScreen - mLeftArea3;
		mHeightArea3 = mHeightArea2;
		mRectBlurRight.set(mLeftArea3, mTopArea3, mLeftArea3 + mWidthArea3,
				mTopArea3 + mHeightArea3);

		// Xac dinh vi tri cua mask bottom
		mTopArea4 = mRectBlurRight.bottom -0.15f; // 1 la kich thuoc cua bound
		mLeftArea4 = mRectBlurTop.left;
		mWidthArea4 = mWidthArea1;
		mHeightArea4 = desImage.bottom - mRectBlurLeft.bottom;
		mRectBlurBottom.set(mLeftArea4, mTopArea4, mLeftArea4 + mWidthArea4,
				mTopArea4 + mHeightArea4);
	}

	public Bitmap cropImage() {
		
		double ratioWidth = mWithImage / desImage.width();
		float ratioHeight = mHeightImage / desImage.height();
		
		double tLeft = (Math.floor(mRectBound.left) + (0 -Math.floor(desImage.left)));
		double tTop = (Math.floor(mRectBound.top) + (0 - Math.floor(desImage.top)));
		
		int left = (int) Math.floor(tLeft * ratioWidth);
		int top  = (int) Math.floor(tTop * ratioHeight);
		
		int widthCrop = (int) Math.floor((mRectBound.width() * ratioWidth));
		int heightCrop  = (int)Math.floor((mRectBound.height() * ratioHeight));
		
		Toast.makeText(getContext(), "Width = " + widthCrop + " |  Height = " + heightCrop, Toast.LENGTH_SHORT).show();
		Bitmap temp = mOriginalImage.copy(Bitmap.Config.ARGB_8888, false);
	    Bitmap bmOverlay = Bitmap.createBitmap(temp,left,top,widthCrop,heightCrop);
	    temp.recycle();
	    return bmOverlay;
	}
	
	/**
	 * Kiem tra xem kich thuoc cua bound co nho? hon min zoom hay lon max bound cua zoom
	 * - Kiem tra kich thuoc that. phai nho hon MIN (ZOOM).
	 * - Dong thoi kich thuoc width phai nho? honn MAX BOUND ZOOM
	 * 
	 * @param width
	 * @return
	 */
	private boolean validSize(float width){
		double mTempRatioWidth = mWithImage / desImage.width();
		mTempWidthCrop = (int) Math.floor((width * mTempRatioWidth));
		Log.i("ZOOM","Widht = " + width + "|  MAX  = " + width);
		if(mTempWidthCrop < MIN_ZOOM || width  >  MAX_BOUND_ZOOM){
			return true;
		}
		return false;
	}
	
	public void releaseBitmap(){
		mOriginalImage.recycle();
	}

	public void setBitmapOriginal(Bitmap bitmapOrgi) {
		this.mOriginalImage = bitmapOrgi;
	}
	
}
