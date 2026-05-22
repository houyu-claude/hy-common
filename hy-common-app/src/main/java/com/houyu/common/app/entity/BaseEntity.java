package com.houyu.common.app.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Schema(description = "公共实体基类")
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "自增主键（内部排序，禁止插队）")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "是否当前有效版本")
    @TableField(value = "is_current")
    private Boolean isCurrent;

    @Schema(description = "日志链路追踪ID")
    @TableField(value = "trace_id", fill = FieldFill.INSERT_UPDATE)
    private String traceId;

    @Schema(description = "数据拥有人")
    @TableField(value = "owner", fill = FieldFill.INSERT_UPDATE)
    private String owner;

    @Schema(description = "创建人")
    @TableField(value = "creater", fill = FieldFill.INSERT)
    private String creater;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "修改人")
    @TableField(value = "updater", fill = FieldFill.INSERT_UPDATE)
    private String updater;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "操作类型（insert,update,delete）")
    @TableField(value = "op_type", fill = FieldFill.INSERT_UPDATE)
    private String opType;

    @Schema(description = "备注")
    @TableField(value = "remark")
    private String remark;
}