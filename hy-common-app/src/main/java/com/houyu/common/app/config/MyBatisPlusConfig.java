package com.houyu.common.app.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.houyu.common.app.mybatis.DataPermissionInterceptor;
import com.houyu.common.app.mybatis.PageInterceptor;
import com.houyu.common.app.mybatis.TableShardInterceptor;
import com.houyu.common.app.service.PermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    private final AppProperties appProperties;
    private final PermissionService permissionService;

    public MyBatisPlusConfig(AppProperties appProperties, PermissionService permissionService) {
        this.appProperties = appProperties;
        this.permissionService = permissionService;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new PageInterceptor(appProperties));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        interceptor.addInnerInterceptor(new TableShardInterceptor());
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(permissionService));

        return interceptor;
    }
}