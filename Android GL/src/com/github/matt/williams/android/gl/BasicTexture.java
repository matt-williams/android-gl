package com.github.matt.williams.android.gl;

import android.opengl.GLES20;


public class BasicTexture extends Texture {
    public BasicTexture(int target, int wrap) {
        super(target, wrap);
    }

    public BasicTexture(int target) {
        super(target);
    }

    @Override
    protected int pushTexture() {
        int[] oldIds = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, oldIds, 0);
        Utils.checkErrors("glGetIntegerv");
        GLES20.glBindTexture(mTarget, mId);
        Utils.checkErrors("glBindTexture");
        return oldIds[0];
    }

    @Override
    protected void popTexture(int oldId) {
        if (oldId != 0) {
            GLES20.glBindTexture(mTarget, oldId);
            Utils.checkErrors("glBindTexture");
        }
    }
}
