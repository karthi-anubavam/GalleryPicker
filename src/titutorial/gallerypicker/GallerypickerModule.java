/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package titutorial.gallerypicker;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.kroll.common.Log;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.widget.Toast;

@Kroll.module(name="Gallerypicker", id="titutorial.gallerypicker")
public class GallerypickerModule extends KrollModule implements TiActivityResultHandler
{

	// Standard Debugging variables
	private static final String TAG = "GallerypickerModule";
	protected int requestCode;
	private KrollFunction successCallback = null;
	private KrollFunction errorCallback = null;
	
	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	@Kroll.constant public static final int MULTIPLE_SELECTION = 200;
	@Kroll.constant public static final int SINGLE_SELECTION = 100;
	Integer selectionType = MULTIPLE_SELECTION;
	Integer limit = 0;
	String cancelButtonText = null;
	String okButtonText = null;
	String titleText = null;
	String errorMessageText = null;
	
	public GallerypickerModule()
	{
		super();
		Utility.loadResourceIds(TiApplication.getInstance());
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	private void sendSuccessEvent(String filepath) {
		if (successCallback != null) {
			HashMap<String, String> event = new HashMap<String, String>();
			event.put("success", "true");
			event.put("filePath", filepath);

			// Fire an event directly to the specified listener (callback)
			successCallback.call(getKrollObject(), event);
		}
	}

	private void sendErrorEvent(String message) {
		if (errorCallback != null) {
			HashMap<String, String> event = new HashMap<String, String>();
			event.put("message", message);

			// Fire an event directly to the specified listener (callback)
			errorCallback.call(getKrollObject(), event);
		}
	}

	@Kroll.method
	public void registerCallbacks(HashMap args) {
		Object callback;

		// Save the callback functions, verifying that they are of the correct type
		if (args.containsKey("success")) {
			callback = args.get("success");
			if (callback instanceof KrollFunction) {
				successCallback = (KrollFunction) callback;
			}
		}

		if (args.containsKey("error")) {
			callback = args.get("error");
			if (callback instanceof KrollFunction) {
				errorCallback = (KrollFunction) callback;
			}
		}
	}
	
	// Methods
	@Kroll.method
	public void openGallery(HashMap args)
	{
		KrollDict options = new KrollDict(args);
		registerCallbacks(args);
		
		Activity activity = this.getActivity();
		TiActivitySupport support = (TiActivitySupport) activity;
		Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
		
		cancelButtonText = (String) options.get("cancelButtonTitle");
		okButtonText = (String) options.get("doneButtonTitle");
		titleText = (String) options.get("title");
		errorMessageText = (String) options.get("errorMessage");
		if(options.containsKeyAndNotNull("limit")){
			limit = (Integer) options.getInt("limit");
		}
		//selectionType = (Integer) options.getInt("selectionType");
		
		cancelButtonText = (cancelButtonText == null ? "Cancel" : cancelButtonText);
		i.putExtra("cancelButtonText", cancelButtonText);

		okButtonText = (okButtonText == null ? "Done" : okButtonText);
		i.putExtra("okButtonText", okButtonText);
		
		titleText = (titleText == null ? "Gallery" : titleText);
		i.putExtra("titleText", titleText);

		errorMessageText = (errorMessageText == null ? "Max limit reached" : errorMessageText);
		i.putExtra("errorMessageText", errorMessageText);
		
		limit = (limit == null ? 0 : limit);
		i.putExtra("limit", limit);
		
		support.launchActivityForResult(i, selectionType, this);
	}
	
	public void onResult(Activity act,int requestCode, int resultCode, Intent data) {
		if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
			String single_path = data.getStringExtra("single_path");
			sendSuccessEvent(single_path);
		} else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
			String[] all_path = data.getStringArrayExtra("all_path");

			ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
			Activity activity = TiApplication.getAppRootOrCurrentActivity();
			//Toast.makeText(activity, "Selected Images: "+all_path.toString(), Toast.LENGTH_SHORT).show();
			String outputString = "";
			for (String string : all_path) {
				CustomGallery item = new CustomGallery();
				item.sdcardPath = string;
				
				if(outputString.length()>0){
					outputString += ","+string;
				} else {
					outputString = string;
				}
				System.out.println("@@## string = "+string);
				dataT.add(item);
			}
			sendSuccessEvent(outputString);
		}
	}


	@Override
	public void onError(Activity arg0, int arg1, Exception e) {
		sendErrorEvent(e.getMessage());
	}
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	@Kroll.method
	public TiBlob decodeBitmapResource(String path, int reqWidth, int reqHeight) {
		
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap opBitmap = BitmapFactory.decodeFile(path, options);
		return TiBlob.blobFromImage(opBitmap);
	}

}
