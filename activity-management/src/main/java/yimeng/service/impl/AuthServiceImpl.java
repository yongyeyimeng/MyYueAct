package yimeng.service.impl;

import yimeng.mapper.UserMapper;
import yimeng.pojo.Users;
import yimeng.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 处理微信登录
     * @param user 微信用户信息
     * @return 登录后的用户信息
     */
    @Override
    public Users processWeChatLogin(Users user) {
        try {
            // 不记录 openid 等敏感标识符
            logger.info("开始处理微信登录");
            
            // 首先根据openid检查用户是否已经存在
            Users existingUser = userMapper.selectUserByOpenid(user.getOpenid());

            if (existingUser != null) {
                // 用户已存在，更新用户信息
                logger.info("用户已存在，执行资料更新");
                existingUser.setNickname(user.getNickname());
                existingUser.setPhone(user.getPhone());
                existingUser.setUpdatedAt(LocalDateTime.now());
                
                int result = userMapper.updateUserByOpenid(existingUser);
                logger.debug("更新用户影响行数: {}", result);
                
                return existingUser;
            } else {
                // 新用户，保存到数据库
                logger.info("新用户，创建用户记录");
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                
                int result = userMapper.insertUser(user);
                logger.debug("插入用户影响行数: {}", result);
                
                return user;
            }
        } catch (Exception e) {
            logger.error("处理微信登录时发生错误: ", e);
            throw e;
        }
    }

    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public Users findUserById(Integer userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 根据openid查找用户
     * @param openid 微信openid
     * @return 用户信息
     */
    @Override
    public Users findUserByOpenid(String openid) {
        return userMapper.selectUserByOpenid(openid);
    }
    
    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Override
    public Users findUserByPhone(String phone) {
        return userMapper.selectUserByPhone(phone);
    }
}