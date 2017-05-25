package com.github.garymr.android.hera;

/**
 * 通过授权结果
 */
public class PermissionGrantedResponse {

    private final PermissionRequest requestedPermission;

    public PermissionGrantedResponse(PermissionRequest requestedPermission) {
        this.requestedPermission = requestedPermission;
    }

    public PermissionGrantedResponse(String permission) {
        this.requestedPermission = new PermissionRequest(permission);
    }

    public PermissionRequest getRequestedPermission() {
        return requestedPermission;
    }

    public String getPermissionName() {
        return requestedPermission.getName();
    }
}
