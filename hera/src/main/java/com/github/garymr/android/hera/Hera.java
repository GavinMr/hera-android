package com.github.garymr.android.hera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 权限控制, 支持安卓任意版本调用。
 */
public final class Hera {

    static final int PERMISSIONS_REQUEST_CODE = 80;
    static final int PERMISSIONS_REQUEST_CODE_SYSTEM_ALERT_WINDOW = 81;
    static final int PERMISSIONS_REQUEST_CODE_WRITE_SETTINGS = 82;
    static final int PERMISSIONS_REQUEST_CODE_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 83;

    private static final AtomicBoolean isRequestingPermission = new AtomicBoolean(false);
    private static final AtomicBoolean isRequestingRationalePermission = new AtomicBoolean(false);

    private static final List<String> pendingPermissions = new LinkedList<>();
    private static PermissionResponse permissionResponse = new PermissionResponse();
    private static PermissionListener permissionListener;
    private static final PermissionListener EMPTY_PERMISSION_LISTENER = new EmptyPermissionListener();

    private static final Object pendingPermissionsLock = new Object();
    private static Activity activity;

    static boolean debugEnabled = false;

    private Hera() {}

    public static void setDebugEnabled(boolean enabled) {
        Hera.debugEnabled = enabled;
    }

    /**
     * 当前是否有请求权限正在进行
     */
    public static boolean isRequestOngoing() {
        return isRequestingPermission.get();
    }

    private static void checkNoRequestOngoing() {
        if (isRequestingPermission.getAndSet(true)) {
            throw new IllegalStateException("Only one permission request at a time is allowed");
        }
    }

    /**
     * 检查是否拥有权限
     * @param context
     * @param permission    PackageManager.PERMISSION_GRANTED
     *                      PackageManager.PERMISSION_DENIED
     * @return
     */
    public static int checkPermission(Context context, String permission) {
        return HeraUtil.checkSelfPermission(context, permission);
    }

    public static void requestPermission(final Context context, final PermissionListener listener, final String... permissions) {
        requestPermission(context, listener, Arrays.asList(permissions));
    }

    public static void requestPermission(final Context context, final PermissionListener listener, final List<String> permissions) {
        checkNoRequestOngoing();

        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("Requires at least one permission");
        }

        pendingPermissions.clear();
        pendingPermissions.addAll(permissions);
        permissionResponse.clear();

        if (listener == null) {
            permissionListener = EMPTY_PERMISSION_LISTENER;
        } else {
            permissionListener = listener;
        }

