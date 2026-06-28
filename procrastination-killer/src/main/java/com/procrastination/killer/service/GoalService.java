package com.procrastination.killer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.procrastination.killer.ai.AIPromptTemplates;
import com.procrastination.killer.common.CurrentUserHelper;
import com.procrastination.killer.dto.*;
import com.procrastination.killer.entity.Goal;
import com.procrastination.killer.entity.Task;
import com.procrastination.killer.mapper.GoalMapper;
import com.procrastination.killer.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 目标服务 —— 核心业务层
 *
 * 职责：
 * 1. 调用 DeepSeek AI 拆解用户目标
 * 2. 解析 AI 返回的 JSON 并存入数据库
 * 3. 提供层级查询（目标 + 子任务）
 * 4. 目标列表查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final ChatClient chatClient;
    private final GoalMapper goalMapper;
    private final TaskMapper taskMapper;
    private final CurrentUserHelper currentUserHelper;
    private final ObjectMapper objectMapper;

    /**
     * 创建目标（核心入库逻辑）
     *
     * 流程：
     * 1. 接收用户输入的目标内容
     * 2. 构建 Prompt 调用 DeepSeek AI 拆解
     * 3. 解析 AI 返回的 JSON
     * 4. 事务性保存 goal（父表） + task[]（子表）
     * 5. 返回完整的目标详情（含子任务列表）
     *
     * @param request 目标创建请求
     * @return 目标详情视图
     */
    @Transactional(rollbackFor = Exception.class)
    public GoalDetailVO createGoal(GoalCreateRequest request) {
        Long userId = currentUserHelper.getCurrentUserId();
        log.info("用户 {} 创建目标: {}", userId, request.getContent());

        // ============ Step 1: 调用 DeepSeek AI 拆解目标 ============
        String aiResponse = callDeepSeekAI(request.getContent());
        log.debug("AI 原始返回: {}", aiResponse);

        // ============ Step 2: 清洗并解析 JSON ============
        String cleanJson = extractJson(aiResponse);
        Map<String, Object> aiResult = parseAIResponse(cleanJson);

        String goalTitle = (String) aiResult.getOrDefault("goalTitle", request.getContent());
        String goalDescription = (String) aiResult.getOrDefault("goalDescription", "");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> taskList = (List<Map<String, Object>>) aiResult.getOrDefault("tasks", new ArrayList<>());

        // ============ Step 3: 保存目标（父表） ============
        Goal goal = Goal.builder()
                .userId(userId)
                .title(goalTitle)
                .description(goalDescription)
                .status("进行中")
                .progress(0)
                .taskTotal(taskList.size())
                .taskCompleted(0)
                .aiRawResponse(aiResponse)
                .build();
        goalMapper.insert(goal);
        log.info("目标已保存: goalId={}, title={}, taskTotal={}", goal.getId(), goal.getTitle(), taskList.size());

        // ============ Step 4: 批量保存子任务 ============
        List<Task> tasks = new ArrayList<>();
        for (Map<String, Object> taskMap : taskList) {
            Task task = Task.builder()
                    .goalId(goal.getId())
                    .content((String) taskMap.get("content"))
                    .status("待办")
                    .sortOrder(taskMap.get("sortOrder") != null
                            ? ((Number) taskMap.get("sortOrder")).intValue()
                            : tasks.size() + 1)
                    .build();
            tasks.add(task);
        }

        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                taskMapper.insert(task);
            }
            log.info("{} 个子任务已保存到 goalId={}", tasks.size(), goal.getId());
        }

        // ============ Step 5: 构建并返回层级视图 ============
        return buildGoalDetailVO(goal, tasks);
    }

    /**
     * 获取当前用户的所有目标列表
     */
    public List<GoalListItemVO> listUserGoals() {
        Long userId = currentUserHelper.getCurrentUserId();

        List<Goal> goals = goalMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Goal>()
                        .eq(Goal::getUserId, userId)
                        .orderByDesc(Goal::getCreatedAt));

        return goals.stream().map(this::toListItemVO).collect(Collectors.toList());
    }

    /**
     * 根据目标ID获取包含子任务详情的层级视图
     *
     * @param goalId 目标ID
     * @return 目标详情（含子任务列表）
     */
    public GoalDetailVO getGoalDetail(Long goalId) {
        Long userId = currentUserHelper.getCurrentUserId();

        // 1. 查询目标，校验所属用户
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null) {
            throw new RuntimeException("目标不存在");
        }
        if (!goal.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该目标");
        }

        // 2. 查询该目标下的所有子任务
        List<Task> tasks = taskMapper.selectByGoalId(goalId);

        // 3. 构建层级视图
        return buildGoalDetailVO(goal, tasks);
    }

    // ==================== 私有方法 ====================

    /**
     * 调用 DeepSeek AI
     */
    private String callDeepSeekAI(String userGoal) {
        try {
            return chatClient.prompt()
                    .system(AIPromptTemplates.SYSTEM_PROMPT)
                    .user(AIPromptTemplates.buildUserPrompt(userGoal))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("DeepSeek AI 调用失败", e);
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试: " + e.getMessage());
        }
    }

    /**
     * 从 AI 返回中提取纯 JSON 字符串
     * 处理 AI 可能包裹的 Markdown 代码块标记
     */
    private String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new RuntimeException("AI 返回内容为空");
        }

        String trimmed = rawResponse.trim();

        // 去除可能的 Markdown 代码块标记 ```json ... ```
        if (trimmed.startsWith("```")) {
            int startIdx = trimmed.indexOf("\n");
            if (startIdx > 0) {
                trimmed = trimmed.substring(startIdx + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
            }
        }

        // 尝试找到 JSON 的起始和结束位置
        int jsonStart = trimmed.indexOf("{");
        int jsonEnd = trimmed.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return trimmed.substring(jsonStart, jsonEnd + 1);
        }

        throw new RuntimeException("AI 返回内容无法解析为 JSON: " + trimmed.substring(0, Math.min(200, trimmed.length())));
    }

    /**
     * 解析 JSON 为 Map
     */
    private Map<String, Object> parseAIResponse(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("JSON 解析失败，原始内容: {}", json);
            throw new RuntimeException("AI 返回的 JSON 格式异常: " + e.getMessage());
        }
    }

    /**
     * 构建目标详情视图
     */
    private GoalDetailVO buildGoalDetailVO(Goal goal, List<Task> tasks) {
        List<TaskVO> taskVOs = tasks.stream()
                .map(task -> TaskVO.builder()
                        .id(task.getId())
                        .content(task.getContent())
                        .status(task.getStatus())
                        .sortOrder(task.getSortOrder())
                        .completedAt(task.getCompletedAt())
                        .createdAt(task.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return GoalDetailVO.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .status(goal.getStatus())
                .progress(goal.getProgress())
                .taskTotal(goal.getTaskTotal())
                .taskCompleted(goal.getTaskCompleted())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .tasks(taskVOs)
                .build();
    }

    /**
     * 目标实体 → 列表项 VO
     */
    private GoalListItemVO toListItemVO(Goal goal) {
        return GoalListItemVO.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .status(goal.getStatus())
                .progress(goal.getProgress())
                .taskTotal(goal.getTaskTotal())
                .taskCompleted(goal.getTaskCompleted())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
