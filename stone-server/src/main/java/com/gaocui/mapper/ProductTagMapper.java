package com.gaocui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaocui.entity.ProductTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductTagMapper extends BaseMapper<ProductTag> {

    // 按使用频次降序取前N个标签
    @Select("SELECT tag_name, COUNT(*) AS cnt FROM product_tags GROUP BY tag_name ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopTags(@Param("limit") int limit);
}
