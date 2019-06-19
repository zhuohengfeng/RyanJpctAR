/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/
package com.ryan.ryanjpctar;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.ryan.ryanjpctar.vuforia.SampleApplicationSession;
import com.ryan.ryanjpctar.vuforia.utils.LoadingDialogHandler;

import com.ryan.ryanjpctar.vuforia.utils.SampleMath;
import com.vuforia.CameraCalibration;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;

import java.util.ArrayList;
import java.util.Collections;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * jpec加载obj格式模型
 *
 * @author Admin
 */
public class ImageTargetRendererObj extends org.rajawali3d.renderer.Renderer {
    private SampleApplicationSession vuforiaAppSession;
    private ImageTargetsActivity mActivity;
    private Renderer mRenderer;
    boolean mIsActive = false;
    /**
     * jpct中的世界
     */
//    private World world;
//    private Camera cam;
//    private FrameBuffer fb;
    private float[] modelViewMat;
    private float fov;
    private float fovy;
    /**
     * jpct中的灯光
     */
//    private Light sun;
    /**
     * jpct中的一个3d模型文件
     * 该属性表示当前的模型
     */
//    private Object3D current;

    /**
     * 模型集合
     */
//    private List<ObjItem> objList = new ArrayList<>();
    /**
     * Assets文件中模型和图片纹理的名字
     */
    private String[] objName = {"01", "02"};

    ImageTargetRendererObj(ImageTargetsActivity activity, SampleApplicationSession session) {
        super(activity);

        mActivity = activity;
        vuforiaAppSession = session;

//        //实例化虚拟世界
//        world = new World();
//        // 如果亮度太暗或是亮度怪怪的，可以調整這裡
//        world.setAmbientLight(255, 255, 255);
//        world.setClippingPlanes(1.0f, 3000.0f);
//        sun = new Light(world);
//        // 如果亮度太暗或是亮度怪怪的，可以調整這裡
//        sun.setIntensity(255, 255, 255);
//        //初始化模型集合
//        initObjList();
//        //垃圾回收，针对旧手机
//        MemoryHelper.compact();
    }


    /*
    private void initObjList() {
        //将所有模型文件放到List中
        for (String anObjName : objName) {
            ObjItem item = new ObjItem();
            item.setIndex(anObjName);
            //有些模型的纹理图片不止一个，你需要另外处理
            item.setTextures(Collections.singletonList(anObjName + ".jpg"));
            //设置纹理名字
            item.setTexturesName(Collections.singletonList(anObjName));
            //设置ser序列化文件的路径
            item.setSer("ser/" + anObjName + ".ser");
            objList.add(item);
        }

        try {
            for (int i = 0; i < objList.size(); i++) {
                ObjItem m = objList.get(i);
                //加载序列化文件的方法，对应有 Loader.loadOBJ() 、Loader.loadMD2() ....
                Object3D[] tmp = Loader.loadSerializedObjectArray(mActivity.getAssets().open(m.getSer()));
                for (int j = 0; m.getTextures() != null && j < m.getTextures().size(); j++) {
                    //模型纹理管理，第一个参数：纹理名字，第二个参数：对应纹理图片
                    TextureManager.getInstance().addTexture(m.getTexturesName().get(j),
                            new Texture(mActivity.getAssets().open(m.getTextures().get(j))));
                }
                if (tmp != null && tmp.length >= 1) {
                    //将当前obj3D存起来
                    m.setTarget(tmp[0]);
                }
                m.getTarget().strip();
                m.getTarget().build();

                if (m.getTextures() != null) {
                    for (int z = 0; z < m.getTextures().size(); z++) {
                        //设置纹理
                        m.getTarget().setTexture(m.getTexturesName().get(z));
                    }
                }
                //将obj添加到世界中
                world.addObject(m.getTarget());
                cam = world.getCamera();

                SimpleVector sv = new SimpleVector();
                sv.set(m.getTarget().getTransformedCenter());
                sv.y -= 100;
                sv.z -= 100;
                sun.setPosition(sv);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", "init: failed" + e);
        }
    }
    */



//    private RGBColor back = new RGBColor(50, 50, 100);

    /**
     * 旋转缩放模型的方法
     * 通过ImageTargets中监听屏幕事件的方法来达到旋转模型的效果
     */
    /*
    private void switchModel(final Object3D item) {
        //通过ImageTargets的手势回调
        mActivity.setOnModelChangeListener(new ImageTargetsActivity.OnModelChangeListener() {
            private float touchTurn;
            private float touchTurnUp;
            private float mScale = 1;

            @Override
            public void modelRotate(float touchTurn, float touchTurnUp) {
                this.touchTurn = touchTurn;
                this.touchTurnUp = touchTurnUp;
                // 如果touchTurn不为0,向Y轴旋转touchTure角度
                if (this.touchTurn != 0) {
                    // 旋转物体的旋转绕Y由给定矩阵W轴角（弧度顺时针方向为正值）,应用到对象下一次渲染时。
                    if (Math.abs(this.touchTurn) > 0.01) {
                        item.rotateY(this.touchTurn);
                    }
                    // 将touchTurn置0
                    this.touchTurn = 0;
                }
                if (this.touchTurnUp != 0) {
                    // 旋转物体的旋转围绕x由给定角度宽（弧度，逆时针为正值）轴矩阵,应用到对象下一次渲染时。
                    item.rotateX(this.touchTurnUp);
                    // 将touchTureUp置0
                    this.touchTurnUp = 0;
                }
                fb.clear(back);
            }

            @Override
            public void modelScale(float scale) {
                mScale = mScale + scale;
                float curScale = item.getScale();
                if (curScale < 0.1 && mScale < 0.99) {
                    //放大倍数小于0.1还要缩的时候 禁止缩放
                    return;
                }
                if (curScale > 6 && mScale > 1.01) {
                    return;
                }
                item.scale(mScale);
            }
        });
    }
    */



