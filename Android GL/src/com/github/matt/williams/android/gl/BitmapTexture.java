package com.github.matt.williams.android.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class BitmapTexture extends BasicTexture {
    private final int mWidth;
    private final int mHeight;

    public BitmapTexture(Bitmap bitmap, int wrap) {
        super(GLES20.GL_TEXTURE_2D, wrap);
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        int oldId = pushTexture();
        GLUtils.texImage2D(mTarget, 0, bitmap, 0);
        Utils.checkErrors("texImage2D");
        bitmap.recycle();
        popTexture(oldId);
    }

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, GLES20.GL_CLAMP_TO_EDGE);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
