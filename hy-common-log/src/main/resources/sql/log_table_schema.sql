CREATE TABLE sys_log (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id            VARCHAR(19)     NOT NULL,
    trace_id            VARCHAR(19)     NOT NULL,
    span_id             VARCHAR(19),
    parent_span_id      VARCHAR(19),
    service_name        VARCHAR(64),
    service_version     VARCHAR(32),
    environment         VARCHAR(32),
    log_level           VARCHAR(16)     NOT NULL,
    logger_name         VARCHAR(512),
    thread_name         VARCHAR(128),
    class_name          VARCHAR(512),
    method_name         VARCHAR(128),
    file_name           VARCHAR(256),
    line_number         INT,
    message             TEXT,
    formatted_message   TEXT,
    exception_class_name VARCHAR(512),
    exception_message   TEXT,
    stack_trace         TEXT,
    user_id             VARCHAR(64),
    username            VARCHAR(128),
    tenant_id           VARCHAR(64),
    server_ip           VARCHAR(64),
    client_ip           VARCHAR(64),
    execution_time      BIGINT,
    is_success          BOOLEAN,
    log_timestamp       TIMESTAMPTZ     NOT NULL,
    created_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sys_log_trace_id ON sys_log(trace_id);
CREATE INDEX idx_sys_log_log_timestamp ON sys_log(log_timestamp);
CREATE INDEX idx_sys_log_service_name ON sys_log(service_name);
CREATE INDEX idx_sys_log_user_id ON sys_log(user_id);
CREATE INDEX idx_sys_log_log_level ON sys_log(log_level);

CREATE TABLE sys_log_http_request (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id            VARCHAR(19)     NOT NULL,
    trace_id            VARCHAR(19)     NOT NULL,
    method              VARCHAR(16),
    uri                 VARCHAR(1024),
    url                 VARCHAR(2048),
    query_string        TEXT,
    content_type        VARCHAR(128),
    user_agent          VARCHAR(512),
    referer             VARCHAR(1024),
    protocol            VARCHAR(16),
    headers             JSONB,
    cookies             JSONB,
    request_body        TEXT,
    parameters          JSONB,
    created_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sys_log_http_request_trace_id ON sys_log_http_request(trace_id);

CREATE TABLE sys_log_http_response (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id            VARCHAR(19)     NOT NULL,
    trace_id            VARCHAR(19)     NOT NULL,
    status_code         INT,
    content_type        VARCHAR(128),
    content_length      BIGINT,
    error_code          VARCHAR(64),
    error_message       TEXT,
    headers             JSONB,
    response_body       TEXT,
    created_at          TIMESTAMPTZ     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sys_log_http_response_trace_id ON sys_log_http_response(trace_id);