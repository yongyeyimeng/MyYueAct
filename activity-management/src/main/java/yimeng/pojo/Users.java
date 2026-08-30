package yimeng.pojo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 修改类名首字母大写
public class Users {
    private Integer id;
    private String openid;
    private String nickname;
    private String phone;
    private String role;
    private String banReason;
    private LocalDateTime bannedUntil;
    private Boolean banned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
