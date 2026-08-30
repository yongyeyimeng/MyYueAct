package yimeng.pojo;

import lombok.Data;

@Data
public class ActivitiesQueryParam {
    private Integer page = 1;
    private Integer pageSize=5;
    private String atName;
    private Integer promoterId; // 添加根据发起人ID查询的字段
}