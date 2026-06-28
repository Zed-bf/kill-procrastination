package com.procrastination.killer.controller;

import com.procrastination.killer.common.Result;
import com.procrastination.killer.dto.TaskVO;
import com.procrastination.killer.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务操作接口
 */
@Tag(name = "任务管理", description = "任务状态更新接口，含目标进度联动")
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "标记任务为已完成",
            description = "将指定任务标记为'已完成'状态。系统会自动：\n"
                    + "1. 更新任务状态和完成时间\n"
                    + "2. 重新计算所属目标的完成进度\n"
                    + "3. 若该目标下所有任务均已完成，自动将目标状态更新为'已完成'"
    )
    @PutMapping("/{taskId}/complete")
    public Result<TaskVO> completeTask(
            @Parameter(description = "任务ID", required = true, example = "1")
            @PathVariable Long taskId) {
        TaskVO taskVO = taskService.completeTask(taskId);
        return Result.success("任务已完成，目标进度已同步更新", taskVO);
    }

    @Operation(
            summary = "撤销任务完成状态",
            description = "将已完成的任务恢复为'待办'状态，同步重新计算目标进度"
    )
    @PutMapping("/{taskId}/undo")
    public Result<TaskVO> undoTask(
            @Parameter(description = "任务ID", required = true, example = "1")
            @PathVariable Long taskId) {
        TaskVO taskVO = taskService.undoTask(taskId);
        return Result.success("任务已撤销，目标进度已同步更新", taskVO);
    }
}
