package yimeng.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import yimeng.pojo.DrawParticipant;

import java.util.List;

@Mapper
public interface DrawParticipantMapper {

    @Insert("INSERT INTO draw_participants(draw_id, user_id, draw_item_id, joined_at) VALUES(#{drawId}, #{userId}, #{drawItemId}, NOW())")
    int insert(DrawParticipant participant);

    @Select("SELECT p.id, p.draw_id as drawId, p.user_id as userId, p.result, p.draw_item_id as drawItemId, " +
            "di.name as itemName, p.joined_at as joinedAt " +
            "FROM draw_participants p LEFT JOIN draw_items di ON p.draw_item_id = di.id " +
            "WHERE p.draw_id = #{drawId} AND p.user_id = #{userId}")
    DrawParticipant selectByDrawAndUser(Integer drawId, Integer userId);

    @Select("SELECT COUNT(*) FROM draw_participants WHERE draw_id = #{drawId}")
    int countByDrawId(Integer drawId);

    @Select("SELECT p.id, p.draw_id as drawId, p.user_id as userId, p.result, p.draw_item_id as drawItemId, " +
            "di.name as itemName, p.joined_at as joinedAt, u.nickname " +
            "FROM draw_participants p LEFT JOIN users u ON p.user_id = u.id " +
            "LEFT JOIN draw_items di ON p.draw_item_id = di.id " +
            "WHERE p.draw_id = #{drawId} ORDER BY p.joined_at ASC, p.id ASC")
    List<DrawParticipant> selectByDrawIdWithNickname(Integer drawId);

    @Update("UPDATE draw_participants SET draw_item_id = #{drawItemId} WHERE id = #{id}")
    int updateDrawItemId(DrawParticipant participant);
}
