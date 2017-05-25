package com.github.garymr.android.hera;


import java.util.List;

public interface PermissionListener {

    /**
     * 授权已处理完成
     * @param response
     */
    void onPermissionChecked(PermissionResponse response);

    /**
     * 上次授权未通过,并且用户上次拒绝授权未勾选"不再提醒"。
     * @param permissions
     * @param handler
     */
    void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionRationaleHandler handler);
}
