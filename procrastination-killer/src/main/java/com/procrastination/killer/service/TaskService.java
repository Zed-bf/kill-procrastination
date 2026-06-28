package com.procrastination.killer.service;

import com.procrastination.killer.dto.TaskVO;
import com.procrastination.killer.entity.Goal;
import com.procrastination.killer.entity.Task;
import com.procrastination.killer.mapper.GoalMapper;
import com.procrastination.killer.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 任务服务
 *
 * 核心职责：任务完成状态联动更新
 * 当用户标记小任务为"已完成"时：
 * 1. 更新该任务的状态和完成时间
 * 2. 重新计算所属目标的进度百分比
 * 3. 若目标下所有任务均已完成，自动将目标状态更新为"已完成"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final GoalMapper goalMapper;

    /**
     * 标记任务为已完成（含状态联动更新逻辑）
     *
     * 该方法在一个事务内完成以下操作：
     * ① 更新 task 状态为"已完成"，记录完成时间
     * ② 统计 goal 下所有 task 的完成数
     * ③ 计算并更新 goal.progress
     * ④ 若全部完成，自动更新 goal.status 为"已完成"
     *
     * @param taskId 任务ID
     * @return 更新后的任务视图
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskVO completeTask(Long taskId) {
        // ============ Step 1: 查询任务并校验 ============
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if ("已完成".equals(task.getStatus())) {
            throw new RuntimeException("该任务已经是完成状态");
        }

        // ============ Step 2: 更新任务状态 ============
        task.setStatus("已完成");
        task.setCompletedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("任务已完成: taskId={}, goalId={}, content={}", taskId, task.getGoalId(), task.getContent());

        // ============ Step 3: 重新计算目标进度 ============
        recalculateGoalProgress(task.getGoalId());

        // ============ Step 4: 返回更新后的任务视图 ============
        return TaskVO.builder()
                .id(task.getId())
                .content(task.getContent())
                .status(task.getStatus())
                .sortOrder(task.getSortOrder())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .build();
    }

    /**
     * 将已完成的任务重置为待办
     * 同样触发目标进度重新计算
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskVO undoTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"已完成".equals(task.getStatus())) {
            throw new RuntimeException("该任务尚未完成，无需撤销");
        }

        // 重置状态
        task.setStatus("待办");
        task.setCompletedAt(null);
        taskMapper.updateById(task);
        log.info("任务已撤销: taskId={}, goalId={}", taskId, task.getGoalId());

        // 重新计算目标进度
        recalculateGoalProgress(task.getGoalId());

        return TaskVO.builder()
                .id(task.getId())
                .content(task.getContent())
                .status(task.getStatus())
                .sortOrder(task.getSortOrder())
                .completedAt(null)
                .createdAt(task.getCreatedAt())
                .build();
    }

    // ==================== 私有方法 ====================

    /**
     * 重新计算目标进度
     *
     * 该方法是状态联动更新的核心：
     * 1. 统计该目标下已完成任务数和总任务数
     * 2. 计算进度百分比（已完成数 / 总数 * 100）
     * 3. 使用原子 SQL 更新 goal 表的 progress 和 task_completed
     * 4. SQL 中通过 CASE WHEN 自动判断：若全部完成，则将 status 改为"已完成"
     *
     * @param goalId 目标ID
     */
    private void recalculateGoalProgress(Long goalId) {
        // 统计已完成数和总数
        int completedCount = taskMapper.countCompletedByGoalId(goalId);
        int totalCount = taskMapper.countTotalByGoalId(goalId);

        // 计算进度百分比（避免除零）
        int progress = totalCount > 0 ? (int) Math.round((double) completedCount / totalCount * 100) : 0;

        // 原子更新：同时更新 progress、taskCompleted，以及条件性更新 status
        int updated = goalMapper.updateProgressAndStatus(goalId, progress, completedCount);

        if (updated > 0) {
            // 查询更新后的目标状态用于日志
            Goal updatedGoal = goalMapper.selectById(goalId);
            log.info("目标进度已更新: goalId={}, progress={}%, completed={}/{}, status={}",
                    goalId, progress, completedCount, totalCount, updatedGoal.getStatus());
        }
    }
}
