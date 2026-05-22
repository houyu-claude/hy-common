package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.app.service.PermissionService;
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
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

public class DataPermissionInterceptor implements InnerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DataPermissionInterceptor.class);

    private final PermissionService permissionService;

    public DataPermissionInterceptor(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        String dataScopeSql = RequestContextHolder.getDataScopeSql();
        if (dataScopeSql != null && !dataScopeSql.isEmpty()) {
            String validationError = validateDataScopeSql(dataScopeSql);
            if (validationError == null) {
                String originalSql = boundSql.getSql();
                if (originalSql.toUpperCase().contains("SELECT")) {
                    String newSql = appendDataScopeCondition(originalSql, dataScopeSql);
                    if (newSql != null) {
                        setBoundSql(boundSql, newSql);
                    }
                }
            } else {
                logger.warn("Data scope SQL validation failed: {}, error: {}", dataScopeSql, validationError);
            }
        }
    }

    private String validateDataScopeSql(String dataScopeSql) {
        Set<String> allowedScopes = permissionService.getDataScopeWhiteList();

        if (allowedScopes.isEmpty()) {
            logger.error("Data scope white list is empty! Data scope SQL injection protection disabled. " +
                    "Please provide a non-empty white list in your PermissionService implementation.");
            return "Data scope white list is empty, data scope SQL is rejected for security reasons";
        }

        if (!allowedScopes.contains(dataScopeSql)) {
            logger.error("Data scope SQL '{}' is not in the white list: {}", dataScopeSql, allowedScopes);
            return "Data scope SQL is not in the allowed white list";
        }

        try {
            CCJSqlParserUtil.parseCondExpression(dataScopeSql);
            return null;
        } catch (JSQLParserException e) {
            logger.error("Invalid data scope SQL expression: {}", e.getMessage());
            return "Invalid SQL expression: " + e.getMessage();
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
        MetaObject metaObject = SystemMetaObject.forObject(boundSql);
        metaObject.setValue("sql", sql);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}