package yimeng.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import yimeng.mapper.UserBlacklistMapper;
import yimeng.pojo.LoginInfo;
import yimeng.pojo.Result;
import yimeng.pojo.UserBlacklist;
import yimeng.pojo.Users;
import yimeng.service.AuthService;
import yimeng.util.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // 允许跨域请求
public class

AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // 微信小程序配置
    @Value("${wechat.appid}")
    private String appid;
    
    @Value("${wechat.secret}")
    private String secret;
    
    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private AuthService authService;

    @Autowired
    private UserBlacklistMapper userBlacklistMapper;

    /**
     * 微信登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody Map<String, Object> loginData) {
        try {
            logger.info("开始处理微信登录请求");
            
            // 从请求数据中获取参数
            String code = (String) loginData.get("code");
            String nickname = (String) loginData.get("nickname");
            String phone = (String) loginData.get("phone");

            // 验证必填参数
            if (code == null || code.isEmpty()) {
                logger.warn("登录请求缺少微信登录凭证");
                return ResponseEntity.badRequest().body(Result.error("缺少微信登录凭证"));
            }

            if (nickname == null || nickname.isEmpty()) {
                logger.warn("登录请求缺少昵称");
                return ResponseEntity.badRequest().body(Result.error("请输入昵称"));
            }

            if (phone == null || phone.isEmpty()) {
                logger.warn("登录请求缺少手机号");
                return ResponseEntity.badRequest().body(Result.error("请输入手机号"));
            }
            
            // 调用微信接口获取 openid
            RestTemplate restTemplate = new RestTemplate();
            String url = WECHAT_LOGIN_URL + "?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
            // 仅记录调用外部服务的动作，不记录完整URL与敏感参数
            logger.info("调用微信登录接口");
            
            // 获取字符串响应
            String responseString = restTemplate.getForObject(url, String.class);
            
            // 解析JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> wechatResponse = objectMapper.readValue(responseString, Map.class);
            
            if (wechatResponse == null) {
                logger.error("微信登录失败: 无法获取响应");
                return ResponseEntity.status(500).body(Result.error("微信登录失败"));
            }
            
            if (wechatResponse.containsKey("errcode") && wechatResponse.get("errcode") != null) {
                logger.error("微信登录失败: errcode={}, errmsg={}", 
                    wechatResponse.get("errcode"), 
                    wechatResponse.get("errmsg"));
                return ResponseEntity.status(500).body(Result.error("微信登录失败"));
            }
            
            String openid = (String) wechatResponse.get("openid");
            if (openid == null || openid.isEmpty()) {
                logger.error("微信登录失败: 无法获取openid");
                return ResponseEntity.status(500).body(Result.error("微信登录失败"));
            }
            
            // 不记录 openid 等敏感标识符

            // 创建用户对象
            Users user = new Users();
            user.setOpenid(openid);
            user.setNickname(nickname);
            user.setPhone(phone);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // 不在日志中记录手机号

            // 处理用户登录逻辑
            Users loggedInUser = authService.processWeChatLogin(user);

            UserBlacklist activeBan = userBlacklistMapper.selectActiveBanByUserId(loggedInUser.getId());
            if (activeBan != null) {
                logger.warn("被封禁用户尝试登录，用户ID: {}", loggedInUser.getId());
                Result banResult = Result.error("账号已被封禁，请联系管理员", -1);
                banResult.setData(buildBanData(activeBan));
                return ResponseEntity.status(403).body(banResult);
            }
            
            logger.info("用户登录处理完成，用户ID: {}", loggedInUser.getId());

            // 生成JWT Token
            String token = JwtUtil.generateToken(loggedInUser.getId().toString(), loggedInUser.getNickname());
            
            // 改为调试级别，避免频繁日志
            logger.debug("JWT Token 生成完成");

            // 创建登录信息对象
	            LoginInfo loginInfo = new LoginInfo(token, loggedInUser);

            // 返回成功响应
	            Map<String, Object> responseData = new HashMap<>();
	            responseData.put("token", loginInfo.getToken());
	            // 仅返回必要且安全的用户信息
	            Map<String, Object> safeUser = new HashMap<>();
	            safeUser.put("id", loggedInUser.getId());
	            safeUser.put("nickname", loggedInUser.getNickname());
	            safeUser.put("role", loggedInUser.getRole());
	            // 返回脱敏后的手机号（如有）
	            safeUser.put("phone", maskPhone(loggedInUser.getPhone()));
	            responseData.put("userInfo", safeUser);
            
            return ResponseEntity.ok(Result.success(responseData));
        } catch (Exception e) {
            logger.error("登录过程中发生错误: ", e);
            return ResponseEntity.status(500).body(Result.error("登录过程中发生错误: " + e.getMessage()));
        }
    }

	    // 将手机号中间四位替换为*，不足长度则原样返回
    @GetMapping("/me")
    public ResponseEntity<Result> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Result.error("未登录或登录已过期", -2));
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Result.error("未登录或登录已过期", -2));
        }

        Users user = authService.findUserById(Integer.parseInt(JwtUtil.getUserIdFromToken(token)));
        if (user == null) {
            return ResponseEntity.status(401).body(Result.error("用户不存在", -2));
        }

        UserBlacklist activeBan = userBlacklistMapper.selectActiveBanByUserId(user.getId());
        if (activeBan != null) {
            Result banResult = Result.error("账号已被封禁，请联系管理员", -1);
            banResult.setData(buildBanData(activeBan));
            return ResponseEntity.status(403).body(banResult);
        }

        Map<String, Object> safeUser = new HashMap<>();
        safeUser.put("id", user.getId());
        safeUser.put("nickname", user.getNickname());
        safeUser.put("role", user.getRole());
        safeUser.put("phone", maskPhone(user.getPhone()));
        return ResponseEntity.ok(Result.success(safeUser));
    }

    private Map<String, Object> buildBanData(UserBlacklist ban) {
        Map<String, Object> data = new HashMap<>();
        data.put("banned", true);
        data.put("reason", ban.getReason());
        data.put("bannedUntil", ban.getBannedUntil() == null ? null : ban.getBannedUntil().toString());
        return data;
    }

	    private String maskPhone(String phone) {
	    	if (phone == null) return null;
	    	if (phone.length() < 7) return phone;
	    	StringBuilder sb = new StringBuilder(phone);
	    	for (int i = 3; i < Math.min(7, sb.length()); i++) {
	    		sb.setCharAt(i, '*');
	    	}
	    	return sb.toString();
	    }
}
