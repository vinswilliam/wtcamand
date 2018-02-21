package com.gvm.wtcamera;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.gvm.wtcamera.camera.CameraFragment;
import com.gvm.wtcamera.camera2.Camera2Fragment;

public class CameraActivity extends AppCompatActivity {

    public static final String CAMERA_RESULT = "camresult";

    public static Intent startThisActivity(Context context) {
        return new Intent(context, CameraActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_camera);
        if (null == savedInstanceState) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2Fragment.newInstance())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CameraFragment.newInstance())
                        .commit();
            }
        }
    }
}
