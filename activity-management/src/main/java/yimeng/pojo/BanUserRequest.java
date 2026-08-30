package yimeng.pojo;

import lombok.Data;

@Data
public class BanUserRequest {
    private Integer userId;
    private String reason;
    private Integer duration;
    private String durationUnit;
}
