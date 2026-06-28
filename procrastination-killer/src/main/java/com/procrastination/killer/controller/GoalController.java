package com.procrastination.killer.controller;

import com.procrastination.killer.common.Result;
import com.procrastination.killer.dto.GoalCreateRequest;
import com.procrastination.killer.dto.GoalDetailVO;
import com.procrastination.killer.dto.GoalListItemVO;
import com.procrastination.killer.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目标业务接口
 */
@Tag(name = "目标管理", description = "目标创建、列表查询、详情查询接口")
@RestController
@RequestMapping("/api/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @Operation(
            summary = "创建目标（AI拆解）",
            description = "用户输入宏观目标，后端调用DeepSeek AI自动拆解为多个小任务，"
                    + "解析JSON后存入数据库（父表goal + 子表task），返回完整的目标详情含子任务列表。"
    )
    @PostMapping("/create")
    public Result<GoalDetailVO> createGoal(@Valid @RequestBody GoalCreateRequest request) {
        GoalDetailVO detail = goalService.createGoal(request);
        return Result.success("目标已拆解并创建成功", detail);
    }

    @Operation(summary = "获取我的目标列表", description = "返回当前登录用户的所有目标（不含子任务详情）")
    @GetMapping("/list")
    public Result<List<GoalListItemVO>> listGoals() {
        List<GoalListItemVO> list = goalService.listUserGoals();
        return Result.success(list);
    }

    @Operation(
            summary = "获取目标详情",
            description = "根据目标ID返回完整的目标信息及其下所有子任务的层级结构。"
                    + "仅允许访问属于自己的目标。"
    )
    @GetMapping("/{goalId}")
    public Result<GoalDetailVO> getGoalDetail(
            @Parameter(description = "目标ID", required = true, example = "1")
            @PathVariable Long goalId) {
        GoalDetailVO detail = goalService.getGoalDetail(goalId);
        return Result.success(detail);
    }
}
