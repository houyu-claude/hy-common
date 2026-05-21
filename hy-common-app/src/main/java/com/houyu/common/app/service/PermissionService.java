package com.houyu.common.app.service;

import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public boolean hasPermission(String userId, String permission) {
        return true;
    }

    public String getDataScope(String userId) {
        return "";
    }
}