package com.xfeng.video;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by lixianfeng on 2017/6/18.
 */

public class PlayRender implements GLSurfaceView.Renderer, PlayWithOpenGLES.RenderUpdateCallback {

    private PlayWithOpenGLES playWithOpenGLES;
    private Context mContext;
    private WeakReference<PlayGLSurfaceView> glSurfaceView;

    public PlayRender(Context context, PlayGLSurfaceView glView){
        mContext = context;
        glSurfaceView = new WeakReference<PlayGLSurfaceView>(glView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        playWithOpenGLES = new PlayWithOpenGLES(mContext);
        playWithOpenGLES.setRenderUpdateCallback(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        playWithOpenGLES.playVideo(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        playWithOpenGLES.draw();
    }

    @Override
    public void needDraw() {
        PlayGLSurfaceView glView = glSurfaceView.get();
        if(glView != null){
            glView.requestRender();
        }
    }

    public void pause(){
        playWithOpenGLES.pause();
    }
    public void destory(){
        playWithOpenGLES.destory();
    }
}
