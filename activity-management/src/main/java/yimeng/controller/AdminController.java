package yimeng.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import yimeng.mapper.UserMapper;
import yimeng.pojo.Activities;
import yimeng.pojo.ActivitiesQueryParam;
import yimeng.pojo.BanUserRequest;
import yimeng.pojo.Result;
import yimeng.pojo.Users;
import yimeng.service.ActivitiesService;
import yimeng.service.AdminService;
import yimeng.service.DrawService;

@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ActivitiesService activitiesService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DrawService drawService;

    @GetMapping("/activities/list")
    public Result listActivities(ActivitiesQueryParam param) {
        return Result.success(activitiesService.page(param));
    }

    @PutMapping("/activities/update")
    public Result updateActivity(@RequestBody Activities activity) {
        try {
            activitiesService.update(activity);
            return Result.success("活动更新成功");
        } catch (Exception e) {
            log.error("管理员更新活动失败", e);
            return Result.error("活动更新失败");
        }
    }

    @DeleteMapping("/activities/delete")
    public Result deleteActivity(@RequestParam Integer id) {
        try {
            activitiesService.deleteById(id);
            return Result.success("活动删除成功");
        } catch (Exception e) {
            log.error("管理员删除活动失败", e);
            return Result.error("活动删除失败");
        }
    }

    @PutMapping("/activities/setPrice")
    public Result setActivityPrice(@RequestParam Integer id, @RequestParam Integer price) {
        try {
            activitiesService.setActivityPrice(id, price);
            return Result.success("费用设置成功");
        } catch (Exception e) {
            log.error("管理员设置费用失败", e);
            return Result.error("费用设置失败");
        }
    }

    @GetMapping("/users/list")
    public Result listUsers(@RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminService.searchUsers(page, pageSize, keyword));
    }

    @PostMapping("/users/ban")
    public Result banUser(@RequestBody BanUserRequest request, HttpServletRequest httpRequest) {
        if (request.getUserId() == null) {
            return Result.error("请选择要封禁的用户");
        }
        Users target = userMapper.selectUserById(request.getUserId());
        if (target == null) {
            return Result.error("用户不存在");
        }
        if ("admin".equals(target.getRole())) {
            return Result.error("不能封禁管理员");
        }
        Integer adminId = (Integer) httpRequest.getAttribute("adminUserId");
        adminService.banUser(adminId, request);
        return Result.success("封禁成功");
    }

    @PostMapping("/users/unban")
    public Result unbanUser(@RequestBody Map<String, Integer> body) {
        Integer userId = body.get("userId");
        if (userId == null) {
            return Result.error("请选择要解封的用户");
        }
        adminService.unbanUser(userId);
        return Result.success("解封成功");
    }

    @GetMapping("/draws/list")
    public Result listDraws(@RequestParam Integer userId,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(drawService.listDrawsByUser(userId, page, pageSize));
    }

    @GetMapping("/draws/detail")
    public Result drawDetail(@RequestParam Integer drawId, HttpServletRequest request) {
        try {
            Integer adminId = (Integer) request.getAttribute("adminUserId");
            return Result.success(drawService.getDrawDetail(drawId, adminId, true));
        } catch (Exception e) {
            log.error("管理员查看抽签失败", e);
            return Result.error(e.getMessage() == null ? "获取抽签失败" : e.getMessage());
        }
    }

    @PostMapping("/draws/dissolve")
    public Result dissolveDraw(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Integer drawId = body.get("drawId") instanceof Number
                    ? ((Number) body.get("drawId")).intValue() : null;
            if (drawId == null) {
                return Result.error("请选择要解散的抽签");
            }
            Integer adminId = (Integer) request.getAttribute("adminUserId");
            drawService.dissolveDraw(drawId, adminId, true);
            return Result.success("抽签已解散");
        } catch (Exception e) {
            log.error("管理员解散抽签失败", e);
            return Result.error(e.getMessage() == null ? "解散抽签失败" : e.getMessage());
        }
    }
}
