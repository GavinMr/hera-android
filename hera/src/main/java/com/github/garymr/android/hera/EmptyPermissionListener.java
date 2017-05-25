package com.github.garymr.android.hera;

import java.util.List;


public class EmptyPermissionListener implements PermissionListener {

    @Override
    public void onPermissionChecked(PermissionResponse response) {
        HeraUtil.logD("onPermissionChecked, isAllPermissionsGranted=" + response.isAllPermissionsGranted());
        if (!response.getGrantedPermissionResponses().isEmpty()) {
            for (PermissionGrantedResponse r : response.getGrantedPermissionResponses()) {
                HeraUtil.logD("Permission granted. " + r.getPermissionName());
            }
        }
        if (!response.getDeniedPermissionResponses().isEmpty()) {
            for (PermissionDeniedResponse r : response.getDeniedPermissionResponses()) {
                HeraUtil.logD("Denied granted. " + r.getPermissionName());
            }
        }
    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionRationaleHandler handler) {
        HeraUtil.logD("onPermissionRationaleShouldBeShown, permissions=" + permissions);

        handler.continuePermissionRequest();
    }
}
