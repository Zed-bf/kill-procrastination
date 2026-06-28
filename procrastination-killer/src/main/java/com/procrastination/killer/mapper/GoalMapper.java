package com.procrastination.killer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.procrastination.killer.entity.Goal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 目标 Mapper
 */
@Mapper
public interface GoalMapper extends BaseMapper<Goal> {

    /**
     * 原子更新目标进度和状态
     * 当所有任务完成时，将目标状态更新为"已完成"
     */
    @Update("UPDATE goal SET progress = #{progress}, task_completed = #{taskCompleted}, " +
            "status = CASE WHEN #{taskCompleted} >= task_total THEN '已完成' ELSE status END, " +
            "updated_at = NOW() WHERE id = #{goalId}")
    int updateProgressAndStatus(@Param("goalId") Long goalId,
                                @Param("progress") Integer progress,
                                @Param("taskCompleted") Integer taskCompleted);
}
