package com.example.back_end.dto.response;

public record AppVersionResponse(
        boolean hasUpdate,
        String latestVersion,
        Integer versionCode,
        String changelog,
        String downloadUrl,
        String packageSize,
        boolean forceUpdate,
        String lanzouUrl,
        String lanzouPwd,
        String githubUrl) {

    public AppVersionResponse(
            boolean hasUpdate,
            String latestVersion,
            Integer versionCode,
            String changelog,
            String downloadUrl,
            String packageSize,
            boolean forceUpdate) {
        this(hasUpdate, latestVersion, versionCode, changelog, downloadUrl, packageSize, forceUpdate, null, null, null);
    }
}
