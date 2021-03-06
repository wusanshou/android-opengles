/*
 */
package com.ccf.glesapp.util;

import android.opengl.Matrix;

/**
 * Description:
 */
public final class Gl2Utils {

    public static final String TAG = "Gl2Utils";

    public static final int TYPE_FITXY = 0;
    public static final int TYPE_CENTERCROP = 1;
    public static final int TYPE_CENTERINSIDE = 2;
    public static final int TYPE_FITSTART = 3;
    public static final int TYPE_FITEND = 4;

    private Gl2Utils() {
    }

    public static void getShowMatrix(float[] matrix, int imgWidth,
                                     int imgHeight, int viewWidth, int viewHeight) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            float viewWHScale = (float) viewWidth / viewHeight;
            float imgWHScale = (float) imgWidth / imgHeight;
            float[] mProjectMatrix = new float[16];
            float[] mViewMatrix = new float[16];
            // 屏幕宽大于屏幕高
            if (viewWidth > viewHeight) { // -> sWidthHeight 大于 1
                if (imgWHScale > viewWHScale) {
                    Matrix.orthoM(mProjectMatrix, 0, -viewWHScale * imgWHScale,
                            viewWHScale * imgWHScale, -1, 1, 3, 5);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -viewWHScale / imgWHScale,
                            viewWHScale / imgWHScale, -1, 1, 3, 5);
                }
            } else {
                if (imgWHScale > viewWHScale) {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1,
                            -1 / viewWHScale * imgWHScale, 1 / viewWHScale * imgWHScale, 3, 5);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -imgWHScale / viewWHScale,
                            imgWHScale / viewWHScale, 3, 5);
                }
            }
            // 设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0,
                    0, 0, 5.0f,
                    0f, 0f, 0f,
                    0f, 1.0f, 0.0f);
            // 计算变换矩阵
            Matrix.multiplyMM(matrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        }
    }

    public static void getMatrix(float[] matrix, int type, int imgWidth,
                                 int imgHeight, int viewWidth, int viewHeight) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            float[] projection = new float[16];
            float[] camera = new float[16];
            if (type == TYPE_FITXY) {
                Matrix.orthoM(projection, 0, -1, 1, -1, 1, 1, 3);
                Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
                Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0);
            }
            float sWhView = (float) viewWidth / viewHeight;
            float sWhImg = (float) imgWidth / imgHeight;
            if (sWhImg > sWhView) {
                switch (type) {
                    case TYPE_CENTERCROP:
                        Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView
                                / sWhImg, -1, 1, 1, 3);
                        break;
                    case TYPE_CENTERINSIDE:
                        Matrix.orthoM(projection, 0, -1, 1, -sWhImg / sWhView,
                                sWhImg / sWhView, 1, 3);
                        break;
                    case TYPE_FITSTART:
                        Matrix.orthoM(projection, 0, -1, 1, 1 - 2 * sWhImg
                                / sWhView, 1, 1, 3);
                        break;
                    case TYPE_FITEND:
                        Matrix.orthoM(projection, 0, -1, 1, -1, 2 * sWhImg
                                / sWhView - 1, 1, 3);
                        break;
                }
            } else {
                switch (type) {
                    case TYPE_CENTERCROP:
                        Matrix.orthoM(projection, 0, -1, 1, -sWhImg / sWhView,
                                sWhImg / sWhView, 1, 3);
                        break;
                    case TYPE_CENTERINSIDE:
                        Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView
                                / sWhImg, -1, 1, 1, 3);
                        break;
                    case TYPE_FITSTART:
                        Matrix.orthoM(projection, 0, -1, 2 * sWhView / sWhImg - 1,
                                -1, 1, 1, 3);
                        break;
                    case TYPE_FITEND:
                        Matrix.orthoM(projection, 0, 1 - 2 * sWhView / sWhImg, 1,
                                -1, 1, 1, 3);
                        break;
                }
            }
            Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0);
        }
    }

    public static void getCenterInsideMatrix(float[] matrix, int imgWidth,
                                             int imgHeight, int viewWidth, int viewHeight) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            float sWhView = (float) viewWidth / viewHeight;
            float sWhImg = (float) imgWidth / imgHeight;
            float[] projection = new float[16];
            float[] camera = new float[16];
            if (sWhImg > sWhView) {
                Matrix.orthoM(projection, 0, -1, 1, -sWhImg / sWhView, sWhImg
                        / sWhView, 1, 3);
            } else {
                Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView
                        / sWhImg, -1, 1, 1, 3);
            }
            Matrix.setLookAtM(camera, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0);
        }
    }

    public static float[] rotate(float[] m, float angle) {
        Matrix.rotateM(m, 0, angle, 0, 0, 1);
        return m;
    }

    public static float[] scale(float[] m, float scaleX, float scaleY) {
        Matrix.scaleM(m, 0, scaleX, scaleY, 1);
        return m;
    }

    public static float[] flip(float[] m, boolean x, boolean y) {
        if (x || y) {
            Matrix.scaleM(m, 0, x ? -1 : 1, y ? -1 : 1, 1);
        }
        return m;
    }

    public static float[] getOriginalMatrix() {
        return new float[]{
                1, 0, 0, 0, //
                0, 1, 0, 0, //
                0, 0, 1, 0, //
                0, 0, 0, 1};
    }

}
