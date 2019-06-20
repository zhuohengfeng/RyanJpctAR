/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/
package com.ryan.ryanjpctar;

import android.content.pm.ActivityInfo;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import com.ryan.ryanjpctar.vuforia.SampleApplicationSession;
import com.ryan.ryanjpctar.vuforia.utils.LoadingDialogHandler;

import com.ryan.ryanjpctar.vuforia.utils.Logger;
import com.ryan.ryanjpctar.vuforia.utils.SampleUtils;
import com.ryan.ryanjpctar.vuforia.utils.VideoBackgroundShader;
import com.vuforia.COORDINATE_SYSTEM_TYPE;
import com.vuforia.CameraCalibration;
import com.vuforia.Device;
import com.vuforia.GLTextureUnit;
import com.vuforia.Matrix44F;
import com.vuforia.Mesh;
import com.vuforia.Renderer;
import com.vuforia.RenderingPrimitives;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIEW;
import com.vuforia.Vec2F;
import com.vuforia.ViewList;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RenderTarget;

import java.nio.FloatBuffer;

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

//    private float[] modelViewMat;
    private float fov;
    private float fovy;
    private double[] mModelViewMatrix;

    ImageTargetRendererObj(ImageTargetsActivity activity, SampleApplicationSession session) {
        super(activity);

        mActivity = activity;
        vuforiaAppSession = session;

        mPosition = new Vector3();
        mOrientation = new Quaternion();
        mModelViewMatrix = new double[16];
    }



    /**
     * 旋转缩放模型的方法
     * 通过ImageTargets中监听屏幕事件的方法来达到旋转模型的效果
     */
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
//                        item.rotateY(this.touchTurn);
                        Logger.d("switchModel rotateY  this.touchTurn="+ this.touchTurn);
                        item.rotate(Vector3.Axis.Y, this.touchTurn * 360f);
                    }
                    // 将touchTurn置0
                    this.touchTurn = 0;
                }
                if (this.touchTurnUp != 0) {
                    // 旋转物体的旋转围绕x由给定角度宽（弧度，逆时针为正值）轴矩阵,应用到对象下一次渲染时。
//                    item.rotateX(this.touchTurnUp);
                    Logger.d("switchModel rotateX  this.touchTurnUp="+ this.touchTurnUp);
                    item.rotate(Vector3.Axis.X, this.touchTurnUp * 360f);
                    // 将touchTureUp置0
                    this.touchTurnUp = 0;
                }
//                fb.clear(back);
            }

            @Override
            public void modelScale(float scale) {
//                mScale = mScale + scale;
//                float curScale = item.getScale();
//                if (curScale < 0.1 && mScale < 0.99) {
//                    //放大倍数小于0.1还要缩的时候 禁止缩放
//                    return;
//                }
//                if (curScale > 6 && mScale > 1.01) {
//                    return;
//                }
//                item.scale(mScale);
            }
        });
    }



    // Function for initializing the renderer.    
    private void initRendering() {
        mRenderer = Renderer.getInstance();
        // Define clear color
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
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

        //------------------------------
        if(mBackgroundRenderTarget == null) {
            mBackgroundRenderTarget = new RenderTarget("camera_preview", width, height);

            addRenderTarget(mBackgroundRenderTarget);
            Material material = new Material();
            material.setColorInfluence(0);
            try {
                material.addTexture(mBackgroundRenderTarget.getTexture());
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            mBackgroundQuad = new ScreenQuad(ScreenQuad.UVmapping.CW);
//            if(!vuforiaAppSession.mIsPortrait) {
//                mBackgroundQuad.setScaleY((float)height / (float)vuforiaAppSession.getVideoHeight());
//            }
//            else{
//                mBackgroundQuad.setScaleX((float)width / (float)vuforiaAppSession.getVideoWidth());
//            }
//            Logger.d("onConfigurationChanged: width="+width+", height="+height+", getScreenOrientation()="+mArManager.getScreenOrientation()
//                    +", getVideoWidth()="+getVideoWidth()+", getVideoHeight()="+getVideoHeight());
            mBackgroundQuad.setMaterial(material);
            getCurrentScene().addChildAt(mBackgroundQuad, 0);
        }

        float fovDegrees = (float) (fovRadians * 180.0f / Math.PI);
        Logger.d("updateRendering: fovDegrees="+fovDegrees+", getVideoWidth="+vuforiaAppSession.getVideoWidth()+", getVideoHeight="+vuforiaAppSession.getVideoHeight());
        getCurrentCamera().setProjectionMatrix(fovDegrees, vuforiaAppSession.getVideoWidth(),
                vuforiaAppSession.getVideoHeight());
    }


    private void transformPositionAndOrientation(float[] modelViewMatrix) {
        mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
                -modelViewMatrix[14]);
        copyFloatToDoubleMatrix(modelViewMatrix, mModelViewMatrix);

        mOrientation.fromMatrix(mModelViewMatrix);

        if(!vuforiaAppSession.mIsPortrait)
        {
            mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
                    -modelViewMatrix[14]);
            mOrientation.y = -mOrientation.y;
            mOrientation.z = -mOrientation.z;
        }
        else
        {
            mPosition.setAll(-modelViewMatrix[13], -modelViewMatrix[12],
                    -modelViewMatrix[14]);
            double orX = mOrientation.x;
            mOrientation.x = -mOrientation.y;
            mOrientation.y = -orX;
            mOrientation.z = -mOrientation.z;
        }

        Logger.d("transformPositionAndOrientation x="+mPosition.x+", y="+mPosition.y+", z="+mPosition.z);
    }


    // The render function.
    private void renderFrame() {
        // clear color and depth buffer
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // get the state, and mark the beginning of a rendering section
        State state = mRenderer.begin();
        // explicitly render the video background

        // 开始绘制到模型上
        int frameBufferId = mBackgroundRenderTarget.getFrameBufferHandle();
        int frameBufferTextureId = mBackgroundRenderTarget.getTexture().getTextureId();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, frameBufferTextureId, 0);
        mRenderer.drawVideoBackground();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        float[] modelviewArray;
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // 获得了可以追踪的目标（你上传的图片目标）
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            //当前扫描的图片
            // Log.e("TAG", "目标文件: " + trackable.getName());
            Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());
            double[] inverseMV = new double[16];
            copyFloatToDoubleMatrix(modelViewMatrix.getData(), inverseMV);
            Matrix.rotateM(inverseMV, 0, -90, 1, 0, 0);

            float[] invTranspMV = new float[16];
            copyDoubleToFloatMatrix(inverseMV, invTranspMV);


            //Matrix44F inverseMV = SampleMath.Matrix44FInverse(modelViewMatrix);
            //Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(inverseMV);

            transformPositionAndOrientation(invTranspMV);
            onFoundMarker();
