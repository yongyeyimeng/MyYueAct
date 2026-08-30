package yimeng.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yimeng.exception.DrawException;
import yimeng.mapper.UserMapper;
import yimeng.pojo.Draw;
import yimeng.pojo.DrawItem;
import yimeng.pojo.DrawParticipant;
import yimeng.pojo.Result;
import yimeng.pojo.Users;
import yimeng.service.DrawService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/draws")
@CrossOrigin(origins = "*")
public class DrawController {

    @Autowired
    private DrawService drawService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/create")
    public Result create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Integer creatorId = (Integer) request.getAttribute("userId");
        try {
            String content = (String) body.get("content");
            String visibility = (String) body.get("visibility");
            List<DrawItem> items = parseItems(body.get("items"));
            return Result.success(drawService.createDraw(creatorId, content, visibility, items));
        } catch (DrawException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("创建抽签失败");
        }
    }

    @PostMapping("/join")
    public Result join(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        try {
            String inviteCode = (String) body.get("inviteCode");
            DrawParticipant participant = drawService.joinDraw(userId, inviteCode);
            Draw detail = drawService.getDrawDetail(participant.getDrawId(), userId, isAdmin(userId));
            return Result.success(detail);
        } catch (DrawException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("参与抽签失败");
        }
    }

    @GetMapping("/detail")
    public Result detail(@RequestParam Integer drawId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        try {
            return Result.success(drawService.getDrawDetail(drawId, userId, isAdmin(userId)));
        } catch (DrawException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取抽签失败");
        }
    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(drawService.listDrawsByUser(userId, page, pageSize));
    }

    @PostMapping("/dissolve")
    public Result dissolve(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        try {
            Integer drawId = body.get("drawId") instanceof Number
                    ? ((Number) body.get("drawId")).intValue() : null;
            if (drawId == null) {
                return Result.error("请选择要解散的抽签");
            }
            drawService.dissolveDraw(drawId, userId, isAdmin(userId));
            return Result.success("抽签已解散");
        } catch (DrawException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("解散抽签失败");
        }
    }

    private boolean isAdmin(Integer userId) {
        Users user = userMapper.selectUserById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    private List<DrawItem> parseItems(Object rawItems) {
        List<DrawItem> items = new ArrayList<>();
        if (!(rawItems instanceof List)) {
            throw new DrawException("请至少添加一个抽签内容");
        }
        for (Object raw : (List<?>) rawItems) {
            if (!(raw instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) raw;
            DrawItem item = new DrawItem();
            item.setName(map.get("name") == null ? null : map.get("name").toString());
            Object count = map.get("count");
            item.setCount(count instanceof Number ? ((Number) count).intValue() : null);
            items.add(item);
        }
        if (items.isEmpty() || items.size() > 10) {
            throw new DrawException("抽签内容数量需在1到10之间");
        }
        return items;
    }
}
