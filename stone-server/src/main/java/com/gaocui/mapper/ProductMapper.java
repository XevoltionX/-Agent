package com.gaocui.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaocui.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {}
