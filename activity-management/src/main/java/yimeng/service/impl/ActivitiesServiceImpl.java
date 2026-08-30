package yimeng.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import yimeng.mapper.ActivitiesMapper;
import yimeng.mapper.ActivityParticipantMapper;
import yimeng.pojo.ActivitiesQueryParam;
import yimeng.pojo.PageResult;
import yimeng.pojo.Activities;
import yimeng.pojo.ActivityParticipant;
import yimeng.service.ActivitiesService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivitiesServiceImpl implements ActivitiesService {

    private final ActivitiesMapper activitiesMapper;
    private final ActivityParticipantMapper activityParticipantMapper;
    
    // 分页查询
    @Override
    public PageResult<Activities> page(ActivitiesQueryParam activitiesQueryParam){
        PageHelper.startPage(activitiesQueryParam.getPage(),activitiesQueryParam.getPageSize());

        List<Activities> activitiesList = activitiesMapper.list(activitiesQueryParam);
        Page<Activities> p = (Page<Activities>) activitiesList;
        return new PageResult<Activities>(p.getTotal(),p.getResult());
    }

    public ActivitiesServiceImpl(ActivitiesMapper activitiesMapper, ActivityParticipantMapper activityParticipantMapper) {
        this.activitiesMapper = activitiesMapper;
        this.activityParticipantMapper = activityParticipantMapper;
    }

    @Override
    public void save(Activities activities) {
        activities.setCreatedAt(LocalDateTime.now());
        activities.setUpdatedAt(LocalDateTime.now());
        activitiesMapper.insert(activities);
    }
    
    @Override
    public void update(Activities activities) {
        activities.setUpdatedAt(LocalDateTime.now());
        activitiesMapper.update(activities);
    }
    
    @Override
    public void deleteById(Integer id) {
        activitiesMapper.deleteById(id);
    }
    
    @Override
    public PageResult<Activities> getJoinedActivities(Integer userId) {
        // 使用PageHelper进行分页
        PageHelper.startPage(1, 100); // 默认显示前100条记录
        
        // 查询用户参与的活动列表
        List<Activities> activitiesList = activitiesMapper.listJoinedActivities(userId);
        
        // 封装分页结果
        Page<Activities> p = (Page<Activities>) activitiesList;
        return new PageResult<Activities>(p.getTotal(), p.getResult());
    }
    
    @Override
    public void joinActivity(ActivityParticipant participant) {
        participant.setJoinedAt(LocalDateTime.now());
        if (participant.getPaymentStatus() == null) {
            participant.setPaymentStatus("not_paid");
        }
        activityParticipantMapper.insert(participant);
    }
    
    @Override
    public void quitActivity(ActivityParticipant participant) {
        activityParticipantMapper.deleteByActivityIdAndUserId(participant.getActivityId(), participant.getUserId());
    }
    
    @Override
    public boolean isUserJoinedActivity(Integer activityId, Integer userId) {
        return activityParticipantMapper.countByActivityIdAndUserId(activityId, userId) > 0;
    }
    
    @Override
    public void setActivityPrice(Integer id, Integer price) {
        activitiesMapper.setActivityPrice(id, price);
    }
}