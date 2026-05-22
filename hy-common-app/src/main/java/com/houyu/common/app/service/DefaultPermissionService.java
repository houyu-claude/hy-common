package com.houyu.common.app.service;

import java.util.Collections;
import java.util.Set;

public class DefaultPermissionService implements PermissionService {

    @Override
    public boolean hasPermission(String userId, String permission) {
        return true;
    }

    @Override
    public String getDataScope(String userId) {
        return "";
    }

    @Override
    public Set<String> getPermissionWhiteList() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getDataScopeWhiteList() {
        return Collections.emptySet();
    }
}