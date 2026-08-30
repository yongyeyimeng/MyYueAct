package yimeng.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yimeng.pojo.Users;

@Mapper
public interface UserMapper {
    
    Logger logger = LoggerFactory.getLogger(UserMapper.class);

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO users(openid, nickname, phone, created_at, updated_at) " +
            "VALUES(#{openid}, #{nickname}, #{phone}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(Users user);

    /**
     * 根据ID更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    @Update("UPDATE users SET nickname=#{nickname}, phone=#{phone}, updated_at=#{updatedAt} WHERE id=#{id}")
    int updateUserById(Users user);

    /**
     * 根据openid更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    @Update("UPDATE users SET nickname=#{nickname}, phone=#{phone}, updated_at=#{updatedAt} WHERE openid=#{openid}")
    int updateUserByOpenid(Users user);

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Select("SELECT id, openid, nickname, phone, role, created_at as createdAt, updated_at as updatedAt FROM users WHERE id = #{id}")
    Users selectUserById(Integer id);

    /**
     * 根据openid查找用户
     * @param openid 微信openid
     * @return 用户对象
     */
    @Select("SELECT id, openid, nickname, phone, role, created_at as createdAt, updated_at as updatedAt FROM users WHERE openid = #{openid}")
    Users selectUserByOpenid(String openid);

    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户对象
     */
    @Select("SELECT id, openid, nickname, phone, role, created_at as createdAt, updated_at as updatedAt FROM users WHERE phone = #{phone}")
    Users selectUserByPhone(String phone);

    /**
     * 获取所有用户
     * @return 用户列表
     */
    @Select("SELECT id, openid, nickname, phone, role, created_at as createdAt, updated_at as updatedAt FROM users")
    List<Users> selectAllUsers();

    /**
     * 管理员搜索普通用户，同时带出当前封禁信息
     * @param keyword 昵称或用户ID
     * @return 用户列表
     */
    @Select("<script>" +
            "SELECT u.id, u.openid, u.nickname, u.phone, u.role, u.created_at as createdAt, u.updated_at as updatedAt, " +
            "b.reason AS banReason, b.banned_until AS bannedUntil, (b.id IS NOT NULL) AS banned " +
            "FROM users u " +
            "LEFT JOIN user_blacklist b ON b.blacklisted_user_id = u.id AND (b.banned_until IS NULL OR b.banned_until > NOW()) " +
            "WHERE u.role = 'user' " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (u.id = #{keyword} OR u.nickname LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "ORDER BY u.id DESC" +
            "</script>")
    List<Users> selectUsersForAdmin(@Param("keyword") String keyword);

    /**
     * 根据openid删除用户
     * @param openid 微信openid
     * @return 影响的行数
     */
    @Delete("DELETE FROM users WHERE openid = #{openid}")
    int deleteUserByOpenid(String openid);
}
