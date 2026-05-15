# hy-common

HouYu Common Module - 公共模块，提供通用工具类、常量定义、异常处理等基础功能。

## 模块结构

```
hy-common/
├── hy-common-log/     # 统一日志管理模块
└── pom.xml            # 父 POM，统一管理依赖版本
```

---

## hy-common-log

通过自定义 Logback Appender 实现统一日志管理的后端公共日志模块。

### 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 4.1.0-RC1 | 应用框架 |
| Logback | 内置 | 日志框架 |
| Disruptor | 3.4.4 | 高性能队列（异步日志处理） |
| Jackson | 内置 | JSON 格式化 |
| Redisson | 3.23.5 | 分布式锁（机器码注册） |
| JetCache | 2.7.9-SNAPSHOT | 多级缓存 |
| Micrometer | 内置 | 指标监控 |

### 核心功能

| 功能 | 说明 |
|------|------|
| Logback Appender 接管 | 通过编程方式注册自定义 Appender，无需修改 logback.xml |
| 高性能异步处理 | 基于 Disruptor 队列，非阻塞投递 |
| 统一日志格式 | 支持 JSON 和文本两种格式 |
| 日志脱敏 | 支持手机号、邮箱、身份证、银行卡、密码脱敏 |
| 日志过滤 | 按级别、关键词等过滤日志 |
| 日志采样 | 支持按比例采样，降低高并发下日志量 |
| 链路追踪 | 19位 TraceId（时间戳+机器码+序列+标志） |
| 多渠道输出 | 控制台、文件、数据库 |
| 数据库日分表 | 自动路由到日分表，自动清理历史数据 |

### 快速开始

#### 1. 添加依赖

```xml
<dependency>
    <groupId>com.houyu</groupId>
    <artifactId>hy-common-log</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 2. 配置（application.yml）

```yaml
hy:
  log:
    enabled: true
    appender:
      buffer-size: 8192
    trace:
      enabled: true
      redis-address: redis://localhost:6379
      redis-database: 0
    output:
      format: json
      db-enabled: false
      db-batch-size: 100
      db-flush-interval-ms: 5000
    desensitize:
      enabled: true
```

#### 3. 使用示例

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExampleService.class);
    
    public void doSomething() {
        // 普通日志
        logger.info("业务操作开始");
        
        // 带 MDC 上下文
        MDC.put("traceId", "2605151030000100010");
        logger.info("用户ID: {}, 操作: {}", "12345", "查询");
        MDC.clear();
    }
}
```

### TraceId 格式

TraceId 为 19 位十进制字符串，格式如下：

```
┌──────────────────┬──────────────┬──────────────┬──────┐
│   10位 时间戳     │  4位 机器码  │  4位 自增序列 │ 1位  │
│  (yyMMddHHmm)    │  (0000~9999) │  (0000~9999) │ 标志 │
└──────────────────┴──────────────┴──────────────┴──────┘
```

**示例**: `2605151030000100010`
- 时间戳: `2605151030`（2026-05-15 10:30）
- 机器码: `0001`
- 自增序列: `0001`
- 标志位: `0`

### 目录结构

```
hy-common-log/
├── src/main/java/com/houyu/common/log/
│   ├── config/          # 配置类
│   │   ├── LogProperties.java
│   │   ├── LogAutoConfiguration.java
│   │   └── RedissonConfig.java
│   ├── appender/        # Logback Appender 接管层
│   │   ├── HyCommonLogAppender.java
│   │   ├── LogEventHolder.java
│   │   └── LogEventHandler.java
│   ├── converter/       # 日志事件转换层
│   │   └── LogEventConverter.java
│   ├── model/           # 数据模型
│   │   ├── HyLogEvent.java
│   │   ├── HttpRequestInfo.java
│   │   ├── HttpResponseInfo.java
│   │   └── LogLevel.java
│   ├── formatter/       # 日志格式化层
│   │   ├── LogFormatter.java
│   │   ├── JsonLogFormatter.java
│   │   └── TextLogFormatter.java
│   ├── desensitizer/    # 日志脱敏层
│   │   ├── Desensitizer.java
│   │   ├── PhoneDesensitizer.java
│   │   ├── EmailDesensitizer.java
│   │   ├── IdCardDesensitizer.java
│   │   ├── BankCardDesensitizer.java
│   │   ├── PasswordDesensitizer.java
│   │   └── DesensitizerManager.java
│   ├── filter/          # 日志过滤器
│   │   ├── LogFilter.java
│   │   ├── LevelLogFilter.java
│   │   └── KeywordLogFilter.java
│   ├── sampler/         # 日志采样器
│   │   ├── LogSampler.java
│   │   └── PercentageLogSampler.java
│   ├── output/          # 日志输出层
│   │   ├── LogOutput.java
│   │   ├── LogOutputManager.java
│   │   ├── ConsoleLogOutput.java
│   │   ├── FileLogOutput.java
│   │   ├── DbLogOutput.java
│   │   ├── AsyncDbLogWriter.java
│   │   └── LogTableRouter.java
│   ├── trace/           # 链路追踪组件
│   │   ├── TraceIdGenerator.java
│   │   ├── DefaultTraceIdGenerator.java
│   │   ├── MachineIdManager.java
│   │   ├── SequenceGenerator.java
│   │   ├── FlagValidator.java
│   │   └── TraceContextHolder.java
│   ├── annotation/      # 注解
│   │   └── LogDesensitize.java
│   ├── constant/        # 常量
│   │   └── LogConstants.java
│   └── util/            # 工具类
│       └── LogUtils.java
├── src/main/resources/
│   ├── META-INF/spring/
│   │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── sql/
│       └── log_table_schema.sql
└── pom.xml
```

---

## 构建

```bash
# 清理并打包
mvn clean package -DskipTests

# 运行测试
mvn test
```