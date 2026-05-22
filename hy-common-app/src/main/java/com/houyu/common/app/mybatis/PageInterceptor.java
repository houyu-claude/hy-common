package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houyu.common.app.config.AppProperties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

public class PageInterceptor implements InnerInterceptor {

    private final AppProperties appProperties;

    public PageInterceptor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (parameter instanceof IPage<?> page) {
            long size = page.getSize();
            int maxSize = appProperties.getPage().getMaxSize();
            if (size > maxSize) {
                page.setSize(maxSize);
            }
        }
    }
}