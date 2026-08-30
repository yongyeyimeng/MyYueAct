package yimeng.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DrawParticipant {
    private Integer id;
    private Integer drawId;
    private Integer userId;
    private Integer result;
    private Integer drawItemId;
    private String itemName;
    private LocalDateTime joinedAt;
    private String nickname;
}
