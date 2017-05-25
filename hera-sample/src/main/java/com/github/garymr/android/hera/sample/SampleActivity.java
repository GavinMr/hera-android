package com.github.garymr.android.hera.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.garymr.android.hera.Hera;
import com.github.garymr.android.hera.PermissionDeniedResponse;
import com.github.garymr.android.hera.PermissionGrantedResponse;
import com.github.garymr.android.hera.PermissionListener;
import com.github.garymr.android.hera.PermissionRationaleHandler;
import com.github.garymr.android.hera.PermissionRequest;
import com.github.garymr.android.hera.PermissionResponse;

import java.util.List;

/**
 * 注意: 请求的权限要在AndroidManifest中声明,否则会Crash
 */
public class SampleActivity extends Activity {

    private static final String TAG = "HeraSample";

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionChecked(PermissionResponse response) {
            Log.d(TAG, "onPermissionChecked, isAllPermissionsGranted=" + response.isAllPermissionsGranted());
            if (!response.getGrantedPermissionResponses().isEmpty()) {
                for (PermissionGrantedResponse r : response.getGrantedPermissionResponses()) {
                    Log.d(TAG, "Permission granted. " + r.getPermissionName());
                }
            }
            if (!response.getDeniedPermissionResponses().isEmpty()) {
                for (PermissionDeniedResponse r : response.getDeniedPermissionResponses()) {
                    Log.d(TAG, "Denied granted. " + r.getPermissionName());
                }
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionRationaleHandler handler) {
            Log.d(TAG, "onPermissionRationaleShouldBeShown, permissions=" + permissions);
            handler.continuePermissionRequest();

            // 必须执行 continuePermissionRequest 或者 cancelPermissionRequest。否则会导致无回调
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Hera.isRequestOngoing()) {
                    return;
                }
                Hera.requestPermission(v.getContext(), permissionListener,
                        Manifest.permission.WAKE_LOCK, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Hera.isRequestOngoing()) {
                    return;
                }
                // 如果没有特殊处理listener也可以不传入
                Hera.requestPermission(v.getContext(), null, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = Hera.checkPermission(v.getContext(), Manifest.permission.SYSTEM_ALERT_WINDOW);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(v.getContext(), Manifest.permission.SYSTEM_ALERT_WINDOW + " 已授权!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(v.getContext(), Manifest.permission.SYSTEM_ALERT_WINDOW + " 未授权!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
