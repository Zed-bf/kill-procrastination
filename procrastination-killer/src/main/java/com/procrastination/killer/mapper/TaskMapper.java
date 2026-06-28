package com.procrastination.killer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.procrastination.killer.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 任务 Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 统计指定目标下已完成的任务数
     */
    @Select("SELECT COUNT(*) FROM task WHERE goal_id = #{goalId} AND status = '已完成' AND deleted = 0")
    int countCompletedByGoalId(@Param("goalId") Long goalId);

    /**
     * 统计指定目标下的任务总数
     */
    @Select("SELECT COUNT(*) FROM task WHERE goal_id = #{goalId} AND deleted = 0")
    int countTotalByGoalId(@Param("goalId") Long goalId);

    /**
     * 查询指定目标下所有未删除的任务，按排序序号升序
     */
    @Select("SELECT * FROM task WHERE goal_id = #{goalId} AND deleted = 0 ORDER BY sort_order ASC")
    java.util.List<Task> selectByGoalId(@Param("goalId") Long goalId);
}
