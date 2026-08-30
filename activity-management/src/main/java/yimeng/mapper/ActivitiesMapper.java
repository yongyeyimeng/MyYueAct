package yimeng.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import yimeng.pojo.Activities;
import yimeng.pojo.ActivitiesQueryParam;

@Mapper
public interface ActivitiesMapper {

    //新增活动
    @Insert("insert into activities(at_name,time,location,content,promoter,created_at,updated_at,num)" +
            "values (#{atName},#{time},#{location},#{content},#{promoter},#{createdAt},#{updatedAt},#{num})")
    void insert(Activities activities);

    //更新活动
    @Update("update activities set at_name=#{atName},time=#{time},location=#{location}," +
            "content=#{content},num=#{num},updated_at=#{updatedAt} where id=#{id}")
    void update(Activities activities);

    // 删除活动
    @Delete("delete from activities where id=#{id}")
    void deleteById(Integer id);

    //条件查询活动
    public List<Activities> list(ActivitiesQueryParam activitiesQueryParam);
    
    // 查询用户参与的活动
    @Select("SELECT a.*, u.nickname FROM activities a " +
            "JOIN activity_participants ap ON a.id = ap.activity_id " +
            "LEFT JOIN users u ON a.promoter = u.id " +
            "WHERE ap.user_id = #{userId} " +
            "ORDER BY a.id DESC")
    List<Activities> listJoinedActivities(Integer userId);
    
    // 设置活动费用
    @Update("UPDATE activities SET a_price = #{price} WHERE id = #{id}")
    void setActivityPrice(Integer id, Integer price);
}