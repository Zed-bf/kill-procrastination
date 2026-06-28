package com.procrastination.killer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务完成请求
 */
@Data
@Schema(description = "任务完成请求")
public class TaskCompleteRequest {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "要标记为完成的任务ID", example = "1")
    private Long taskId;
}
