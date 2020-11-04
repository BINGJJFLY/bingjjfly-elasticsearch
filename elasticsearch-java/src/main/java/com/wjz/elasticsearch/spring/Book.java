package com.wjz.elasticsearch.spring;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "book", shards = 1, replicas = 0, createIndex = true)
public class Book {

    @Id
    private Integer id;         //  图书ID

    @Field(type = FieldType.Keyword)
    private String name;        //  图书名称

    @Field(type = FieldType.Keyword)
    private String author;      //  作者

    @Field(type = FieldType.Integer)
    private Integer category;   //  图书分类

    @Field(type = FieldType.Double)
    private Double price;       //  图书价格

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
    private String sellReason;  //  上架理由

    private String sellTime;      //  上架时间

    private Integer status;     //  状态（1：可售，0：不可售）
}
