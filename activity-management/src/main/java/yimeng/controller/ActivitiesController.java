package yimeng.controller;

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

import lombok.extern.slf4j.Slf4j;
import yimeng.pojo.Activities;
import yimeng.pojo.ActivitiesQueryParam;
import yimeng.pojo.PageResult;
import yimeng.pojo.Result;
import yimeng.pojo.ActivityParticipant;
import yimeng.service.ActivitiesService;

@Slf4j
@RequestMapping("/activities")
@RestController
@CrossOrigin(origins = "*")

public class ActivitiesController {

    @Autowired
    
    private ActivitiesService activitiesService;

    @PostMapping("/add")
    
    public Result save(@RequestBody Activities activities){
        log.info("新活:{}",activities);
        activitiesService.save(activities);
        return Result.success();
    }

//    分页查询
    @GetMapping("/show")
    
    public Result page(ActivitiesQueryParam activitiesQueryParam){
    log.info("分页查询：{}",activitiesQueryParam);
    
    PageResult<Activities> pageResult = activitiesService.page(activitiesQueryParam);
    return Result.success(pageResult);
    }
    
    // 查询用户参与的活动
    @GetMapping("/joined")
    public Result getJoinedActivities(@RequestParam Integer userId) {
        log.info("查询用户参与的活动：{}", userId);
        
        // 你需要在这里实现关联activity_participants表的查询逻辑
        // 这将返回用户参与的活动列表
        
        PageResult<Activities> pageResult = activitiesService.getJoinedActivities(userId);
        return Result.success(pageResult);
    }
    
    // 更新活动信息
    @PutMapping("/update")
    public Result updateActivity(@RequestBody Activities activity) {
        log.info("更新活动:{}", activity);
        
        try {
            activitiesService.update(activity);
            return Result.success("活动更新成功");
        } catch (Exception e) {
            log.error("更新活动失败", e);
            return Result.error("活动更新失败");
        }
    }
    
    // 删除活动
    @DeleteMapping("/delete")
    public Result deleteActivity(@RequestParam Integer id) {
        log.info("删除活动，ID:{}", id);
        
        try {
            activitiesService.deleteById(id);
            return Result.success("活动删除成功");
        } catch (Exception e) {
            log.error("删除活动失败", e);
            return Result.error("活动删除失败");
        }
    }
    
    // 用户参与活动
    @PostMapping("/join")
    public Result joinActivity(@RequestBody ActivityParticipant participant) {
        log.info("用户参与活动：{}", participant);
        
        // 检查用户是否已参与该活动
        if (activitiesService.isUserJoinedActivity(participant.getActivityId(), participant.getUserId())) {
            return Result.error("您已参与该活动");
        }
        
        try {
            activitiesService.joinActivity(participant);
            return Result.success("参与活动成功");
        } catch (Exception e) {
            log.error("参与活动失败", e);
            return Result.error("参与活动失败");
        }
    }
    
    // 用户退出活动
    @PostMapping("/quit")
    public Result quitActivity(@RequestBody ActivityParticipant participant) {
        log.info("用户退出活动：{}", participant);
        
        try {
            activitiesService.quitActivity(participant);
            return Result.success("退出活动成功");
        } catch (Exception e) {
            log.error("退出活动失败", e);
            return Result.error("退出活动失败");
        }
    }
    
    // 设置活动费用
    @PutMapping("/setPrice")
    public Result setActivityPrice(@RequestParam Integer id, @RequestParam Integer price) {
        log.info("设置活动费用，ID:{}, 价格:{}", id, price);
        
        try {
            activitiesService.setActivityPrice(id, price);
            return Result.success("费用设置成功");
        } catch (Exception e) {
            log.error("费用设置失败", e);
            return Result.error("费用设置失败");
        }
    }
}
