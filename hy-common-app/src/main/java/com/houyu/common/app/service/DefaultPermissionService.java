package com.houyu.common.app.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Set;

/**
 * 默认权限服务实现。
 * <p>
 * <strong>@Deprecated - 仅限开发/测试环境使用！</strong>
 * </p>
 * <p>
 * 此实现会放行所有权限检查，适用于开发和测试阶段快速验证功能。
 * 生产环境必须提供真实的 PermissionService 实现。
 * </p>
 * <p>
 * 使用方式：在 @SpringBootApplication 类上添加 @EnableConfigurationProperties 或通过 @Bean 覆盖。
 * </p>
 *
 * @deprecated 生产环境必须替换为真实实现
 */
@Deprecated(since = "1.0.0", forRemoval = false)
public class DefaultPermissionService implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPermissionService.class);

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        if (activeProfile != null && !activeProfile.isEmpty()) {
            boolean isDevProfile = activeProfile.contains("dev") || activeProfile.contains("test") || 
                                   activeProfile.contains("local");
            if (!isDevProfile) {
                logger.warn("============================================");
                logger.warn("WARNING: DefaultPermissionService is being used in non-development environment!");
                logger.warn("Profile: {}", activeProfile);
                logger.warn("This implementation allows all permissions without any checks.");
                logger.warn("Please provide a real PermissionService implementation for production!");
                logger.warn("============================================");
            }
        }
    }

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