package com.procrastination.killer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 目标列表项视图（不含子任务详情）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "目标列表项")
public class GoalListItemVO {

    @Schema(description = "目标ID")
    private Long id;

    @Schema(description = "目标标题")
    private String title;

    @Schema(description = "目标状态：进行中 / 已完成")
    private String status;

    @Schema(description = "完成进度（0-100）")
    private Integer progress;

    @Schema(description = "任务总数")
    private Integer taskTotal;

    @Schema(description = "已完成任务数")
    private Integer taskCompleted;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
