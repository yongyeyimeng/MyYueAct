package yimeng.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import yimeng.mapper.UserBlacklistMapper;
import yimeng.mapper.UserMapper;
import yimeng.pojo.BanUserRequest;
import yimeng.pojo.PageResult;
import yimeng.pojo.UserBlacklist;
import yimeng.pojo.Users;
import yimeng.service.AdminService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBlacklistMapper userBlacklistMapper;

    @Override
    public PageResult<Users> searchUsers(Integer page, Integer pageSize, String keyword) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        PageHelper.startPage(page, pageSize);
        List<Users> users = userMapper.selectUsersForAdmin(keyword);
        Page<Users> p = (Page<Users>) users;
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public void banUser(Integer adminId, BanUserRequest request) {
        LocalDateTime bannedUntil = null;
        if (request.getDuration() != null && request.getDuration() > 0) {
            if ("hour".equalsIgnoreCase(request.getDurationUnit())) {
                bannedUntil = LocalDateTime.now().plusHours(request.getDuration());
            } else if ("day".equalsIgnoreCase(request.getDurationUnit())) {
                bannedUntil = LocalDateTime.now().plusDays(request.getDuration());
            }
        }

        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setUserId(adminId);
        blacklist.setBlacklistedUserId(request.getUserId());
        blacklist.setReason(request.getReason());
        blacklist.setBannedUntil(bannedUntil);
        userBlacklistMapper.upsert(blacklist);
    }

    @Override
    public void unbanUser(Integer userId) {
        userBlacklistMapper.deleteByBlacklistedUserId(userId);
    }
}
