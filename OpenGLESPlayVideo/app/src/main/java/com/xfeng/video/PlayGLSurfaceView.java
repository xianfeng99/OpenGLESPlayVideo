package com.xfeng.video;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by lixianfeng on 2017/6/18.
 */

public class PlayGLSurfaceView extends GLSurfaceView {
    private PlayRender render;

    public PlayGLSurfaceView(Context context) {
        super(context);
        initRender();
    }

    public PlayGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRender();
    }

    private void initRender(){
        render = new PlayRender(getContext(), this);
        setEGLContextClientVersion(2);
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onPause() {
        render.pause();
        super.onPause();
    }

    public void onDestory(){
        render.destory();
    }

}
