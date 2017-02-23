package com.example.akash.facedetection;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by akash on 22-02-2017.
 */

public class Service {
    public static boolean isServiceAvailable(@NonNull Activity activity, final int requestCode) {
        if (activity == null) {
            return false;
        }
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, requestCode).show();
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }
}
