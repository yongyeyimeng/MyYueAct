package yimeng.service;

import yimeng.pojo.Draw;
import yimeng.pojo.DrawParticipant;
import yimeng.pojo.DrawItem;
import yimeng.pojo.PageResult;

import java.util.List;

public interface DrawService {

    Draw createDraw(Integer creatorId, String content, String visibility, List<DrawItem> items);

    DrawParticipant joinDraw(Integer userId, String inviteCode);

    Draw getDrawDetail(Integer drawId, Integer userId, boolean isAdmin);

    void dissolveDraw(Integer drawId, Integer userId, boolean isAdmin);

    PageResult<Draw> listDrawsByUser(Integer userId, Integer page, Integer pageSize);
}
