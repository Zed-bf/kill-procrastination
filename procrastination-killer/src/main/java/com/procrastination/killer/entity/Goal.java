package com.procrastination.killer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 目标实体（父表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("goal")
public class Goal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 目标标题 */
    private String title;

    /** 目标描述（AI生成的详细说明） */
    private String description;

    /**
     * 目标状态
     * 可选值：进行中 / 已完成
     */
    private String status;

    /** 完成进度（0-100） */
    private Integer progress;

    /** 任务总数 */
    private Integer taskTotal;

    /** 已完成任务数 */
    private Integer taskCompleted;

    /** AI原始返回JSON（调试/回溯用） */
    private String aiRawResponse;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
