package com.github.matt.williams.android.gl;

import java.io.IOException;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.opengl.GLES11Ext;


public class CameraTexture extends Texture implements OnFrameAvailableListener {

    private final SurfaceTexture mSurfaceTexture;
    // We need to count the number of frames available, not just record a flag.  Otherwise SurfaceView processing seems to lock up.
    private volatile int mFramesAvailable = 0;

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
        mFramesAvailable++;
    }

    @Override
    public void use(int channel) {
        while (mFramesAvailable > 0) {
            mSurfaceTexture.updateTexImage();
            mFramesAvailable--;
        }
        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        super.use(channel);
    }

}
