package com.github.matt.williams.android.gl;

import android.opengl.Matrix;
import android.util.FloatMath;

public class Projection implements Cloneable {
    private static final float NEAR = 0.1f;
    private static final float FAR = 100.0f;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mInverseProjectionMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mInverseViewMatrix = new float[16];

    public Projection() {
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mInverseProjectionMatrix, 0);
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mInverseViewMatrix, 0);
    }

    public Projection(float[] vertices, int aOff, int bOff, int cOff, int dOff, float rotation) {
        // Construct the rotation matrix.
        float[] temp = new float[16];
        extractUnitVector(temp, 0, vertices, aOff, bOff, cOff, dOff);
        extractUnitVector(temp, 4, vertices, aOff, cOff, bOff, dOff);
        crossProduct(temp, 8, temp, 0, temp, 4);
        normalize(temp, 8);
        temp[15] = 1;
        Matrix.transposeM(mRotationMatrix, 0, temp, 0);

        // TODO: Figure out why Y is offset 4 and X is offset 0 - shouldn't it be the other way round?
        float aX = dotProduct(vertices, aOff, temp, 4);
        float bX = dotProduct(vertices, bOff, temp, 4);
        float cX = dotProduct(vertices, cOff, temp, 4);
        float dX = dotProduct(vertices, dOff, temp, 4);
        float aY = dotProduct(vertices, aOff, temp, 0);
        float bY = dotProduct(vertices, bOff, temp, 0);
        float cY = dotProduct(vertices, cOff, temp, 0);
        float dY = dotProduct(vertices, dOff, temp, 0);
        float z = dotProduct(vertices, aOff, temp, 8);
        float left = ((aX < cX) ? aX : cX) * NEAR / z;
        float right = ((bX > dX) ? bX : dX) * NEAR / z;
        float bottom = ((aY < bY) ? aY : bY) * NEAR / z;
        float top = ((cY > dY) ? cY : dY) * NEAR / z;

        setProjection(left, right, bottom, top, NEAR, FAR, rotation);
    }

    public void setProjection(float horizViewAngle, float vertViewAngle, float rotation) {
        // Viewing angles are full angles (not half angles) - to get the left-right distance, we need to halve the angle (so that we have 2 right-angled triangles), take the tangent and then double the result.
        float leftRight = (float)Math.tan(horizViewAngle / 2 * Math.PI / 180) * 2 * NEAR;
        float bottomTop = (float)Math.tan(vertViewAngle / 2 * Math.PI / 180) * 2 * NEAR;
        setProjection(-leftRight / 2, leftRight / 2, -bottomTop / 2, bottomTop / 2, NEAR, FAR, rotation);
    }

    public void setProjection(float left, float right, float bottom, float top, float near, float far, float rotation) {
        float[] projectionMatrix = new float[16];
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
        // We rotate the projection matrix about the z axis (into the display) to switch between portrait and landscape mode.
        Matrix.rotateM(projectionMatrix, 0, rotation, 0, 0, 1);
        setProjectionMatrix(projectionMatrix);
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        System.arraycopy(projectionMatrix, 0, mProjectionMatrix, 0, 16);
        Matrix.invertM(mInverseProjectionMatrix, 0, mProjectionMatrix, 0);
        multiplyMatrices();
    }

    public void setRotationMatrix(float[] rotationMatrix) {
        System.arraycopy(rotationMatrix, 0, mRotationMatrix, 0, 16);
        multiplyMatrices();
    }

    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }

    public float[] getInverseProjectionMatrix() {
        return mInverseProjectionMatrix;
    }

    public float[] getRotationMatrix() {
        return mRotationMatrix;
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    public float[] inverseProject(float x, float y) {
        float[] result = new float[4];
        Matrix.multiplyMV(result, 0, mInverseProjectionMatrix, 0, new float[] {x, y, NEAR, 1f}, 0);
        return result;
    }

    public float[] view(float x, float y, float z) {
        float[] result = new float[4];
        Matrix.multiplyMV(result, 0, mViewMatrix, 0, new float[] {x, y, z, 1f}, 0);
        return new float[] {result[0] / result[3], result[1] / result[3]};
    }

    public float[] inverseView(float x, float y) {
        float[] result = new float[4];
        Matrix.multiplyMV(result, 0, mInverseViewMatrix, 0, new float[] {x, y, NEAR, 1f}, 0);
        return result;
    }

    public void inverseView(float[] res, int resOff, float[] v, int vOff, int num) {
        float[] temp = new float[4];
        for (int ii = 0; ii < num; ii++) {
            Matrix.multiplyMV(temp, 0, mInverseViewMatrix, 0, new float[] {v[vOff + ii * 2], v[vOff + ii * 2 + 1], NEAR, 1f}, 0);
            res[ii * 3] = temp[0] / temp[3];
            res[ii * 3 + 1] = temp[1] / temp[3];
            res[ii * 3 + 2] = temp[2] / temp[3];
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private void multiplyMatrices() {
        Matrix.multiplyMM(mViewMatrix, 0, mProjectionMatrix, 0, mRotationMatrix, 0);
        Matrix.invertM(mInverseViewMatrix, 0, mViewMatrix, 0);
    }

    // Extracts a unit vector from the average of v1a->v1b and v2a->v2b.
    private void extractUnitVector(float[] res, int resOff, float[] v, int v1aOff, int v1bOff, int v2aOff, int v2bOff) {
        for (int ii = 0; ii < 3; ii++) {
            res[resOff + ii] = v[v1bOff + ii] - v[v1aOff + ii] + v[v2bOff + ii] - v[v2aOff + ii];
        }
        normalize(res, resOff);
    }

    private void crossProduct(float[] res, int resOff, float[] v1, int v1Off, float[] v2, int v2Off) {
        res[resOff] = v1[v1Off + 1] * v2[v2Off + 2] - v1[v1Off + 2] * v2[v2Off + 1];
        res[resOff + 1] = v1[v1Off + 2] * v2[v2Off] - v1[v1Off] * v2[v2Off + 2];
        res[resOff + 2] = v1[v1Off] * v2[v2Off + 1] - v1[v1Off + 1] * v2[v2Off];
    }

    private float dotProduct(float[] v1, int v1Off, float[] v2, int v2Off) {
        return v1[v1Off] * v2[v2Off] + v1[v1Off + 1] * v2[v2Off + 1] + v1[v1Off + 2] * v2[v2Off + 2];
    }

    private void normalize(float[] v, int vOff) {
        float lenSquared = 0;
        for (int ii = 0; ii < 3; ii++) {
            lenSquared += v[vOff + ii] * v[vOff + ii];
        }
        float len = FloatMath.sqrt(lenSquared);
        if (len > 0) {
            for (int ii = 0; ii < 3; ii++) {
                v[vOff + ii] /= len;
            }
        }
    }
}
