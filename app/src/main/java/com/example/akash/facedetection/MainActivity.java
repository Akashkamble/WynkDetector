package com.example.akash.facedetection;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.akash.facedetection.Events.LeftEyeCloseEvent;
import com.example.akash.facedetection.Events.NeutralFaceEvent;
import com.example.akash.facedetection.Events.RightEyeCloseEvent;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private FaceDetector mFaceDetector;
    private CameraSource mCameraSource;
    private TextView lefteye, righteye;
    private final AtomicBoolean updating = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lefteye = (TextView) findViewById(R.id.lefteye);
        righteye = (TextView) findViewById(R.id.righteye);

        Service.isServiceAvailable(this, 1);
        if (isCameraPermissionGranted()) {
            createCameraResources();
        } else {
            requestCameraPermission();
        }
    }


    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createCameraResources();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission")
                .setMessage("No camera permission")
                .setPositiveButton("Ok", listener)
                .show();
    }

    private boolean isCameraPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void createCameraResources() {
        mFaceDetector = new FaceDetector.Builder(getApplicationContext())
//                For single face
                .setProminentFaceOnly(true)
//                Enable face tracking
                .setTrackingEnabled(true)
//                Detection of facial land marks
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE&FaceDetector.ACCURATE_MODE)
                .build();

        mFaceDetector.setProcessor(new LargestFaceFocusingProcessor(mFaceDetector, new FaceTracker()));
        // Check if FaceDetector is operational or not
        if (!mFaceDetector.isOperational()) {
            Toast.makeText(getBaseContext(), "createCameraResources: detector NOT operational", Toast.LENGTH_SHORT).show();
        }
//        Front camera
        mCameraSource = new CameraSource.Builder(this, mFaceDetector)
                .setRequestedPreviewSize(320, 320)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(60f)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        if (mCameraSource != null && isCameraPermissionGranted()) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFaceDetector != null) {
            mFaceDetector.release();
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLeftEyeClosed(LeftEyeCloseEvent e) {
        if (catchUpdatingLock()) {
            lefteye.setText("-");
            releaseUpdatingLock();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRightEyeClosed(RightEyeCloseEvent e) {
        if (catchUpdatingLock()) {
            righteye.setText("-");
            releaseUpdatingLock();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNeutralFace(NeutralFaceEvent e) {
        if (catchUpdatingLock()) {
            lefteye.setText("0");
            righteye.setText("0");
            releaseUpdatingLock();
        }
    }

    private boolean catchUpdatingLock() {
        return !updating.getAndSet(true);
    }

    private void releaseUpdatingLock() {
        updating.set(false);
    }
}
