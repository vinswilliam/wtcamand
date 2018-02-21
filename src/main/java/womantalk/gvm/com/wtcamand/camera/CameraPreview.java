package womantalk.gvm.com.wtcamand.camera;


import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private int mCamId;
    private SurfaceHolder mHolder;
    private float mRatio;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private List<Camera.Size> mSupportedPictureSizes;

    public CameraPreview(Context context, Camera camera, int camId) {
        super(context);
        mCamera = camera;
        mCamId = camId;
        mRatio = 1f;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mCamera.stopPreview();
        int paramRotate = setCameraDisplayOrientation((Activity) getContext(), mCamId, mCamera);
        Camera.Size supportedPictSize = getSupportedPictureSize(0.05);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(paramRotate);
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        parameters.setPictureSize(supportedPictSize.width, supportedPictSize.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mHolder.removeCallback(this);
            mCamera.release();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);

        if (mPreviewSize != null) {
            int heightTmp = mPreviewSize.height;
            int widthTmp = mPreviewSize.width;

            if (heightTmp >= widthTmp) {
                mRatio = (float) heightTmp / (float) widthTmp;
            } else {
                mRatio = (float) widthTmp / (float) heightTmp;
            }
            setMeasuredDimension(width, (int) (width * mRatio));
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) (h / w);

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        if (sizes != null) {
            for (Camera.Size size : sizes) {
                double ratio = (double) size.height / (double) size.width;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue;
                }

                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = (double) Math.abs(size.height - h);
                }
            }

            if (optimalSize == null) {
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size;
                        break;
                    }
                }
            }
        }
        return optimalSize;
    }

    private Camera.Size getSupportedPictureSize(double ratioTolerance) {
        Camera.Size result = null;
        for (Camera.Size size : mSupportedPictureSizes) {
            int height = size.height;
            int width = size.width;
            float tmpRatio;
            if (height >= width) {
                tmpRatio = (float) height / (float) width;
            } else {
                tmpRatio = (float) width / (float) height;
            }
            if (Math.abs(tmpRatio - mRatio) / mRatio <= ratioTolerance) {
                result = size;
            }
        }

        if (result == null) {
            return getSupportedPictureSize(ratioTolerance + 0.05);
        } else {
            return result;
        }
    }

    private int setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        if (rotation == Surface.ROTATION_0) {
            degrees = 0;
        } else if (rotation == Surface.ROTATION_90) {
            degrees = 90;
        } else if (rotation == Surface.ROTATION_180) {
            degrees = 180;
        } else if (rotation == Surface.ROTATION_270) {
            degrees = 270;
        }

        int result;

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        return result;
    }
}
