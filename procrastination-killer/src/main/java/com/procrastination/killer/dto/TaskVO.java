package com.procrastination.killer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务视图对象（子任务）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务详情")
public class TaskVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务内容")
    private String content;

    @Schema(description = "任务状态：待办 / 已完成")
    private String status;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
