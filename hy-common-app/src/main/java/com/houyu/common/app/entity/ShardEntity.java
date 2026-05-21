package com.houyu.common.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公共分表实体")
public class ShardEntity extends BaseEntity {

    @Schema(description = "替换前表名")
    private String tableNameSrc;

    @Schema(description = "替换后表名")
    private String tableNameDest;
}