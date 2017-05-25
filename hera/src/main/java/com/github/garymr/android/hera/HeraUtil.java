package com.github.garymr.android.hera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;


public final class HeraUtil {

    private static final String TAG = "Hera";

    public static void logD(String message) {
        if (Hera.debugEnabled) {
            Log.d(TAG, message);
        }
    }

    public static void logW(String message, Throwable e) {
        if (Hera.debugEnabled) {
            Log.w(TAG, message, e);
        }
    }

    static int checkSelfPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (TextUtils.equals(Manifest.permission.SYSTEM_ALERT_WINDOW, permission)) {
                return Settings.canDrawOverlays(context) ?
                        PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            } else if (TextUtils.equals(Manifest.permission.WRITE_SETTINGS, permission)) {
                return Settings.System.canWrite(context) ?
                        PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            }
        }

        try {
            return ContextCompat.checkSelfPermission(context, permission);
        } catch (RuntimeException ignored) {
            return PackageManager.PERMISSION_DENIED;
        }
    }

    static void requestPermissions(Activity activity, List<String> permissions) {
        List<String> normalPermissions = new LinkedList<>();

        for (String permission : permissions) {
            if (TextUtils.equals(permission, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, Hera.PERMISSIONS_REQUEST_CODE_SYSTEM_ALERT_WINDOW);
                } catch (Exception e) {
                    HeraUtil.logW("request SYSTEM_ALERT_WINDOW permission failure.", e);
                    normalPermissions.add(permission);
                }
            } else if (TextUtils.equals(permission, Manifest.permission.WRITE_SETTINGS)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, Hera.PERMISSIONS_REQUEST_CODE_WRITE_SETTINGS);
                } catch (Exception e) {
                    HeraUtil.logW("request WRITE_SETTINGS permission failure.", e);
                    normalPermissions.add(permission);
                }
            } else {
                normalPermissions.add(permission);
            }
        }

        if (normalPermissions.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    normalPermissions.toArray(new String[normalPermissions.size()]), Hera.PERMISSIONS_REQUEST_CODE);
        }
    }

    static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        try {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        } catch (Exception e) {
            HeraUtil.logW(e.getMessage(), e);
            return false;
        }
    }

}
