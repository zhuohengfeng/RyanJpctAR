package com.ryan.ryanjpctar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Process;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ryan.ryanjpctar.vuforia.SampleApplicationControl;
import com.ryan.ryanjpctar.vuforia.SampleApplicationException;
import com.ryan.ryanjpctar.vuforia.SampleApplicationSession;
import com.ryan.ryanjpctar.vuforia.utils.LoadingDialogHandler;
import com.ryan.ryanjpctar.vuforia.utils.SampleApplicationGLView;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;

public class ImageTargetsActivity extends Activity implements SampleApplicationControl {
    private static final String LOG_TAG = "ImageTargets";

    SampleApplicationSession vuforiaAppSession;

    /**
     * 数据集(dat和xml文件)，用作图像追踪
     * 否则无法识别对应图像显示相应模型
     */
    private DataSet dataSet;

    /**
     * GLSurfaceView，openGl的视图，显示3D模型
     */
    private SampleApplicationGLView mGlView;

    /**
     * 渲染类，识别图片并显示3D模型的类
     */
    private ImageTargetRendererObj mRenderer;

    /**
     * 手势识别，这里仅仅用于点击对焦
     */
    private GestureDetector mGestureDetector;

    private boolean mSwitchDatasetAsap = false;
    private RelativeLayout mUILayout;

    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    private boolean mIsDroidDevice = false;

    private ArrayList<String> mDatasetStrings = new ArrayList<>();

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        vuforiaAppSession = new SampleApplicationSession(this);
        startLoadingAnimation();
        //添加你下载的图片数据库的xml文件
        mDatasetStrings.add("dataset.xml");
//        mDatasetStrings.add("StonesAndChips.xml"); // 和模型有关系？？？
//        mDatasetStrings.add("Tarmac.xml");

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mGestureDetector = new GestureDetector(this, new GestureListener());
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");
    }

    /**
     * 手势事件，在这里仅用于对焦
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        // 手动对焦，一秒后生效
        private final Handler autofocusHandler = new Handler();

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //Handler延迟对焦
            autofocusHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            return true;
        }
    }

    /**
     * 唤醒控件
     */
    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        try {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e) {
            Log.e(LOG_TAG, e.getString());
        }
        // Resume the GL view:
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(LOG_TAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }

    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e) {
            Log.e(LOG_TAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOG_TAG, e.getString());
        }

        Process.killProcess(Process.myPid());
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        mRenderer = new ImageTargetRendererObj(this, vuforiaAppSession);
        mGlView.setSurfaceRenderer(mRenderer);

    }

    /**
     * loading动画，等待Vuforia真正加载完毕
     */
    private void startLoadingAnimation() {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);
        mUILayout.setVisibility(View.VISIBLE);
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);
        loadingDialogHandler.mLL = mUILayout.findViewById(R.id.camera_overlay_tv);
        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (dataSet == null)
            dataSet = objectTracker.createDataSet();

        if (dataSet == null)
            return false;

        //加载数据集是否成功
        if (!dataSet.load(mDatasetStrings.get(0), STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        //激活数据集是否成功
        if (!objectTracker.activateDataSet(dataSet))
            return false;

        int numTrackables = dataSet.getNumTrackables();
        //将所有自己上传的图片的名字传给ImageTargetRenderer
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = dataSet.getTrackable(count);
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOG_TAG, "UserData:Set the following user data " + trackable.getUserData());
        }
        return true;
    }


    /**
     * 卸载追踪器
     *
     * @return 是否卸载成功
     */
    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (dataSet != null && dataSet.isActive()) {
            if (objectTracker.getActiveDataSet().equals(dataSet)
                    && !objectTracker.deactivateDataSet(dataSet)) {
                result = false;
            } else if (!objectTracker.destroyDataSet(dataSet)) {
                result = false;
            }

            dataSet = null;
        }

        return result;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null) {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e) {
                Log.e(LOG_TAG, e.getString());
            }
            CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
        } else {
            Log.e(LOG_TAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }


    // Shows initialization error messages as System dialogs
    private void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ImageTargetsActivity.this);
                builder.setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap) {
            Log.e("onVuforiaUpdate", "成功");
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || dataSet == null
                    || ot.getActiveDataSet() == null) {
                Log.d(LOG_TAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }


    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(
                    LOG_TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOG_TAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public void doStartTrackers() {
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

    }


    @Override
    public void doStopTrackers() {
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

    }

    @Override
    public boolean doDeinitTrackers() {
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return true;
    }

    private float xpos = -1;
    private float ypos = -1;

    /**
     * 旧的距离（两个手指按下后产生的距离）
     */
    private double oldDist;

    /**
     * 是否为双指按下，防止多点和单点手势冲突
     */
    private boolean isDoubleTouch = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        //手指数
        int pointerCount = event.getPointerCount();
        if (pointerCount == 1) {
            // 按键开始
            if (action == MotionEvent.ACTION_DOWN) {
                isDoubleTouch = false;
                // 保存按下的初始x,y位置于xpos,ypos中
                xpos = event.getX();
                ypos = event.getY();

                return true;
            }
            // 按键结束
            float touchTurn;
            float touchTurnUp;
            if (action == MotionEvent.ACTION_UP) {
                // 设置x,y及旋转角度为初始值
                xpos = -1;
                ypos = -1;
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE && !isDoubleTouch) {
                // 计算x,y偏移位置及x,y轴上的旋转角度
                float xd = event.getX() - xpos;
                float yd = event.getY() - ypos;
                // Logger.log("me.getX() - xpos----------->>"
                // + (me.getX() - xpos));
                xpos = event.getX();
                ypos = event.getY();
//                Logger.d("xpos------------>>" + xpos);
                // Logger.log("ypos------------>>" + ypos);
                // 以x轴为例，鼠标从左向右拉为正，从右向左拉为负
                touchTurn = xd / 100f;
                touchTurnUp = yd / 100f;
//                Logger.d("touchTurn------------>>" + touchTurn);
                // Logger.log("touchTurnUp------------>>" + touchTurnUp);
                if (listener != null) {
                    listener.modelRotate(touchTurn, touchTurnUp);
                }
                return true;
            }


        } else if (pointerCount == 2) {
            //缩放
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    isDoubleTouch = true;
                    xpos = -1;
                    ypos = -1;
                    oldDist = spacing(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (oldDist > 50) {
                        Log.e("TAG", "双手: " + oldDist);
                        double newDist = spacing(event);
                        float scale = (float) (newDist - oldDist);
                        scale = scale / 10000;
                        if (listener != null) {
                            listener.modelScale(scale);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        // 每Move一下休眠毫秒
        try {
            Thread.sleep(15);
        } catch (Exception e) {
            // No need for this...
        }

        return mGestureDetector.onTouchEvent(event);
    }


    /**
     * 计算两点之间的距离（两个手指）
     *
     * @param event Touch事件
     * @return 距离
     */
    private double spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    /**
     * 模型改变的事件
     */
    public OnModelChangeListener listener;

    public void setOnModelChangeListener(OnModelChangeListener listener) {
        this.listener = listener;
    }

    public interface OnModelChangeListener {
        /**
         * 模型旋转
         */
        void modelRotate(float touchTurn,
                         float touchTurnUp);

        /**
         * 模型缩放
         */
        void modelScale(float scale);
    }
}
