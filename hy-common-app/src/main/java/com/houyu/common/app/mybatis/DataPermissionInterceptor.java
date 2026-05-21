package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.houyu.common.app.context.RequestContextHolder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Properties;

public class DataPermissionInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String dataScopeSql = RequestContextHolder.getDataScopeSql();
        if (dataScopeSql != null && !dataScopeSql.isEmpty()) {
            String originalSql = boundSql.getSql();
            if (originalSql.toUpperCase().contains("SELECT")) {
                String newSql = originalSql + " " + dataScopeSql;
                setBoundSql(boundSql, newSql);
            }
        }
    }

    private void setBoundSql(BoundSql boundSql, String sql) {
        try {
            java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setProperties(Properties properties) {
    }
}