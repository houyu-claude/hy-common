package com.houyu.common.app.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.houyu.common.app.mybatis.DataPermissionInterceptor;
import com.houyu.common.app.mybatis.PageInterceptor;
import com.houyu.common.app.mybatis.TableShardInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new PageInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        interceptor.addInnerInterceptor(new TableShardInterceptor());
        interceptor.addInnerInterceptor(new DataPermissionInterceptor());

        return interceptor;
    }
}