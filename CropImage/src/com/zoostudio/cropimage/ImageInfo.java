package com.zoostudio.cropimage;

import java.io.IOException;

import android.media.ExifInterface;

public class ImageInfo {
	public static final int LEFT_LANS = 0;
	public static final int RIGHT_LANS = 1;
	public static final int TOP_PORT = 2;
	public static final int BOTTOM_PORT = 3;
	private String mDateTime;
	private String mFlash;
	private String mGpsLat;
	private String mGpsLatRef;
	private String mGpsLong;
	private String mGpsLongRef;
	private String mImageLength;
	private String mImageWidth;
	private String mMake;
	private String mModel;
	private int mOrientation;
	private String mWhiteBalance;
	public ImageInfo() {
		mOrientation  = ExifInterface.ORIENTATION_NORMAL;
	}
	public ImageInfo(String filename) {
		try {
			ExifInterface exif = new ExifInterface(filename);
			getExif(exif);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getExif(ExifInterface exif) {
		mDateTime = getTagString(ExifInterface.TAG_DATETIME, exif);
		mFlash = getTagString(ExifInterface.TAG_FLASH, exif);
		mGpsLat = getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
		mGpsLatRef = getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
		mGpsLong = getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
		mGpsLongRef = getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
		mImageLength = getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
		mImageWidth = getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
		mMake = getTagString(ExifInterface.TAG_MAKE, exif);
		mModel = getTagString(ExifInterface.TAG_MODEL, exif);
		mOrientation = Integer.parseInt(getTagString(ExifInterface.TAG_ORIENTATION, exif));
		mWhiteBalance = getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
	}

	private String getTagString(String tag, ExifInterface exif) {
		return exif.getAttribute(tag);
	}

	public String getDateTime() {
		return mDateTime;
	}

	/**
	 * @return the mFlash
	 */
	public String getFlash() {
		return mFlash;
	}

	/**
	 * @return the mGpsLat
	 */
	public String getGpsLat() {
		return mGpsLat;
	}

	public int getOrientation() {
		return mOrientation;
	}

}
