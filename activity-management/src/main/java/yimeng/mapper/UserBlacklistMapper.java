package yimeng.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import yimeng.pojo.UserBlacklist;

@Mapper
public interface UserBlacklistMapper {

    @Insert("INSERT INTO user_blacklist(user_id, blacklisted_user_id, reason, banned_until, created_at, updated_at) " +
            "VALUES(#{userId}, #{blacklistedUserId}, #{reason}, #{bannedUntil}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE reason=VALUES(reason), banned_until=VALUES(banned_until), updated_at=NOW()")
    int upsert(UserBlacklist blacklist);

    @Select("SELECT id, user_id as userId, blacklisted_user_id as blacklistedUserId, reason, " +
            "banned_until as bannedUntil, created_at as createdAt, updated_at as updatedAt " +
            "FROM user_blacklist " +
            "WHERE blacklisted_user_id = #{userId} AND (banned_until IS NULL OR banned_until > NOW()) " +
            "ORDER BY id DESC LIMIT 1")
    UserBlacklist selectActiveBanByUserId(Integer userId);

    @Delete("DELETE FROM user_blacklist WHERE blacklisted_user_id = #{userId}")
    int deleteByBlacklistedUserId(Integer userId);
}
