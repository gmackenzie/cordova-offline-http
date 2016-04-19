package com.commontime.mdesign.plugins.thumbnail;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Base64;

import com.commontime.mdesign.plugins.base.Files;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Thumbnail extends CordovaPlugin {

	@Override
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if ("getThumbnail".equals(action)) {
			cordova.getThreadPool().execute(new Runnable() {
				public void run() {
					try {
						getThumbnail(args.getString(0), args.getInt(1), args.getInt(2), args.getInt(3), callbackContext);
					} catch (Exception e) {
						e.printStackTrace();
						callbackContext.error(e.getMessage());
					}
				}
			});
			return true;
		}
		return false;
	}
	
	private void getThumbnail(String path, int maxWidth, int maxHeight, int quality, CallbackContext callbackContext) {
		
		File rootDir = Files.getRootDir(cordova.getActivity());
		String fullPath = new File(rootDir, path).getAbsolutePath();
				
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fullPath, options);
				
		final int width = options.outWidth; 
		final int height = options.outHeight;
		float ratio = 1;
		if (width > maxWidth || height > maxHeight) {
			final float widthRatio = (float)maxWidth/(float)width;
			final float heightRatio = (float)maxHeight/(float)height;
			ratio = Math.min(widthRatio, heightRatio);
		}

		options.inSampleSize = (int) (1/ratio);		
		options.inJustDecodeBounds = false;
		Bitmap original = BitmapFactory.decodeFile(fullPath, options);
		
		if (original == null) {
			callbackContext.error("thumbnail error: unable to open image at " + fullPath);
			return;
		}
		
		int thumbWidth = Math.round(width * ratio);
		int thumbHeight = Math.round(height * ratio);
		Bitmap thumb = Bitmap.createScaledBitmap(original, thumbWidth, thumbHeight, true);
		if (thumb != original){
			original.recycle();
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		thumb.compress(CompressFormat.JPEG, quality, output);
		thumb.recycle();
		
		String base64 = Base64.encodeToString(output.toByteArray(), Base64.DEFAULT);
		try {
			output.close();
		} catch (IOException e) {			
			callbackContext.error("thumbnail error: error closing output stream");
			return;
		}

		callbackContext.success("data:image/jpeg;base64," + base64);		
	}
}
