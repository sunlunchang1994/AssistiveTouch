package com.slc.assistivetouch.model.kernel;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.slc.assistivetouch.model.XpLog;

/**
 * Created by on the way on 2018/10/22.
 */

public class TorchModel {
    public static final int TORCH_STATUS_OFF = 0;
    public static final int TORCH_STATUS_ON = 1;
    public static final int TORCH_STATUS_ERROR = -1;
    public static final int TORCH_STATUS_UNKNOWN = -2;

    private Handler mHandler;
    private Context mContext;
    private CameraManager mCameraManager;
    private Camera mCamera;
    private String mCameraId;
    private int mTorchStatus;

    public TorchModel(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    private final CameraManager.TorchCallback mTorchCallback =
            new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    XpLog.log("onTorchModeUnavailable: cameraId=" + cameraId);
                    if (TextUtils.equals(cameraId, getCameraId())) {
                        mTorchStatus = TORCH_STATUS_ERROR;
                        Toast.makeText(mContext, "打开手电筒出错", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    XpLog.log("onTorchModeChanged: cameraId=" + cameraId +
                            "; enabled=" + enabled);
                    if (TextUtils.equals(cameraId, getCameraId())) {
                        if (enabled) {
                            mTorchStatus = TORCH_STATUS_ON;
                        } else {
                            mTorchStatus = TORCH_STATUS_OFF;

                        }
                    }
                }
            };

    /**
     * 获取相机Id
     *
     * @return
     */
    private String getCameraId() {
        if (mCameraId == null) {
            try {
                String[] ids = mCameraManager.getCameraIdList();
                for (String id : ids) {
                    CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                    Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                    if (flashAvailable != null && flashAvailable && lensFacing != null &&
                            lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        mCameraId = id;
                    }
                }
                XpLog.log("getCameraId: " + mCameraId);
            } catch (Exception e) {
                /*e.printStackTrace();
                mTorchStatus = TORCH_STATUS_ERROR;*/
            }
        }
        return mCameraId;
    }

    private Runnable toggleTorchRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTorchStatus == TORCH_STATUS_OFF) {
                setTorchOn();
            } else if (mTorchStatus == TORCH_STATUS_ON) {
                setTorchOff();
            }
        }
    };

    /**
     * 闪光灯
     */
    public synchronized void toggleTorch() {
        mHandler.post(toggleTorchRunnable);
    }

    /**
     * 打开
     */
    private synchronized void setTorchOn() {
        if (mTorchStatus != TORCH_STATUS_OFF) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                mCameraManager.registerTorchCallback(mTorchCallback, null);
                mCameraManager.setTorchMode(getCameraId(), true);
            } else {
                if (mCamera == null) {
                    mCamera = Camera.open();
                }
                Camera.Parameters camParams = mCamera.getParameters();
                camParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(camParams);
                mCamera.setPreviewTexture(new SurfaceTexture(0));
                mCamera.startPreview();
            }
            mTorchStatus = TORCH_STATUS_ON;
            XpLog.log("setTorchOn");
        } catch (Exception e) {
            /*e.printStackTrace();
            mTorchStatus = TORCH_STATUS_ERROR;*/
        }
    }

    /**
     * 关闭
     */
    private synchronized void setTorchOff() {
        if (mTorchStatus != TORCH_STATUS_ON) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(getCameraId(), false);
                mCameraManager.unregisterTorchCallback(mTorchCallback);
                mCameraManager = null;
            } else {
                if (mCamera != null) {
                    Camera.Parameters camParams = mCamera.getParameters();
                    camParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(camParams);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            }
            mTorchStatus = TORCH_STATUS_OFF;
            XpLog.log("setTorchOff");
        } catch (Exception e) {
            /*e.printStackTrace();
            mTorchStatus = TORCH_STATUS_ERROR;*/
        }
    }

}