    // Function for initializing the renderer.    
    private void initRendering() {
        mRenderer = Renderer.getInstance();
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
    }

    private void updateRendering(int width, int height) {
        // Update screen dimensions
        vuforiaAppSession.setmScreenWidth(width);
        vuforiaAppSession.setmScreenHeight(height);

        // Reconfigure the video background
        vuforiaAppSession.configureVideoBackground();

        CameraCalibration camCalibration = com.vuforia.CameraDevice.getInstance().getCameraCalibration();
        Vec2F size = camCalibration.getSize();
        Vec2F focalLength = camCalibration.getFocalLength();
        float fovyRadians = (float) (2 * Math.atan(0.5f * size.getData()[1] / focalLength.getData()[1]));
        float fovRadians = (float) (2 * Math.atan(0.5f * size.getData()[0] / focalLength.getData()[0]));

        if (vuforiaAppSession.mIsPortrait) {
            fovy = fovRadians;
            fov = fovyRadians;
        } else {
            fov = fovRadians;
            fovy = fovyRadians;
        }

    }


    public static void printMatrix(String name, float[] matrix) {
        StringBuilder build = new StringBuilder();
        for (float f : matrix) {
            build.append(f + " ");
        }
        Log.d("zhf-m:", name+": "+build.toString());
    }

    // The render function.
    private void renderFrame() {
        // clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // get the state, and mark the beginning of a rendering section
        State state = mRenderer.begin();
        // explicitly render the video background
        mRenderer.drawVideoBackground();

        float[] modelviewArray;
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // 获得了可以追踪的目标（你上传的图片目标）
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            //当前扫描的图片
            // Log.e("TAG", "目标文件: " + trackable.getName());
            Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());
            printMatrix("modelViewMatrix", modelViewMatrix.getData());
            Matrix44F inverseMV = SampleMath.Matrix44FInverse(modelViewMatrix);
            printMatrix("inverseMV", inverseMV.getData());
            Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(inverseMV);
            printMatrix("invTranspMV", invTranspMV.getData());

            try {
                //先移除所有模型，以免模型重复叠加
//                world.removeAllObjects();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //重新获取当前扫描图片的模型
//            for (int i = 0; i < objList.size(); i++) {
//                ObjItem m = objList.get(i);
//                //如果设置的索引名与当前的图片名一致
//                if (m.getIndex().equals(trackable.getName())) {
//                    //将当前模型重新显示在世界里面
//                    world.addObject(m.getTarget());
//                    SimpleVector sv = new SimpleVector();
//                    sv.set(m.getTarget().getTransformedCenter());
//                    sv.y -= 100;
//                    sv.z -= 100;
//                    sun.setPosition(sv);
//                    current = objList.get(i).getTarget();
//                    break;
//                }
//            }

            modelviewArray = invTranspMV.getData();
            updateModelViewMatrix(modelviewArray);
        }

        // 没有检测到图片目标则隐藏3D模型
        if (state.getNumTrackableResults() == 0) {
            modelviewArray = new float[]{
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, -10000, 1
            };
            updateModelViewMatrix(modelviewArray);
        }
        mRenderer.end();
    }

    private void updateModelViewMatrix(float mat[]) {
        modelViewMat = mat;
    }

    private void updateCamera() {
        if (modelViewMat != null) {
            float[] m = modelViewMat;

            /*
            final SimpleVector camUp;
            if (vuforiaAppSession.mIsPortrait) {
                camUp = new SimpleVector(-m[0], -m[1], -m[2]);
            } else {
                camUp = new SimpleVector(-m[4], -m[5], -m[6]);
            }

            final SimpleVector camDirection = new SimpleVector(m[8], m[9], m[10]);
            final SimpleVector camPosition = new SimpleVector(m[12], m[13], m[14]);

            cam = world.getCamera();


            cam.setOrientation(camDirection, camUp);
            cam.setPosition(camPosition);

            cam.setFOV(fov);
            cam.setYFOV(fovy);
            */
        }
    }


    //---------------------------------------------
    @Override
    protected void initScene() {

    }

    // Called when the surface is created or recreated.
    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        initRendering(); // NOTE: Cocokin sama cpp - DONE

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler.sendEmptyMessageDelayed(LoadingDialogHandler.HIDE_LOADING_DIALOG, 5000);
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
//        if (fb != null) {
//            fb.dispose();
//        }
//        fb = new FrameBuffer(width, height);
//        Config.viewportOffsetAffectsRenderTarget = true;

        updateRendering(width, height);
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }

    @Override
    public void onRenderFrame(GL10 gl) {
        super.onRenderFrame(gl);
        if (!mIsActive) {
            return;
        }
        //渲染
        renderFrame();
        //更新相机
        updateCamera();
        //模型旋转缩放操作
//        if (current != null) {
//            switchModel(current);
//        }
//        world.renderScene(fb);
//        world.draw(fb);
//        fb.display();
    }



    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}
