package com.wjz.es.controller;

import com.alibaba.fastjson.JSON;
import com.github.houbb.segment.api.ISegmentResult;
import com.github.houbb.segment.support.segment.result.impl.SegmentResultHandlers;
import com.github.houbb.segment.util.SegmentHelper;
import com.wjz.es.domain.Poem;
import com.wjz.es.mapper.PoemMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/front")
public class FrontController {

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private PoemMapper poemMapper;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("poems", searchES(null,null,null,null));
        return "/front/list";
    }

    @GetMapping("/search")
    public String search(Model model, String content, String category) {
        String name = (String) model.getAttribute("name");
        String author = (String) model.getAttribute("author");
//        String content = (String) model.getAttribute("content");
        model.addAttribute("poems", searchES(name, author, category, content));
        return "/front/list";
    }

    private List<Poem> searchES(String name, String author, String category, String content) {
        if (content != null) {
            List<String> words = SegmentHelper.segment(content, SegmentResultHandlers.word());
            words.forEach(word -> {
                redisTemplate.opsForZSet().incrementScore("hotwords", word, 0.5);
            });
        }
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            HighlightBuilder highlight = new HighlightBuilder().field("*").requireFieldMatch(false).preTags(new String[]{"<span style='color:red'>"}).postTags(new String[]{"</span>"});
            sourceBuilder.highlighter(highlight);

            if (content == null) {
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            } else {
                sourceBuilder.query(QueryBuilders.multiMatchQuery(content, "name", "author", "content", "category"));
            }
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (name != null) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("name", name));
            }
            if (author != null) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("author", author));
            }
            if (category != null) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));
            }
            sourceBuilder.postFilter(boolQueryBuilder);

            SearchRequest searchRequest = new SearchRequest("poem");
            searchRequest.source(sourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            List<Poem> list = new ArrayList<>();
            SearchHits searchHits = searchResponse.getHits();
            for (SearchHit hit : searchHits.getHits()) {
                String source = hit.getSourceAsString();
                Poem book = JSON.parseObject(source, Poem.class);
                list.add(book);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields.containsKey("name")) {
                    book.setName(highlightFields.get("name").fragments()[0].toString());
                }
                if (highlightFields.containsKey("author")) {
                    book.setAuthor(highlightFields.get("author").fragments()[0].toString());
                }
            }

            long totalHits = searchHits.getTotalHits().value;

            System.out.println(" totalHits "+totalHits+"");

            TimeValue took = searchResponse.getTook();
            System.out.println("查询成功！请求参数: {}, 用时{}毫秒"+ searchRequest.source().toString()+ took.millis());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/hotwords")
    @ResponseBody
    public Set<ZSetOperations.TypedTuple<Object>> hotwords() {
         return redisTemplate.opsForZSet().reverseRangeWithScores("hotwords", 0, 20);
    }
}
