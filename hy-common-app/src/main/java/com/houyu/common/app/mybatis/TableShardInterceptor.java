package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.houyu.common.app.entity.ShardEntity;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

public class TableShardInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (parameter instanceof ShardEntity shardEntity) {
            String tableNameSrc = shardEntity.getTableNameSrc();
            String tableNameDest = shardEntity.getTableNameDest();

            if (tableNameSrc != null && tableNameDest != null) {
                String sql = boundSql.getSql();
                String newSql = sql.replace(tableNameSrc, tableNameDest);
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
}