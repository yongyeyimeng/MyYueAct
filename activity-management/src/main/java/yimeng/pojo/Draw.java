package yimeng.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Draw {
    private Integer id;
    private String content;
    private Integer peopleCount;
    private String visibility;
    private String inviteCode;
    private Integer creatorId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime drawnAt;
    private Integer participantCount;
    private List<DrawParticipant> participants;
    private List<DrawItem> items;
}
