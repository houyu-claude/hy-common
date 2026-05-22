package com.houyu.common.app.service;

import java.util.Collections;
import java.util.Set;

public interface PermissionService {

    boolean hasPermission(String userId, String permission);

    String getDataScope(String userId);

    default Set<String> getPermissionWhiteList() {
        return Collections.emptySet();
    }

    default Set<String> getDataScopeWhiteList() {
        return Collections.emptySet();
    }
}