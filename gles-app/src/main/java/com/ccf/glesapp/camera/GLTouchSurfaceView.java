package com.ccf.glesapp.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

// com.ccf.glesapp.camera.GLTouchSurfaceView
public class GLTouchSurfaceView extends GLSurfaceView {

    private float touchX, touchY;

    final String TAG = "GLTouchSurfaceView";

    public GLTouchSurfaceView(Context context) {
        super(context);
    }

    public GLTouchSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchY = touchX = 0;
        } else {
            touchX = event.getX();
            touchY = event.getY();
        }
        return true;
    }

    private int width, height;

    public float[] getTouchPosition() {
        if (touchY == 0 || touchX == 0) {
            return new float[0];
        }
        if (width == 0 || height == 0) {
            width = getWidth();
            height = getHeight();
        }
        if (width == 0) {
            return new float[0];
        }
        float fx = touchX * 2 / width - 1;
        float fy = 1 - touchY * 2 / height;
        return new float[]{fx, fy};
    }
}
