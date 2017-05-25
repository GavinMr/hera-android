package com.github.garymr.android.hera;

/**
 * 拒绝授权结果
 */
public class PermissionDeniedResponse {

    private final PermissionRequest requestedPermission;

    private final boolean permanentlyDenied;

    public PermissionDeniedResponse(PermissionRequest requestedPermission,
                                    boolean permanentlyDenied) {
        this.requestedPermission = requestedPermission;
        this.permanentlyDenied = permanentlyDenied;
    }

    public PermissionDeniedResponse(String permission,
                                    boolean permanentlyDenied) {
        this.requestedPermission = new PermissionRequest(permission);
        this.permanentlyDenied = permanentlyDenied;
    }

    public PermissionRequest getRequestedPermission() {
        return requestedPermission;
    }

    public String getPermissionName() {
        return requestedPermission.getName();
    }

    /**
     * 用户是否勾选不在显示
     * @return
     */
    public boolean isPermanentlyDenied() {
        return permanentlyDenied;
    }
}
