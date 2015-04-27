package org.daniel.andruino.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import org.daniel.andruino.app.utils.TU;

/**
 * Created by jiaoyang on 4/26/15.
 */
public class CameraActivity extends Activity {
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initCamera();
    }

    private void initCamera() {
        setContentView(R.layout.activity_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
    }

    private void initView() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);


    }
}
