package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

public class PageInterceptor implements InnerInterceptor {

    private static final int MAX_PAGE_SIZE = 5000;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (parameter instanceof IPage<?> page) {
            long size = page.getSize();
            if (size > MAX_PAGE_SIZE) {
                page.setSize(MAX_PAGE_SIZE);
            }
        }
    }
}