        if (Build.VERSION.SDK_INT >= 23) { // 6.0版本 运行时权限
            Intent intent = new Intent(context, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (Build.VERSION.SDK_INT >= 18) { // 4.3版本 增加自定义管理权限功能
            Intent intent = new Intent(context, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            updatePermissionsAsGranted(permissions);
        }
    }

    /**
     * PermissionActivity中会调用该方法
     * @param activity
     */
    static void onActivityReady(Activity activity) {
        HeraUtil.logD("onActivityReady");

        Hera.activity = activity;

        List<String> grantedPermissions = new LinkedList<>();
        List<String> deniedPermissions = new LinkedList<>();
        synchronized (pendingPermissionsLock) {
            if (activity != null) {
                for (String permission : pendingPermissions) {
                    int permissionState = checkSelfPermission(permission);
                    switch (permissionState) {
                        case PackageManager.PERMISSION_DENIED:
                            deniedPermissions.add(permission);
                            break;
                        case PackageManager.PERMISSION_GRANTED:
                        default:
                            grantedPermissions.add(permission);
                            break;
                    }
                }
            }
        }

        HeraUtil.logD("deniedPermissions size=" + deniedPermissions.size() + ", grantedPermissions size=" + grantedPermissions.size());
        // 已通过授权需要先处理,否则未通过授权处理中会导致pendingPermissions重复处理。
        if (grantedPermissions.size() > 0) {
            updatePermissionsAsGranted(grantedPermissions);
        }
        if (deniedPermissions.size() > 0) {
            handleDeniedPermissions(deniedPermissions);
        }
    }

    private static int checkSelfPermission(String permission) {
        return HeraUtil.checkSelfPermission(activity, permission);
    }

    private static void handleDeniedPermissions(List<String> permissions) {
        HeraUtil.logD("handleDeniedPermissions");
        if (permissions.isEmpty()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<PermissionRequest> shouldShowRequestRationalePermissions = new LinkedList<>();

            for (String permission : permissions) {
                if (HeraUtil.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRequestRationalePermissions.add(new PermissionRequest(permission));
                }
            }

            HeraUtil.logD("shouldShowRequestRationalePermissions isEmpty=" + shouldShowRequestRationalePermissions.isEmpty());
            if (shouldShowRequestRationalePermissions.isEmpty()) {
                requestPermissionsToSystem(permissions);
            } else if (!isRequestingRationalePermission.get()) {
                permissionListener.onPermissionRationaleShouldBeShown(shouldShowRequestRationalePermissions,
                        new PermissionRationaleHandler());
            }
        } else {
            updatePermissionsAsDenied(permissions);
        }
    }

    /**
     * 更新通过授权权限
     * @param permissions
     */
    private static void updatePermissionsAsGranted(List<String> permissions) {
        HeraUtil.logD("updatePermissionsAsGranted, permissions=" + permissions);
        for (String permission : permissions) {
            PermissionGrantedResponse response = new PermissionGrantedResponse(permission);
            permissionResponse.addGrantedPermissionResponse(response);
        }
        onPermissionsChecked(permissions);
    }

    /**
     * 更新禁止授权权限
     * @param permissions
     */
    private static void updatePermissionsAsDenied(List<String> permissions) {
        HeraUtil.logD("updatePermissionsAsDenied, permissions=" + permissions);
        for (String permission : permissions) {
            PermissionDeniedResponse response = new PermissionDeniedResponse(permission,
                    !HeraUtil.shouldShowRequestPermissionRationale(activity, permission));
            permissionResponse.addDeniedPermissionResponse(response);
        }
        onPermissionsChecked(permissions);
    }

    private static void onPermissionsChecked(List<String> permissions) {
        if (pendingPermissions.isEmpty()) {
            return;
        }

        synchronized (pendingPermissionsLock) {
            pendingPermissions.removeAll(permissions); // 删除已请求的权限
            if (pendingPermissions.isEmpty()) { // 所有权限都调用完,调用回调接口
                if (activity != null) {
                    activity.finish();
                    activity = null;
                }
                isRequestingPermission.set(false);
                isRequestingRationalePermission.set(false);
                permissionListener.onPermissionChecked(permissionResponse);
                permissionListener = EMPTY_PERMISSION_LISTENER;
            }
        }
    }

    private static void requestPermissionsToSystem(List<String> permissions) {
        HeraUtil.logD("requestPermissionsToSystem, activity=" + activity + ", permissions=" + permissions);

        HeraUtil.requestPermissions(activity, permissions);
    }

    static void onPermissionsRequested( List<String> grantedPermissions,
                                       List<String> deniedPermissions) {
        onPermissionRequestGranted(grantedPermissions);
        onPermissionRequestDenied(deniedPermissions);
    }

    /**
     * Method called whenever the permissions has been granted by the user
     */
    static void onPermissionRequestGranted(List<String> permissions) {
        if (permissions != null && permissions.size() > 0) {
            updatePermissionsAsGranted(permissions);
        }
    }

    /**
     * Method called whenever the permissions has been denied by the user
     */
    static void onPermissionRequestDenied(List<String> permissions) {
        if (permissions != null && permissions.size() > 0) {
            updatePermissionsAsDenied(permissions);
        }
    }

    static void onContinuePermissionRequest() {
        isRequestingRationalePermission.set(true);
        requestPermissionsToSystem(pendingPermissions);
    }

    /**
     * Method called when the user has been informed with a rationale and decides to cancel
     * the permission request process
     */
    static void onCancelPermissionRequest() {
        isRequestingRationalePermission.set(false);
        updatePermissionsAsDenied(pendingPermissions);
    }

    static void onActivityResult(int requestCode, int resultCode, Intent data) {
        HeraUtil.logD("onActivityResult, requestCode=" + requestCode + ", resultCode=" + resultCode + ", data=" + data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new LinkedList<>();
            Integer permission = null;
            if (requestCode == PERMISSIONS_REQUEST_CODE_SYSTEM_ALERT_WINDOW) {
                permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
                permission = HeraUtil.checkSelfPermission(activity, Manifest.permission.SYSTEM_ALERT_WINDOW);
            } else if (requestCode == PERMISSIONS_REQUEST_CODE_WRITE_SETTINGS) {
                permissions.add(Manifest.permission.WRITE_SETTINGS);
                permission = HeraUtil.checkSelfPermission(activity, Manifest.permission.WRITE_SETTINGS);
            } else if (requestCode == PERMISSIONS_REQUEST_CODE_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) {
                permissions.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                permission = HeraUtil.checkSelfPermission(activity, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            }

            if (permission == null) {
                return;
            }

            if (permission == PackageManager.PERMISSION_GRANTED) {
                updatePermissionsAsGranted(permissions);
            } else {
                updatePermissionsAsDenied(permissions);
            }
        }

    }

}
