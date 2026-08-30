package yimeng.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBlacklist {
    private Integer id;
    private Integer userId;
    private Integer blacklistedUserId;
    private String reason;
    private LocalDateTime bannedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
