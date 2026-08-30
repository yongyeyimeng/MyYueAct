package yimeng.pojo;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Activities {
    private Integer id;
    private String atName;
    private String time;
    private String location;
    private String content;
    private Integer promoter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer num;
    private Integer aPrice;

    //活动人员
    private String[] participants;
    //负责人昵称
    private String nickname;

}