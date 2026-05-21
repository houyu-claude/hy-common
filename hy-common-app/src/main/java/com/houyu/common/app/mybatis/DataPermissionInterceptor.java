package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.houyu.common.app.context.RequestContextHolder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;

public class DataPermissionInterceptor implements InnerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DataPermissionInterceptor.class);

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String dataScopeSql = RequestContextHolder.getDataScopeSql();
        if (dataScopeSql != null && !dataScopeSql.isEmpty()) {
            String originalSql = boundSql.getSql();
            if (originalSql.toUpperCase().contains("SELECT")) {
                String newSql = appendDataScopeCondition(originalSql, dataScopeSql);
                if (newSql != null) {
                    setBoundSql(boundSql, newSql);
                }
            }
        }
    }

    private String appendDataScopeCondition(String originalSql, String dataScopeSql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select select) {
                SelectBody selectBody = select.getSelectBody();
                if (selectBody instanceof PlainSelect plainSelect) {
                    appendWhereCondition(plainSelect, dataScopeSql);
                } else if (selectBody instanceof SetOperationList setOperationList) {
                    setOperationList.getSelects().forEach(sb -> {
                        if (sb instanceof PlainSelect ps) {
                            appendWhereCondition(ps, dataScopeSql);
                        }
                    });
                }
                return statement.toString();
            }
        } catch (JSQLParserException e) {
            logger.error("Failed to parse SQL for data permission: {}", e.getMessage());
        }
        return null;
    }

    private void appendWhereCondition(PlainSelect plainSelect, String dataScopeSql) {
        try {
            net.sf.jsqlparser.expression.Expression dataScopeExpr = CCJSqlParserUtil.parseCondExpression(dataScopeSql);
            
            if (plainSelect.getWhere() == null) {
                plainSelect.setWhere(dataScopeExpr);
            } else {
                net.sf.jsqlparser.expression.Expression existingWhere = plainSelect.getWhere();
                net.sf.jsqlparser.expression.Expression combined = 
                    new net.sf.jsqlparser.expression.operators.conditional.AndExpression(
                        existingWhere, dataScopeExpr);
                plainSelect.setWhere(combined);
            }
        } catch (JSQLParserException e) {
            logger.error("Failed to parse data scope condition: {}", e.getMessage());
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