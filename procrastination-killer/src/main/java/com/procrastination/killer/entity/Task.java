package com.procrastination.killer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务实体（子表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属目标ID */
    private Long goalId;

    /** 任务内容 */
    private String content;

    /**
     * 任务状态
     * 可选值：待办 / 已完成
     */
    private String status;

    /** 排序序号（AI拆分时的建议执行顺序） */
    private Integer sortOrder;

    /** 完成时间 */
    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
