package com.wjz.es.mapper;

import com.wjz.es.domain.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("select * from t_category")
    List<Category> list();

    @Insert("insert into t_category values (#{id}, #{name})")
    void save(Category category);
}
