package com.github.garymr.android.hera;


public class PermissionRationaleHandler {

    private boolean isResolved = false;

    public void continuePermissionRequest() {
        if (!isResolved) {
            Hera.onContinuePermissionRequest();
            isResolved = true;
        }
    }

    public void cancelPermissionRequest() {
        if (!isResolved) {
            Hera.onCancelPermissionRequest();
            isResolved = true;
        }
    }
}