//            try {
//                //先移除所有模型，以免模型重复叠加
////                world.removeAllObjects();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

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

//            modelviewArray = invTranspMV.getData();
//            updateModelViewMatrix(invTranspMV);
        }

//        // 没有检测到图片目标则隐藏3D模型
        if (state.getNumTrackableResults() == 0) {
//            modelviewArray = new float[]{
//                    1, 0, 0, 0,
//                    0, 1, 0, 0,
//                    0, 0, 1, 0,
//                    0, 0, -10000, 1 // z = -10000
//            };
//            updateModelViewMatrix(modelviewArray);
            onNoFoundMarker();
        }
        mRenderer.end();
    }

//    private void updateModelViewMatrix(float mat[]) {
//        modelViewMat = mat;
//    }

    private void copyFloatToDoubleMatrix(float[] src, double[] dst)
    {
        for(int mI = 0; mI < 16; mI++)
        {
            dst[mI] = src[mI];
        }
    }

    private void copyDoubleToFloatMatrix(double[] src, float[] dst)
    {
        for(int mI = 0; mI < 16; mI++)
        {
            dst[mI] = (float) src[mI];
        }
    }




    //---------------------------------------------
    private Vector3 mPosition;
    private Quaternion mOrientation;

    private DirectionalLight          mLight;
    private SkeletalAnimationObject3D mBob;

    private Object3D mSphere;

    // 绘制相机背景
    private RenderTarget mBackgroundRenderTarget;
    private ScreenQuad mBackgroundQuad;

    @Override
    protected void initScene() {
        getCurrentCamera().setNearPlane(10f);
        getCurrentCamera().setFarPlane(2500f);

        try {
            mLight = new DirectionalLight(.1f, 0, -1.0f);
            mLight.setColor(1.0f, 1.0f, 1.0f);
            mLight.setPower(3);

            getCurrentScene().addLight(mLight);

            LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this,
                    R.raw.boblampclean_mesh);
            meshParser.parse();
            mBob = (SkeletalAnimationObject3D) meshParser
                    .getParsedAnimationObject();
            mBob.setScale(3);

            LoaderMD5Anim animParser = new LoaderMD5Anim("dance", this,
                    R.raw.boblampclean_anim);
            animParser.parse();
            mBob.setAnimationSequence((SkeletalAnimationSequence) animParser
                    .getParsedAnimationSequence());

            getCurrentScene().addChild(mBob);

            //mBob.play();
            mBob.setVisible(false);

            //------------
            Material material = new Material();
            material.addTexture(new Texture("earthColors",
                    R.drawable.earthtruecolor_nasa_big));
            material.setColorInfluence(0);
            mSphere = new Sphere(10, 50, 50);
            mSphere.setScale(10);
            mSphere.setMaterial(material);
            getCurrentScene().addChild(mSphere);

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.e("加载模型出错了！！");
        }
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
        updatePositionAndOrientation();
        //模型旋转缩放操作
        if (mBob != null) {
            switchModel(mBob);
        }
        if (mSphere != null) {
            mSphere.rotate(Vector3.Axis.Y, 1.0);
        }
    }

    private void updatePositionAndOrientation() {
        //mBob.setVisible(true);
        mBob.setPosition(mPosition);
        //mBob.setOrientation(mOrientation);

        mSphere.setPosition(mPosition);
        mSphere.setOrientation(mOrientation);
    }


    private void onFoundMarker(){
        if (mBob != null) {
            mBob.play();
            mBob.setVisible(true);
        }
        if (mSphere != null) {
            mSphere.setVisible(true);
        }
    }

    private void onNoFoundMarker(){
        if (mBob != null) {
            mBob.pause();
            mBob.setVisible(false);
        }
        if (mSphere != null) {
            mSphere.setVisible(false);
        }
    }


    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}
