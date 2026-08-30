package yimeng.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import yimeng.mapper.UserMapper;
import yimeng.pojo.Users;
import yimeng.util.JwtUtil;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (!JwtUtil.validateToken(token)) {
                writeError(response, 401, "未登录或登录已过期");
                return false;
            }
            Integer userId = Integer.valueOf(JwtUtil.getUserIdFromToken(token));
            Users user = userMapper.selectUserById(userId);
            if (user == null || !"admin".equals(user.getRole())) {
                writeError(response, 403, "无管理员权限");
                return false;
            }
            request.setAttribute("adminUserId", userId);
            return true;
        } catch (Exception e) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":0,\"msg\":\"" + message + "\"}");
    }
}
