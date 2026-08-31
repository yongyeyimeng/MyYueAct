package yimeng.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import yimeng.util.JwtUtil;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void initJwtSecret() {
        JwtUtil.init(secret);
    }
}
