package com.ccf.glesapp.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Utils {

    final static String TAG = "Utils";

    public static int createProgram(String vertex, String fragment) {
        int vertexId = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        if (vertexId == 0) {
            return 0;
        }

        int fragmentId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        if (fragmentId == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();

        if (program != 0) {
            GLES20.glAttachShader(program, vertexId);
            checkGLError("Attach Vertex Shader");
            GLES20.glAttachShader(program, fragmentId);
            checkGLError("Attach Fragment Shader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG,
                        "Could not link program:"
                                + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static int createProgramFromAssets(Resources res, String vertexRes, String fragmentRes) {
        return createProgram(
                getAssetsResourceString(res, vertexRes),
                getAssetsResourceString(res, fragmentRes));
    }

    public static int loadShaderFromAssets(Resources res, int shaderType, String resName) {
        return loadShader(shaderType, getAssetsResourceString(res, resName));
    }

    public static int loadShader(int type, String shaderCode) {
        // 根据type创建顶点着色器或者片元着色器
        int shader = GLES20.glCreateShader(type);
        // 将资源加入到着色器中并编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int compiled[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader:" + type);
            Log.e(TAG, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static FloatBuffer allocateFloatBuffer(float[] input) {
        // float占4个字节，所以是length * 4，
        ByteBuffer floatBB = ByteBuffer.allocateDirect(input.length * 4);
        floatBB.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = floatBB.asFloatBuffer();
        floatBuffer.put(input);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static ShortBuffer allocateShortBuffer(short[] input) {
        // short占2个字节，所以是length * 2，
        ByteBuffer floatBB = ByteBuffer.allocateDirect(input.length * 2);
        floatBB.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = floatBB.asShortBuffer();
        shortBuffer.put(input);
        shortBuffer.position(0);
        return shortBuffer;
    }

    public static void checkGLError(String op) {
    }

    // 通过路径加载Assets中的文本内容
    public static String getAssetsResourceString(Resources mRes, String path) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            Log.e(TAG, "getAssetsResourceString: " + e);
            return null;
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }

    /**
     * 创建TextureId
     *
     * @param mBitmap
     * @return
     */
    public static int createTexture(Bitmap mBitmap) {
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            // 生成纹理(GLsizei n, textures, offset)
            GLES20.glGenTextures(1, texture, 0);
            // 生成纹理(target, texture)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // 设置环绕方向S，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            // 设置环绕方向T，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // 根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            return texture[0];
        }
        Log.e(TAG, "createTexture: " + texture[0]);
        return 0;
    }

    /**
     * 创建Stream类型的扩展纹理
     *
     * @return
     */
    public static int createStreamTexture() {
        int[] texture = new int[1];
        // 生成纹理
        GLES20.glGenTextures(1, texture, 0);
        // 绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        //
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        //
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //
        return texture[0];
        //
        // 由于我们创建的是扩展纹理，所以绑定的时候我们也需要绑定到扩展纹理上才可以正常使用，
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])。
    }
    // 直接传入buffer数据。
    // GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
    // height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

    public static void saveBitmapFromByteBuffer(final ByteBuffer data,
                                                final int bmpWidth,
                                                final int bmpHeight,
                                                final String imgPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(data);
                saveBitmap(bitmap, imgPath);
                data.clear();
            }
        }).start();
    }

    // 保存图片
    public static boolean saveBitmap(final Bitmap b, final String mImgPath) {
        String path = mImgPath.substring(0, mImgPath.lastIndexOf("/") + 1);
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName = path + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
