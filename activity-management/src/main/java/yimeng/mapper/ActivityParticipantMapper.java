package yimeng.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import yimeng.pojo.ActivityParticipant;

@Mapper
public interface ActivityParticipantMapper {
    
    // 插入活动参与记录
    @Insert("INSERT INTO activity_participants(activity_id, user_id, joined_at, payment_status) " +
            "VALUES(#{activityId}, #{userId}, #{joinedAt}, COALESCE(#{paymentStatus}, 'not_paid'))")
    void insert(ActivityParticipant participant);
    
    // 删除活动参与记录
    @Delete("DELETE FROM activity_participants WHERE activity_id = #{activityId} AND user_id = #{userId}")
    void deleteByActivityIdAndUserId(Integer activityId, Integer userId);
    
    // 检查用户是否已参与某个活动
    @Select("SELECT COUNT(*) FROM activity_participants WHERE activity_id = #{activityId} AND user_id = #{userId}")
    int countByActivityIdAndUserId(Integer activityId, Integer userId);
}