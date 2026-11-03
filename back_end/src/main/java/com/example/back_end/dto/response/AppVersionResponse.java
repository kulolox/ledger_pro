package com.example.back_end.dto.response;

public record AppVersionResponse(
        boolean hasUpdate,
        String latestVersion,
        Integer versionCode,
        String changelog,
        String downloadUrl,
        String packageSize,
        boolean forceUpdate) {}
