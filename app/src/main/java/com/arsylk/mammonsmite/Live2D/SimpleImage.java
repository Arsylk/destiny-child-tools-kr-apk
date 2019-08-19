package com.arsylk.mammonsmite.Live2D;

import com.arsylk.mammonsmite.utils.Utils;
import jp.live2d.android.UtOpenGL;

import javax.microedition.khronos.opengles.GL10;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class SimpleImage {
    static ShortBuffer drawImageBufferIndex = null;
    static FloatBuffer drawImageBufferUv = null;
    static FloatBuffer drawImageBufferVer = null;

    private InputStream textureStream = null;

    private boolean isTextured = false;
    private int texture;

    private float imageBottom;
    private float imageLeft;
    private float imageRight;
    private float imageTop;
    
    private float uvBottom;
    private float uvLeft;
    private float uvRight;
    private float uvTop;

    public SimpleImage(InputStream in) {
        textureStream = in;
        uvLeft = 0.0f;
        uvRight = 1.0f;
        uvBottom = 0.0f;
        uvTop = 1.0f;
        imageLeft = -1.0f;
        imageRight = 1.0f;
        imageBottom = -1.0f;
        imageTop = 1.0f;
    }

    public void load(GL10 gl) {
        try{
            texture = UtOpenGL.loadTexture(gl, textureStream, true);
            textureStream.close();
            textureStream = null;
        }catch(Exception e) {
            e.printStackTrace();
        }
        isTextured = true;
    }

    private void update() {
        float[] ver = new float[]{imageLeft, imageTop, imageRight, imageTop, imageRight, imageBottom, imageLeft, imageBottom};
        short[] index = new short[]{0, 1, 2, 0, 2, 3};
        drawImageBufferUv = Utils.setupFloatBuffer(drawImageBufferUv, new float[]{uvLeft, uvBottom, uvRight, uvBottom, uvRight, uvTop, uvLeft, uvTop});
        drawImageBufferVer = Utils.setupFloatBuffer(drawImageBufferVer, ver);
        drawImageBufferIndex = Utils.setupShortBuffer(drawImageBufferIndex, index);
    }

    public void draw(GL10 gl) {
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, drawImageBufferUv);
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, drawImageBufferVer);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, drawImageBufferIndex);
    }

    public void setDrawRect(float left, float right, float top, float bottom) {
        imageLeft = left;
        imageRight = right;
        imageBottom = bottom;
        imageTop = top;
        update();
    }

    public void setUVRect(float left, float right, float top, float bottom) {
        uvLeft = left;
        uvRight = right;
        uvBottom = bottom;
        uvTop = top;
        update();
    }

    public boolean isTextured() {
        return isTextured;
    }

}
