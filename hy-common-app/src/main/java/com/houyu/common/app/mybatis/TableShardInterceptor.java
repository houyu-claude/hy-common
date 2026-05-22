package com.houyu.common.app.mybatis;

import com.baomidou.mybatisplus.core.interceptor.InnerInterceptor;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.houyu.common.app.entity.ShardEntity;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class TableShardInterceptor implements InnerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TableShardInterceptor.class);
    
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (parameter instanceof ShardEntity shardEntity) {
            String tableNameSrc = shardEntity.getTableNameSrc();
            String tableNameDest = shardEntity.getTableNameDest();

            if (tableNameSrc != null && tableNameDest != null) {
                validateTableName(tableNameSrc, "source");
                validateTableName(tableNameDest, "destination");
                
                String originalSql = boundSql.getSql();
                String newSql = replaceTableName(originalSql, tableNameSrc, tableNameDest);
                if (newSql != null) {
                    setBoundSql(boundSql, newSql);
                }
            }
        }
    }

    private void validateTableName(String tableName, String type) throws SQLException {
        if (tableName == null || tableName.isEmpty()) {
            throw new SQLException("Table sharding " + type + " table name cannot be null or empty");
        }
        
        if (!TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            logger.error("Invalid table name '{}' for table sharding: contains invalid characters", tableName);
            throw new SQLException("Invalid " + type + " table name: " + tableName);
        }
        
        if (tableName.length() > 64) {
            logger.error("Table name '{}' exceeds maximum length of 64 characters", tableName);
            throw new SQLException(type + " table name exceeds maximum length of 64 characters");
        }
    }

    private String replaceTableName(String sql, String srcTable, String destTable) throws SQLException {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            if (statement instanceof Select select) {
                replaceTableInSelect(select, srcTable, destTable);
            } else if (statement instanceof Update update) {
                Table table = update.getTable();
                if (table != null && srcTable.equalsIgnoreCase(table.getName())) {
                    table.setName(destTable);
                }
            } else if (statement instanceof Insert insert) {
                Table table = insert.getTable();
                if (table != null && srcTable.equalsIgnoreCase(table.getName())) {
                    table.setName(destTable);
                }
            } else if (statement instanceof Delete delete) {
                Table table = delete.getTable();
                if (table != null && srcTable.equalsIgnoreCase(table.getName())) {
                    table.setName(destTable);
                }
            }

            return statement.toString();
        } catch (JSQLParserException e) {
            logger.error("Failed to parse SQL for table sharding: {}", e.getMessage());
            throw new SQLException("Table sharding SQL parsing failed: " + sql, e);
        }
    }

    private void replaceTableInSelect(Select select, String srcTable, String destTable) {
        select.getSelectBody().accept(new net.sf.jsqlparser.util.TablesNamesFinder() {
            @Override
            public void visit(Table table) {
                if (srcTable.equalsIgnoreCase(table.getName())) {
                    table.setName(destTable);
                }
                super.visit(table);
            }
        });
    }

    private void setBoundSql(BoundSql boundSql, String sql) {
        PluginUtils.mpBoundSql(boundSql).sql(sql);
    }
}