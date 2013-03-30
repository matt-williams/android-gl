package com.github.matt.williams.android.gl;

import java.io.IOException;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.opengl.GLES11Ext;


public class CameraTexture extends Texture implements OnFrameAvailableListener {

    private final SurfaceTexture mSurfaceTexture;
    private volatile boolean mFrameAvailable = false;

    public CameraTexture(Camera camera) {
        super(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        mSurfaceTexture = new SurfaceTexture(getId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        try {
            camera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onFrameAvailable(SurfaceTexture arg0) {
        mFrameAvailable = true;
    }

    @Override
    public void use(int channel) {
        if (mFrameAvailable) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTransformMatrix);
            mFrameAvailable = false;
        }
        super.use(channel);
    }

}
