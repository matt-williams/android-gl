package com.github.matt.williams.android.gl;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Texture {
    protected final int mTarget;
    protected final int mId;
    protected final float[] mTransformMatrix = new float[16];
    {
        Matrix.setIdentityM(mTransformMatrix, 0);
    }

    public Texture(int target, int wrap) {
        mTarget = target;
        mId = generateTextureId();
        int oldId = pushTexture();
        GLES20.glTexParameteri(mTarget, GLES20.GL_TEXTURE_WRAP_S, wrap);
        Utils.checkErrors("glTexParameteri");
        GLES20.glTexParameteri(mTarget, GLES20.GL_TEXTURE_WRAP_T, wrap);
        Utils.checkErrors("glTexParameteri");
        GLES20.glTexParameteri(mTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        Utils.checkErrors("glTexParameteri");
        GLES20.glTexParameteri(mTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        Utils.checkErrors("glTexParameteri");
        popTexture(oldId);
    }

    public Texture(int target) {
        this(target, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mId != 0) {
                GLES20.glDeleteTextures(1, new int[] {mId}, 0);
                Utils.checkErrors("glDeleteTextures");
            }
        } finally {
            super.finalize();
        }
    }

    public void use(int channel) {
        GLES20.glActiveTexture(channel);
        Utils.checkErrors("glActiveTexture");
        GLES20.glBindTexture(mTarget, mId);
        Utils.checkErrors("glBindTexture");
    }

    public int getId() {
        return mId;
    }

    public float[] getTransformMatrix() {
        return mTransformMatrix;
    }

    protected int pushTexture() {
        return 0;
    }

    protected void popTexture(int oldId) {
    }

    private static int generateTextureId() {
        int[] ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        Utils.checkErrors("glGenTextures");
        return ids[0];
    }
}