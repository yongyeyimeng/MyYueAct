package yimeng.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import yimeng.pojo.Draw;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DrawMapper {

    @Insert("INSERT INTO draws(content, people_count, visibility, invite_code, creator_id, status, expires_at) " +
            "VALUES(#{content}, #{peopleCount}, #{visibility}, #{inviteCode}, #{creatorId}, 'open', #{expiresAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Draw draw);

    @Select("SELECT id, content, people_count as peopleCount, visibility, invite_code as inviteCode, " +
            "creator_id as creatorId, status, created_at as createdAt, expires_at as expiresAt, drawn_at as drawnAt " +
            "FROM draws WHERE invite_code = #{inviteCode}")
    Draw selectByInviteCode(String inviteCode);

    @Select("SELECT id, content, people_count as peopleCount, visibility, invite_code as inviteCode, " +
            "creator_id as creatorId, status, created_at as createdAt, expires_at as expiresAt, drawn_at as drawnAt " +
            "FROM draws WHERE id = #{id}")
    Draw selectById(Integer id);

    @Select("SELECT d.id, d.content, d.people_count as peopleCount, d.visibility, d.invite_code as inviteCode, " +
            "d.creator_id as creatorId, d.status, d.created_at as createdAt, d.expires_at as expiresAt, d.drawn_at as drawnAt, " +
            "(SELECT COUNT(*) FROM draw_participants dp WHERE dp.draw_id = d.id) AS participantCount " +
            "FROM draws d " +
            "WHERE d.creator_id = #{userId} OR d.id IN (SELECT dp2.draw_id FROM draw_participants dp2 WHERE dp2.user_id = #{userId}) " +
            "ORDER BY d.id DESC")
    List<Draw> selectByUserId(Integer userId);

    @Update("UPDATE draws SET status = #{status}, drawn_at = #{drawnAt} WHERE id = #{id}")
    int updateStatus(Draw draw);

    @Delete("DELETE FROM draws WHERE id = #{id}")
    int deleteById(Integer id);

    @Delete("DELETE FROM draws WHERE status = 'drawn' AND drawn_at < #{cutoff}")
    int deleteDrawnBefore(LocalDateTime cutoff);
}
