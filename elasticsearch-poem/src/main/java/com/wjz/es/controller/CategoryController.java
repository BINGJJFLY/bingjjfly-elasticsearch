package com.wjz.es.controller;

import com.wjz.es.domain.Category;
import com.wjz.es.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryMapper mapper;

    @GetMapping("/list")
    public List<Category> list() {
        return mapper.list();
    }

    @GetMapping("/create")
    public String create() {
        Category category = new Category();
        category.setId("1");
        category.setName("1");
        mapper.save(category);
        return "success";
    }
}
