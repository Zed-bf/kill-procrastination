package com.procrastination.killer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 目标创建请求
 */
@Data
@Schema(description = "目标创建请求")
public class GoalCreateRequest {

    @NotBlank(message = "目标内容不能为空")
    @Size(min = 2, max = 200, message = "目标内容长度需在2-200个字符之间")
    @Schema(description = "用户输入的目标内容", example = "我想在一个月内减重5公斤")
    private String content;
}
