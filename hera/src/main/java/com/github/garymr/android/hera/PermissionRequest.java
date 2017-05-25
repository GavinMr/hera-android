package com.github.garymr.android.hera;


public class PermissionRequest {

    private final String name;

    public PermissionRequest(String name) {
        this.name = name;
    }

    /**
     * One of the values found in {@link android.Manifest.permission}
     */
    public String getName() {
        return name;
    }

    @Override public String toString() {
        return "Permission name: " + name;
    }
}
