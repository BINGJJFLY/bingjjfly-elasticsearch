package com.wjz.es.mapper;

import com.wjz.es.domain.Poem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PoemMapper {

    @Select("select * from t_poem")
    List<Poem> list();

    @Insert("INSERT INTO `t_poem` VALUES (#{id}, #{name}, #{author}, #{categoryId}, #{category}, #{content}, #{authorDesc})")
    void save(Poem poem);

    @Update("UPDATE t_poem SET author_desc = #{authorDesc} WHERE id = #{id}")
    void update(Poem poem);
}
