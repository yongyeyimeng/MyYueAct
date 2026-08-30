package yimeng.service;

import yimeng.pojo.Users;

public interface AuthService {

    /**
     * 处理微信登录
     * @param user 微信用户信息
     * @return 登录后的用户信息
     */
    Users processWeChatLogin(Users user);

    /**
     * 根据用户ID查找用户
     * @param userId 用户ID
     * @return 用户信息
     */
    Users findUserById(Integer userId);

    /**
     * 根据openid查找用户
     * @param openid 微信openid
     * @return 用户信息
     */
    Users findUserByOpenid(String openid);
    
    /**
     * 根据手机号查找用户
     * @param phone 手机号
     * @return 用户信息
     */
    Users findUserByPhone(String phone);
}