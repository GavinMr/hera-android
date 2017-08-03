# Hera

Hera 提供简单易用的Android运行时权限管理。

## 下载

### Gradle

```
dependencies {
	compile 'com.github.garymr.android:hera:1.0.1'
}
```

### Maven

```
<dependency>
  <groupId>com.github.garymr.android</groupId>
  <artifactId>hera</artifactId>
  <version>1.0.1</version>
</dependency>
```

## 开始使用Hera

### 检查是否拥有权限
```
Hera.checkPermission(context, permission)
```

### 当前是否有请求权限正在进行
```
Hera.isRequestOngoing()
```

### 请求权限

```
PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionChecked(PermissionResponse response) {
            // 权限请求完成

            Log.d("Hera", "onPermissionChecked, isAllPermissionsGranted=" + response.isAllPermissionsGranted());

            // 用户同意授予的权限
            if (!response.getGrantedPermissionResponses().isEmpty()) {
                for (PermissionGrantedResponse r : response.getGrantedPermissionResponses()) {
                    Log.d("Hera", "Permission granted. " + r.getPermissionName());
                }
            }

            // 用户拒绝授予的权限
            if (!response.getDeniedPermissionResponses().isEmpty()) {
                for (PermissionDeniedResponse r : response.getDeniedPermissionResponses()) {
                    Log.d("Hera", "Denied granted. " + r.getPermissionName());
                }
            }
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionRationaleHandler handler) {
            handler.continuePermissionRequest();
            // 必须执行 continuePermissionRequest 或者 cancelPermissionRequest。否则会导致无回调
        }
};

Hera.requestPermission(v.getContext(), permissionListener,
                        Manifest.permission.WAKE_LOCK, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE);

// 如果没有特殊处理listener也可以不传入
Hera.requestPermission(v.getContext(), null,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.WRITE_SETTINGS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);

```

* 如果不初始化将会使用默认配置，TAG 为：LOGGER*

### 开启调试状态

```
Hera.setDebugEnabled(true);

```

## License
* [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)