package yimeng.service;

import yimeng.pojo.ActivitiesQueryParam;
import yimeng.pojo.PageResult;
// 修改引用的类名
import yimeng.pojo.Activities;
import yimeng.pojo.ActivityParticipant;

// 修改接口名首字母大写
public interface ActivitiesService {

    //新增活动
    // 修改参数类型
    void save(Activities activities);

    //分页查询
    // 修改返回类型
    PageResult<Activities> page(ActivitiesQueryParam activitiesQueryParam);
    
    // 更新活动
    void update(Activities activities);
    
    // 删除活动
    void deleteById(Integer id);
    
    // 查询用户参与的活动
    PageResult<Activities> getJoinedActivities(Integer userId);
    
    // 用户参与活动
    void joinActivity(ActivityParticipant participant);
    
    // 用户退出活动
    void quitActivity(ActivityParticipant participant);
    
    // 检查用户是否已参与活动
    boolean isUserJoinedActivity(Integer activityId, Integer userId);
    
    // 设置活动费用
    void setActivityPrice(Integer id, Integer price);
}