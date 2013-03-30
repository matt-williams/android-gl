package com.github.matt.williams.android.gl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLES20;
import android.util.SparseArray;

public class Utils {
    private Utils() {}
    
    public static final Options BITMAP_OPTIONS = new Options();
    static {
        BITMAP_OPTIONS.inScaled = false;
        BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.ARGB_8888;
    };
    
   private final static SparseArray<String> GL_ERRORS = new SparseArray<String>();
    static {
        GL_ERRORS.put(GLES20.GL_INVALID_ENUM, "GL_INVALID_ENUM");
        GL_ERRORS.put(GLES20.GL_INVALID_FRAMEBUFFER_OPERATION, "GL_INVALID_FRAMEBUFFER_OPERATION");
        GL_ERRORS.put(GLES20.GL_INVALID_OPERATION, "GL_INVALID_OPERATION");
        GL_ERRORS.put(GLES20.GL_INVALID_VALUE, "GL_INVALID_VALUE");
    }
    
    public static void checkErrors(String name) {
        int error = GLES20.glGetError();
        if (error != 0) {
            String errorString = GL_ERRORS.get(error);
            throw new IllegalStateException(name + " raised " + ((errorString != null) ? errorString : ("error " + error)));
        }
    }
}
