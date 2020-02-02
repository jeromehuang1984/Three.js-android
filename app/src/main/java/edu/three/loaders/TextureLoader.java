package edu.three.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import edu.three.textures.Texture;

import static edu.three.constant.Constants.RGBAFormat;
import static edu.three.constant.Constants.RGBFormat;

public class TextureLoader {
    public String TAG = getClass().getSimpleName();

    public Texture loadTexture(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            Log.e(TAG, "resource cannot be decoded.");
            return null;
        }
        String type = options.outMimeType;
        boolean isJPEG = "image/jpeg".equals(type);
        Log.i(TAG, "image mime: " + type);
        Texture texture = new Texture();
        texture.setImage(bitmap);
        texture.format = isJPEG ? RGBFormat : RGBAFormat;
        return texture;
    }
}
