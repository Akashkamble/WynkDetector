package com.example.akash.facedetection;

import com.example.akash.facedetection.Events.LeftEyeCloseEvent;
import com.example.akash.facedetection.Events.NeutralFaceEvent;
import com.example.akash.facedetection.Events.RightEyeCloseEvent;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by akash on 22-02-2017.
 */

public class FaceTracker extends Tracker<Face> {
    private static final float OPEN_THRESHOLD = 0.65f;
    private boolean lefteyeClosed = true;
    private boolean righteyeClosed = true;

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        super.onUpdate(detections, face);
        float lefteye = face.getIsLeftEyeOpenProbability();
        float righteye = face.getIsRightEyeOpenProbability();
        if (lefteye == face.UNCOMPUTED_PROBABILITY && righteye == face.UNCOMPUTED_PROBABILITY) {
            lefteyeClosed = false;
            righteyeClosed = false;
        }
        if (lefteyeClosed && lefteye > OPEN_THRESHOLD) {
            lefteyeClosed = false;

        } else if (!lefteyeClosed && lefteye < OPEN_THRESHOLD) {
            lefteyeClosed = true;
        }
        if (righteyeClosed && righteye > OPEN_THRESHOLD) {
            righteyeClosed = false;
        } else if (!lefteyeClosed && righteye < OPEN_THRESHOLD) {
            righteyeClosed = true;
        }

        if (lefteyeClosed && !righteyeClosed) {
            EventBus.getDefault().post(new LeftEyeCloseEvent());
        } else if (righteyeClosed && !lefteyeClosed) {
            EventBus.getDefault().post(new RightEyeCloseEvent());
        } else if (!lefteyeClosed && !righteyeClosed) {
            EventBus.getDefault().post(new NeutralFaceEvent());
        }

    }
}

