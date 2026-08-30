package yimeng.pojo;

public class LoginInfo {
    private String token;
    private Users userInfo;

    // 构造函数
    public LoginInfo() {}

    public LoginInfo(String token, Users userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    // Getter和Setter方法
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Users getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Users userInfo) {
        this.userInfo = userInfo;
    }
}