/*
 *
 * GrayFilter.java
 *
 * Created by Wuwang on 2016/12/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.stream.camerafilter;

import android.content.res.Resources;

import com.ccf.glesapp.stream.StreamFilter;

/**
 * Description:
 */
public class LightFilter extends StreamFilter {

    public LightFilter(Resources mRes, boolean back) {
        super(mRes, back);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/camera/CameraPreviewLight.shader",
                "fshader/camera/CameraPreviewLight.shader");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }
}
