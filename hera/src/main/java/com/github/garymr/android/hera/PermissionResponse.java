package com.github.garymr.android.hera;

import java.util.LinkedList;
import java.util.List;

/**
 * 授权结果,包含通过授权和拒绝授权信息
 */
public class PermissionResponse {

    private final List<PermissionGrantedResponse> grantedPermissionResponses;
    private final List<PermissionDeniedResponse> deniedPermissionResponses;

    PermissionResponse() {
        grantedPermissionResponses = new LinkedList<>();
        deniedPermissionResponses = new LinkedList<>();
    }

    /**
     * 获取已通过授权权限列表
     * @return
     */
    public List<PermissionGrantedResponse> getGrantedPermissionResponses() {
        return grantedPermissionResponses;
    }

    /**
     * 获取已拒绝授权权限列表
     * @return
     */
    public List<PermissionDeniedResponse> getDeniedPermissionResponses() {
        return deniedPermissionResponses;
    }

    /**
     * 是否所有权限已通过授权
     * @return
     */
    public boolean isAllPermissionsGranted() {
        return deniedPermissionResponses.isEmpty();
    }

    boolean addGrantedPermissionResponse(PermissionGrantedResponse response) {
        return grantedPermissionResponses.add(response);
    }

    boolean addDeniedPermissionResponse(PermissionDeniedResponse response) {
        return deniedPermissionResponses.add(response);
    }

    void clear() {
        grantedPermissionResponses.clear();
        deniedPermissionResponses.clear();
    }
}
