package com.github.matt.williams.android.gl;

import android.graphics.Rect;
import android.opengl.GLES20;

public class ScreenTarget {
    private int mX;
    private int mY;
    private int mWidth;
    private int mHeight;

    public ScreenTarget(int x, int y, int width, int height) {
        set(x, y, width, height);
    }

    public ScreenTarget(int width, int height) {
        this(0, 0, width, height);
    }

    public ScreenTarget() {
        this(0, 0);
    }

    public void set(int x, int y, int width, int height) {
        mX = x;
        mY = y;
        mWidth = width;
        mHeight = height;
    }

    public void set(int width, int height) {
        set(0, 0, width, height);
    }

    public void set(int width, int height, double aspect) {
        if (width / height < aspect) {
            int cameraWidth = (int)(aspect * height);
            set((width - cameraWidth) / 2, 0, cameraWidth, height);
        } else {
            int cameraHeight = (int)(width / aspect);
            set(0, (height - cameraHeight) / 2, width, cameraHeight);
        }
    }

    public void renderTo() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        Utils.checkErrors("glBindFramebuffer");
        GLES20.glViewport(mX, mY, mWidth, mHeight);
        Utils.checkErrors("glViewport");
    }

    public float toGlX(float screenX) {
        return (screenX - mX) * 2 / mWidth - 1;
    }

    public float toGlY(float screenY) {
        return 1 - (screenY - mY) * 2 / mHeight;
    }

    public Rect getRect() {
        return new Rect(mX, mY, mX + mWidth, mY + mHeight);
    }
}
