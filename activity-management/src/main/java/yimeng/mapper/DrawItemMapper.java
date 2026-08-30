package yimeng.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import yimeng.pojo.DrawItem;

import java.util.List;

@Mapper
public interface DrawItemMapper {

    @Insert("INSERT INTO draw_items(draw_id, name, count) VALUES(#{drawId}, #{name}, #{count})")
    int insert(DrawItem item);

    @Select("SELECT id, draw_id as drawId, name, count FROM draw_items WHERE draw_id = #{drawId} ORDER BY id ASC")
    List<DrawItem> selectByDrawId(Integer drawId);
}
