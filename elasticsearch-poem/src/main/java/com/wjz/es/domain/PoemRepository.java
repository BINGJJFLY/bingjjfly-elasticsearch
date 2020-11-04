package com.wjz.es.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PoemRepository extends ElasticsearchRepository<Poem, String> {
}
