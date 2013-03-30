package com.github.matt.williams.android.gl;

import java.nio.ByteBuffer;

import android.opengl.GLES20;

public class TargetTexture extends BasicTexture {
    private final int mFramebufferId;
    private int mWidth;
    private int mHeight;
    private boolean mResizePending;
    private boolean mFramebufferResizePending;

    public TargetTexture(int width, int height, int wrap) {
        super(GLES20.GL_TEXTURE_2D, wrap);
        mFramebufferId = generateFramebufferId();
        int oldId = pushFramebuffer();
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, mTarget, mId, 0);
        popFramebuffer(oldId);
        setSize(width, height);
    }

    public TargetTexture(int width, int height) {
        this(width, height, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mFramebufferId != 0) {
                GLES20.glDeleteTextures(1, new int[] {mFramebufferId}, 0);
            }
        } finally {
            super.finalize();
        }
    }

    public void setSize(int width, int height) {
        if ((mWidth != width) || (mHeight != height)) {
            mWidth = width;
            mHeight = height;
            mResizePending = true;
            mFramebufferResizePending = true;
        }
    }

    public void renderTo() {
        if (mFramebufferResizePending) {
            int oldId = pushTexture();
            GLES20.glTexImage2D(mTarget, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.allocate(mWidth * mHeight * 4));
            Utils.checkErrors("glTexImage2D");
            popTexture(oldId);
            mFramebufferResizePending = false;
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
        Utils.checkErrors("glBindFramebuffer");
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) returned " + status);
        }
        GLES20.glViewport(0, 0, mWidth, mHeight);
        Utils.checkErrors("glViewport");
    }

    public void setData(byte[] data) {
        int oldId = pushTexture();
        if (mResizePending) {
            GLES20.glTexImage2D(mTarget, 0, GLES20.GL_LUMINANCE, mWidth, mHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data, 0, mWidth * mHeight));
            Utils.checkErrors("glTexImage2D");
            mResizePending = false;
        } else {
            GLES20.glTexSubImage2D(mTarget, 0, 0, 0, mWidth, mHeight, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data, 0, mWidth * mHeight));
            Utils.checkErrors("glTexSubImage2D");
        }
        popTexture(oldId);
    }

    public int getFramebufferId() {
        return mFramebufferId;
    }

    private static int generateFramebufferId() {
        int[] ids = new int[1];
        GLES20.glGenFramebuffers(1, ids, 0);
        Utils.checkErrors("glFramebuffers");
        return ids[0];
    }

    private int pushFramebuffer() {
        int[] oldIds = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, oldIds, 0);
        Utils.checkErrors("glGetIntegerv");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
        Utils.checkErrors("glBindFramebuffer");
        return oldIds[0];
    }

    private void popFramebuffer(int oldId) {
        // Revert to the previous framebuffer, unless it's 0 (in which case nothing was previously bound).
        if (oldId != 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, oldId);
            Utils.checkErrors("glBindFramebuffer");
        }
    }
}
