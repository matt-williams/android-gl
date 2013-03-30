package com.github.matt.williams.android.gl;

import android.opengl.GLES20;

public class VertexShader extends Shader {
    public VertexShader(String source) {
        super(GLES20.GL_VERTEX_SHADER, source);
    }
}