package yimeng.pojo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipant {
    private Integer id;
    private Integer activityId;
    private Integer userId;
    private LocalDateTime joinedAt;
    private String paymentStatus; // 'not_paid', 'paid'
    private Integer paymentAmount;
}