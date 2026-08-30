package yimeng.service;

import yimeng.pojo.BanUserRequest;
import yimeng.pojo.PageResult;
import yimeng.pojo.Users;

public interface AdminService {

    PageResult<Users> searchUsers(Integer page, Integer pageSize, String keyword);

    void banUser(Integer adminId, BanUserRequest request);

    void unbanUser(Integer userId);
}
