package com.chocodroid.labyrinthGame;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Imagenes {

	//decodes image and scales it to reduce memory consumption
	public static Drawable recuperaImagen(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=70;

	        //Find the correct scale value. It should be the power of 2.
	        int width_tmp=o.outWidth, height_tmp=o.outHeight;
	        int scale=1;
	        while(true){
	            if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                break;
	            width_tmp/=2;
	            height_tmp/=2;
	            scale*=2;
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        Drawable d =new BitmapDrawable(BitmapFactory.decodeStream(new FileInputStream(f), null, o2));
	        return d;
	    } catch (FileNotFoundException e) {
	    	
	    }
	    return null;
	}

	public static Drawable getAssetImage(Context context, String filename){
		try{
	    AssetManager assets = context.getResources().getAssets();
	    InputStream buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".png")));
	    Bitmap bitmap = BitmapFactory.decodeStream(buffer);
	    return new BitmapDrawable(bitmap);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
