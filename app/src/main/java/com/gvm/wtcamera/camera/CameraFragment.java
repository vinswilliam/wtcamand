package com.gvm.wtcamera.camera;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gvm.wtcamera.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraFragment extends Fragment implements Camera.PictureCallback {

    public static final String CAMERA_RESULT = "camresult";

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    private Camera mCamera;
    private boolean isCameraPause = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!checkCamera(getActivity())) {
            getActivity().finish();
        } else {
            openCamera();
        }

        (getActivity().findViewById(R.id.btnTakePicture)).setOnClickListener(onClickCamera);
    }

    private View.OnClickListener onClickCamera = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.takePicture(null, null, CameraFragment.this);
        }
    };

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFile = getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        if (pictureFile != null) {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.flush();
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                showErrorMsg("File not found");
            }

            Intent intent = new Intent();
            intent.putExtra(CAMERA_RESULT, pictureFile.getAbsolutePath());
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCameraPause) {
            openCamera();
            isCameraPause = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isCameraPause = true;
    }

    private void openCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        int mCamId = getCameraInfo();
        try {
            mCamera = Camera.open(mCamId);
            CameraPreview cameraPreview = new CameraPreview(getActivity(), mCamera, mCamId);
            ((FrameLayout) getActivity().findViewById(R.id.camera_preview)).addView(cameraPreview);
        } catch (Exception e) {
            showErrorMsg("Failed open camera.");
        }
    }

    private void showErrorMsg(String errorMsg) {
        Snackbar.make(getActivity().findViewById(android.R.id.content), errorMsg,
                Snackbar.LENGTH_SHORT).show();
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "wtcam");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
                .format(new Date());

        if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            return new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            return new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
    }

    private boolean checkCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private int getCameraInfo() {
        int numbOfCam = Camera.getNumberOfCameras();
        int camId = -1;
        for (int i = 0; i < numbOfCam; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camId = i;
                break;
            } else {
                camId = i;
            }
        }
        return camId;
    }
}
