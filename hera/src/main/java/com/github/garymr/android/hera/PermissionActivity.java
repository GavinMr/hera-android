package com.github.garymr.android.hera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;

public class PermissionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Hera.onActivityReady(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Hera.onActivityReady(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        HeraUtil.logD("onRequestPermissionsResult");
        List<String> grantedPermissions = new LinkedList<>();
        List<String> deniedPermissions = new LinkedList<>();

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            } else {
                grantedPermissions.add(permission);
            }
        }

        Hera.onPermissionsRequested(grantedPermissions, deniedPermissions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Hera.onActivityResult(requestCode, resultCode, data);
    }
}
