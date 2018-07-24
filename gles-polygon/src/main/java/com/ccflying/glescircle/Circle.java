package com.ccflying.glescircle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccflying.util.Utils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Circle implements GLSurfaceView.Renderer {
    private final static String TAG = "Circle";
    //
    private String vShader = "";
    private String fShader = "";

    // 绘制顺序为逆时针
    private float circleCoords[];
    // GLES程序id
    private int mProgram;
    // 片元着色器填充颜色
    private float color[] = {1.0f, 1.0f, 1.0f, 1.0f};
    // 顶点坐标buffer，将传递给native层
    private FloatBuffer vertexBuffer;
    // 顶点着色器句柄
    private int mGLPositionHandle;
    // 变换矩阵vMatrix成员句柄
    private int mGLMatrixHandle;
    // 顶点着色器句柄
    private int mGLColorHandle;
    // 每个顶点坐标个数
    private static final int COORDS_PER_VERTEX = 3;
    // 顶点偏移量
    private int vertexStride = COORDS_PER_VERTEX * 4;
    // 顶点个数
    private int vertexCount;

    private float[] mMVPMatrix = new float[16];

    public Circle(Context context) {
        vShader = Utils.getAssetsResourceString(context.getResources(),
                "vshader/" + TAG + ".shader");
        fShader = Utils.getAssetsResourceString(context.getResources(),
                "fshader/" + TAG + ".shader");
        //
        circleCoords = initCircleCoords();
        // 顶点个数
        vertexCount = circleCoords.length / COORDS_PER_VERTEX;
    }

    private float[] initCircleCoords() {
        int n = 360; // 份数
        // n + 2 个点
        float[] f = new float[(n + 2) * 3];
        float singleAngle = 360f / n;
        float radius = 0.5f;
        for (int i = 0; i <= n; i++) {
            float angle = i * singleAngle;
            int baseOfx = (i + 1) * 3;
            f[baseOfx + 0] = (float) (radius * Math.cos(angle * Math.PI / 180f)); // x
            f[baseOfx + 1] = (float) (radius * Math.sin(angle * Math.PI / 180f)); // y
            f[baseOfx + 2] = 0; // z
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1);
        //
        mProgram = GLES20.glCreateProgram();
        //
        int vShaderId = Utils.loadShader(GLES20.GL_VERTEX_SHADER, vShader);
        int fShaderId = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fShader);
        // 将顶点着色器、片段着色器加入到程序
        GLES20.glAttachShader(mProgram, vShaderId);
        GLES20.glAttachShader(mProgram, fShaderId);
        // 连接到着色器程序
        GLES20.glLinkProgram(mProgram);
        //
        vertexBuffer = Utils.allocateFloatBuffer(circleCoords);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //
        float radio = width * 1f / height;
        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];
        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -radio, radio, -1, 1, 3, 7);
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, // 相机变换的矩阵, 变换矩阵的起始位置（偏移量）
                0, 0, 6f, // 相机位置
                0, 0, 0, // 观测点位置
                0, 1, 0); // up向量在xyz上的分量
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //
        GLES20.glUseProgram(mProgram);
        // 获取变换矩阵vMatrix成员句柄
        mGLMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        // 指定vMatrix值，count: matrix个数。
        GLES20.glUniformMatrix4fv(mGLMatrixHandle, 1, false, mMVPMatrix, 0);
        // 获取顶点句柄
        mGLPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mGLPositionHandle);
        GLES20.glVertexAttribPointer(mGLPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, vertexBuffer);
        // 获取颜色句柄
        mGLColorHandle = GLES20.glGetUniformLocation(mProgram, "fColor");
        // count: 颜色点个数
        GLES20.glUniform4fv(mGLColorHandle, 1, color, 0);
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
        // 禁用顶点句柄
        GLES20.glDisableVertexAttribArray(mGLPositionHandle);

    }
}

// GLES20.glDrawArrays的第一个参数表示绘制方式，第二个参数表示偏移量，第三个参数表示顶点个数。
// 绘制方式有：
// int GL_POINTS //将传入的顶点坐标作为单独的点绘制
// int GL_LINES //将传入的坐标作为单独线条绘制，ABCDEFG六个顶点，绘制AB、CD、EF三条线
// int GL_LINE_STRIP //将传入的顶点作为折线绘制，ABCD四个顶点，绘制AB、BC、CD三条线
// int GL_LINE_LOOP //将传入的顶点作为闭合折线绘制，ABCD四个顶点，绘制AB、BC、CD、DA四条线
// int GL_TRIANGLES //将传入的顶点作为单独的三角形绘制，ABCDEF绘制ABC,DEF两个三角形
// int GL_TRIANGLE_FAN //将传入的顶点作为扇面绘制，ABCDEF绘制ABC、ACD、ADE、AEF四个三角形
// int GL_TRIANGLE_STRIP //将传入的顶点作为三角条带绘制，ABCDEF绘制ABC,BCD,CDE,DEF四个三角形