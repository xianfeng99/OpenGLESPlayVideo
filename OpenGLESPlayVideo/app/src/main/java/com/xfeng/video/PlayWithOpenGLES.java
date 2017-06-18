package com.xfeng.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Environment;
import android.view.Surface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by lixianfeng on 2017/6/18.
 */

public class PlayWithOpenGLES implements SurfaceTexture.OnFrameAvailableListener {


    public static final String videoPath = Environment.getExternalStorageDirectory().getPath()+"/video.mp4";

    private int mProgram;
    private int mMatrixHandle;
    private int maPositionHandle;
    private int maTexPositionHandle;
    private int mTextureHandle;
    private int mTextureId;

    private SurfaceTexture mSurfaceTexture;
    private float[] mSrfMatrix = new float[16];
    private boolean updateSurface = false;

    private int vCount;//顶点数量
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private ShortBuffer mOrderBuffer;

    private WeakReference<Context> mContext;
    private MediaPlayer mediaPlayer;

    private int screenHeight;
    private int screenWidth;
    private int startX;
    private int startY;

    private int width;
    private int height;

    public PlayWithOpenGLES(Context context){
        mContext = new WeakReference<Context>(context);
        initGL();
        initVertexData();
    }

    private void initGL(){
        mProgram = ShaderUtil.createProgram(ShaderUtil.vertexShader, ShaderUtil.fragShader);
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSurfaceTextureMatrix");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maTexPositionHandle = GLES20.glGetAttribLocation(mProgram, "aTexturePosition");
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "texture");

        int[] tid = new int[1];
        GLES20.glGenTextures(1, tid, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        ShaderUtil.checkGLError("Gen surface texture error");
        mTextureId = tid[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    private void initVertexData(){
        //顶点坐标
        float[] vertexPosition = {
                -1, 1,
                -1, -1,
                1, -1,
                1, 1
        };
        //纹理坐标
        float[] texturePosition = {
                0, 1, 0, 1,
                0, 0, 0, 1,
                1, 0, 0, 1,
                1, 1, 0, 1
        };
        short[] order = {0, 1, 2, 0, 2, 3};
        vCount = order.length;
        ByteBuffer vp = ByteBuffer.allocateDirect(vertexPosition.length * 4);
        vp.order(ByteOrder.nativeOrder());
        mVertexBuffer = vp.asFloatBuffer();
        mVertexBuffer.put(vertexPosition);
        mVertexBuffer.position(0);

        ByteBuffer tp = ByteBuffer.allocateDirect(texturePosition.length * 4);
        tp.order(ByteOrder.nativeOrder());
        mTextureBuffer = tp.asFloatBuffer();
        mTextureBuffer.put(texturePosition);
        mTextureBuffer.position(0);

        ByteBuffer ob = ByteBuffer.allocateDirect(order.length * 2);
        ob.order(ByteOrder.nativeOrder());
        mOrderBuffer = ob.asShortBuffer();
        mOrderBuffer.put(order);
        mOrderBuffer.position(0);
    }

    public void draw(){
        synchronized (this){
            if(updateSurface){
                updateSurface = false;
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mSrfMatrix);
            }
        }
        GLES20.glViewport(startX, startY, width, height);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        GLES20.glVertexAttribPointer(maPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        GLES20.glVertexAttribPointer(maTexPositionHandle, 4, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(maTexPositionHandle);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mTextureHandle, 0);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mSrfMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vCount, GLES20.GL_UNSIGNED_SHORT, mOrderBuffer);
    }


    public void playVideo(final int w, final int h){
        screenWidth = w;
        screenHeight = h;
        if(mediaPlayer == null){
            Context context = mContext.get();
            if(context == null){
                return;
            }
            mediaPlayer = MediaPlayer.create(context, R.raw.video);
//            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    width = mediaPlayer.getVideoWidth();
                    height = mediaPlayer.getVideoHeight();

                    float sw = (float)width / screenWidth;
                    float sh = (float)height / screenHeight;
                    if(sw > sh){
                        width = (int) (width / sw);
                        height = (int) (height / sw);
                    }else{
                        width = (int) (width / sh);
                        height = (int) (height / sh);
                    }
                    startX = (screenWidth - width) / 2;
                    startY = (screenHeight - height) / 2;
                }
            });

            Surface surface = new Surface(mSurfaceTexture);
            mediaPlayer.setSurface(surface);
            surface.release();
            try {
//                mediaPlayer.setDataSource(videoPath);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else {
            mediaPlayer.start();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this){
            updateSurface = true;
            if(mCallback != null){
                mCallback.needDraw();
            }
        }
    }

    private RenderUpdateCallback mCallback;
    public void setRenderUpdateCallback(RenderUpdateCallback callback){
        mCallback = callback;
    }
    public interface RenderUpdateCallback{
        void needDraw();
    }

    public void pause(){
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    public void destory(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

}
