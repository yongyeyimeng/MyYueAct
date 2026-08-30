package yimeng.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import yimeng.exception.DrawException;
import yimeng.mapper.DrawItemMapper;
import yimeng.mapper.DrawMapper;
import yimeng.mapper.DrawParticipantMapper;
import yimeng.pojo.Draw;
import yimeng.pojo.DrawItem;
import yimeng.pojo.DrawParticipant;
import yimeng.pojo.PageResult;
import yimeng.service.DrawService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DrawServiceImpl implements DrawService {

    @Autowired
    private DrawMapper drawMapper;

    @Autowired
    private DrawItemMapper drawItemMapper;

    @Autowired
    private DrawParticipantMapper drawParticipantMapper;

    @Override
    public Draw createDraw(Integer creatorId, String content, String visibility, List<DrawItem> items) {
        if (content == null || content.trim().isEmpty()) {
            content = "抽签";
        }
        if (items == null || items.isEmpty() || items.size() > 10) {
            throw new DrawException("抽签内容数量需在1到10之间");
        }

        int peopleCount = 0;
        for (DrawItem item : items) {
            if (item.getName() == null || item.getName().trim().isEmpty()) {
                throw new DrawException("抽签内容不能为空");
            }
            if (item.getCount() == null || item.getCount() < 1) {
                throw new DrawException("每个内容的人数至少为1");
            }
            item.setName(item.getName().trim());
            peopleCount += item.getCount();
        }

        Draw draw = new Draw();
        draw.setContent(content.trim());
        draw.setPeopleCount(peopleCount);
        draw.setVisibility("private".equalsIgnoreCase(visibility) ? "private" : "public");
        draw.setCreatorId(creatorId);
        draw.setStatus("open");
        draw.setExpiresAt(LocalDateTime.now().plusHours(1));

        for (int i = 0; i < 20; i++) {
            draw.setInviteCode(String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000)));
            try {
                drawMapper.insert(draw);
                for (DrawItem item : items) {
                    item.setDrawId(draw.getId());
                    drawItemMapper.insert(item);
                }
                draw.setItems(items);
                return draw;
            } catch (DuplicateKeyException ignored) {
                // retry with another code
            }
        }
        throw new DrawException("邀请码生成失败，请重试");
    }

    @Override
    public DrawParticipant joinDraw(Integer userId, String inviteCode) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            throw new DrawException("请输入邀请码");
        }

        Draw draw = drawMapper.selectByInviteCode(inviteCode.trim());
        if (draw == null) {
            throw new DrawException("邀请码不存在");
        }
        if (draw.getExpiresAt().isBefore(LocalDateTime.now())) {
            expireDraw(draw);
            throw new DrawException("邀请码已过期");
        }
        if ("expired".equals(draw.getStatus())) {
            throw new DrawException("邀请码已过期");
        }
        if ("drawn".equals(draw.getStatus())) {
            throw new DrawException("该抽签已结束");
        }
        if (drawParticipantMapper.selectByDrawAndUser(draw.getId(), userId) != null) {
            throw new DrawException("您已参与该抽签");
        }

        int currentCount = drawParticipantMapper.countByDrawId(draw.getId());
        if (currentCount >= draw.getPeopleCount()) {
            completeDraw(draw);
            throw new DrawException("该抽签人数已满");
        }

        DrawParticipant participant = new DrawParticipant();
        participant.setDrawId(draw.getId());
        participant.setUserId(userId);
        assignRandomItem(draw, participant);
        drawParticipantMapper.insert(participant);

        currentCount = drawParticipantMapper.countByDrawId(draw.getId());
        if (currentCount >= draw.getPeopleCount()) {
            completeDraw(draw);
        }
        return drawParticipantMapper.selectByDrawAndUser(draw.getId(), userId);
    }

    @Override
    public Draw getDrawDetail(Integer drawId, Integer userId, boolean isAdmin) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new DrawException("抽签不存在");
        }
        if (draw.getExpiresAt().isBefore(LocalDateTime.now()) && "open".equals(draw.getStatus())) {
            expireDraw(draw);
        }

        draw.setItems(drawItemMapper.selectByDrawId(draw.getId()));
        List<DrawParticipant> participants = drawParticipantMapper.selectByDrawIdWithNickname(draw.getId());
        draw.setParticipantCount(participants.size());

        if ("public".equals(draw.getVisibility()) || draw.getCreatorId().equals(userId) || isAdmin) {
            draw.setParticipants(participants);
        } else {
            List<DrawParticipant> ownResult = new ArrayList<>();
            for (DrawParticipant participant : participants) {
                if (participant.getUserId().equals(userId)) {
                    ownResult.add(participant);
                    break;
                }
            }
            draw.setParticipants(ownResult);
        }
        return draw;
    }

    @Override
    public void dissolveDraw(Integer drawId, Integer userId, boolean isAdmin) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new DrawException("抽签不存在");
        }
        if (!isAdmin && !draw.getCreatorId().equals(userId)) {
            throw new DrawException("只有创建者或管理员可以解散抽签");
        }
        drawMapper.deleteById(drawId);
    }

    @Override
    public PageResult<Draw> listDrawsByUser(Integer userId, Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        PageHelper.startPage(page, pageSize);
        List<Draw> draws = drawMapper.selectByUserId(userId);
        Page<Draw> p = (Page<Draw>) draws;
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    private void completeDraw(Draw draw) {
        List<DrawParticipant> participants = drawParticipantMapper.selectByDrawIdWithNickname(draw.getId());
        for (DrawParticipant participant : participants) {
            if (participant.getDrawItemId() == null) {
                assignRandomItem(draw, participant);
                drawParticipantMapper.updateDrawItemId(participant);
            }
        }

        draw.setStatus("drawn");
        draw.setDrawnAt(LocalDateTime.now());
        drawMapper.updateStatus(draw);
    }

    private void assignRandomItem(Draw draw, DrawParticipant participant) {
        List<DrawItem> items = drawItemMapper.selectByDrawId(draw.getId());
        List<DrawParticipant> assigned = drawParticipantMapper.selectByDrawIdWithNickname(draw.getId());
        List<Integer> slots = new ArrayList<>();
        for (DrawItem item : items) {
            int used = 0;
            for (DrawParticipant p : assigned) {
                if (p.getDrawItemId() != null && p.getDrawItemId().equals(item.getId())) {
                    used++;
                }
            }
            for (int i = used; i < item.getCount(); i++) {
                slots.add(item.getId());
            }
        }
        if (slots.isEmpty()) {
            throw new DrawException("该抽签人数已满");
        }
        Collections.shuffle(slots);
        participant.setDrawItemId(slots.get(0));
    }

    private void expireDraw(Draw draw) {
        draw.setStatus("expired");
        draw.setDrawnAt(null);
        drawMapper.updateStatus(draw);
    }

}
