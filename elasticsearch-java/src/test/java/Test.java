import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {

    private static final String INDEX_NAME = "book";
    private static final String hostname = "192.168.88.128";
    private static final int port = 9200;
    private static final RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost(hostname, port, "http")));

    public static void main(String[] args) throws IOException {
        BookModel bookModel = new BookModel(1, "围城", "钱钟书", 1, 29.9, "《围城》是钱钟书所著的长篇小说，被誉为“新儒林外史”", "2020-10-30",1);
//        save(bookModel);

//        BookModel query = new BookModel();
////        query.setName("围城");
//        query.setSellReason("小说");
//        List<BookModel> result = list(query);
//        System.out.println(result.size());


//        BookModel update = new BookModel();
//        update.setId(1);
//        update.setSellReason("《围城》是钱钟书先生所著的长篇小说，被誉为“新儒林外史”，我个人虽然未曾拜读，但是我很神往！");
//        update(update);

//delete(1);

//        createIndex();

//        deleteIndex();

        bulk();
    }

    public static List<BookModel> list(BookModel bookRequestVO) {
        int pageNo = 1;
        int pageSize = 10;

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(pageNo - 1);
        sourceBuilder.size(pageSize);
        sourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.ASC));
//        sourceBuilder.query(QueryBuilders.matchAllQuery());
        HighlightBuilder highlight = new HighlightBuilder().field("*").requireFieldMatch(false).preTags(new String[]{"<span>"}).postTags(new String[]{"</span>"});
        sourceBuilder.highlighter(highlight);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (bookRequestVO.getName() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("name", bookRequestVO.getName()));
        }
        if (bookRequestVO.getAuthor() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("author", bookRequestVO.getAuthor()));
        }
        if (null != bookRequestVO.getStatus()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("status", bookRequestVO.getStatus()));
        }
        if (bookRequestVO.getSellTime() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("sellTime", bookRequestVO.getSellTime()));
        }
        if (bookRequestVO.getSellReason() != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("sellReason", bookRequestVO.getSellReason()));
        }
        if (bookRequestVO.getCategory() != null) {
            List<Integer> categoryList = new ArrayList<>();
            categoryList.add(bookRequestVO.getCategory());
            BoolQueryBuilder categoryBoolQueryBuilder = QueryBuilders.boolQuery();
            for (Integer category : categoryList) {
                categoryBoolQueryBuilder.should(QueryBuilders.termQuery("category", category));
            }
            boolQueryBuilder.must(categoryBoolQueryBuilder);
        }

        sourceBuilder.query(boolQueryBuilder);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            RestStatus restStatus = searchResponse.status();
            if (restStatus != RestStatus.OK) {
                return null;
            }

            List<BookModel> list = new ArrayList<>();
            SearchHits searchHits = searchResponse.getHits();
            for (SearchHit hit : searchHits.getHits()) {
                String source = hit.getSourceAsString();
                BookModel book = JSON.parseObject(source, BookModel.class);
                list.add(book);
            }

            long totalHits = searchHits.getTotalHits().value;

            System.out.println("pageNo "+pageNo+" totalHits "+totalHits+"");

            TimeValue took = searchResponse.getTook();
            System.out.println("查询成功！请求参数: {}, 用时{}毫秒"+ searchRequest.source().toString()+ took.millis());
            return list;
        } catch (IOException e) {
            System.out.println("查询失败！原因: {}"+ e.getMessage()+ e);
            return null;
        } finally {
            try {
                client.close();
            } catch (IOException e) {

            }
        }
    }

    public static void save(BookModel bookModel) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", bookModel.getId());
        jsonMap.put("name", bookModel.getName());
        jsonMap.put("author", bookModel.getAuthor());
        jsonMap.put("category", bookModel.getCategory());
        jsonMap.put("price", bookModel.getPrice());
        jsonMap.put("sellTime", bookModel.getSellTime());
        jsonMap.put("sellReason", bookModel.getSellReason());
        jsonMap.put("status", bookModel.getStatus());

        IndexRequest indexRequest = new IndexRequest(INDEX_NAME).id(String.valueOf(bookModel.getId()));
        indexRequest.source(jsonMap);

        client.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                String index = indexResponse.getIndex();
                String id = indexResponse.getId();
                long version = indexResponse.getVersion();

                System.out.println("Index: {}, Type: {}, Id: {}, Version: {}"+ index+ "_doc"+ id+ version);

                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    System.out.println("写入文档");
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    System.out.println("修改文档");
                }
                ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                    System.out.println("部分分片写入成功");
                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                        String reason = failure.reason();
                        System.out.println("失败原因: {}"+ reason);
                    }
                }
                try {
                    client.close();
                } catch (IOException e) {

                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e.getMessage()+ e);
                try {
                    client.close();
                } catch (IOException ioException) {

                }
            }
        });
    }

    public static void update(BookModel bookModel) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("sellReason", bookModel.getSellReason());
        UpdateRequest request = new UpdateRequest(INDEX_NAME, String.valueOf(bookModel.getId()));
        request.doc(jsonMap);
        try {
            UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
            if (updateResponse.status() == RestStatus.OK) {
                System.out.println("更新成功！");
            }
        } catch (IOException e) {
            System.out.println("更新失败！原因: {}"+ e.getMessage()+ e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {

            }
        }
    }

    public static void delete(int id) {
        DeleteRequest request = new DeleteRequest(INDEX_NAME, String.valueOf(id));
        try {
            DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
            if (deleteResponse.status() == RestStatus.OK) {
                System.out.println("删除成功！id: {}"+ id);
            }
        } catch (IOException e) {
            System.out.println("删除失败！原因: {}"+ e.getMessage()+ e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {

            }
        }
    }

    public static void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("book");
        request.settings(Settings.builder()
                .put("index.number_of_shards", 2)
                .put("index.number_of_replicas", 1)
        );
        request.mapping("{\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\",\"search_analyzer\":\"ik_max_word\"},\"author\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\",\"search_analyzer\":\"ik_max_word\"},\"category\":{\"type\":\"integer\"},\"price\":{\"type\":\"double\"},\"status\":{\"type\":\"short\"},\"sellReason\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\",\"search_analyzer\":\"ik_max_word\"},\"sellTime\":{\"type\":\"date\",\"format\":\"yyyy-MM-dd\"}}}", XContentType.JSON);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
        try {
            client.close();
        } catch (IOException e) {

        }
    }

    public static void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("book");
        AcknowledgedResponse deleteIndexResponse = client.indices().delete(request, RequestOptions.DEFAULT);
        boolean acknowledged = deleteIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
        try {
            client.close();
        } catch (IOException e) {

        }
     }

     public static void bulk() throws IOException {
         BulkRequest request = new BulkRequest();

         BookModel bookModel = new BookModel(1, "活着", "余华", 1, 39.9, "《活着》是余华所著的长篇小说，里边主要人物是福贵，机缘巧合农村历史题材", "2020-10-30",1);
         Map<String, Object> jsonMap = new HashMap<>();
         jsonMap.put("id", bookModel.getId());
         jsonMap.put("name", bookModel.getName());
         jsonMap.put("author", bookModel.getAuthor());
         jsonMap.put("category", bookModel.getCategory());
         jsonMap.put("price", bookModel.getPrice());
         jsonMap.put("sellTime", bookModel.getSellTime());
         jsonMap.put("sellReason", bookModel.getSellReason());
         jsonMap.put("status", bookModel.getStatus());

         IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
         indexRequest.source(jsonMap);
         request.add(indexRequest);

         bookModel = new BookModel(1, "平凡的世界", "路遥", 1, 49.9, "《平凡的世界》是路遥所著的长篇小说，浩浩荡荡几百万字，这是接触的第一本长篇小说，故事内容非常精彩，故事引人入胜，我也被感动的一塌糊涂", "2020-10-30",1);
         jsonMap = new HashMap<>();
         jsonMap.put("id", bookModel.getId());
         jsonMap.put("name", bookModel.getName());
         jsonMap.put("author", bookModel.getAuthor());
         jsonMap.put("category", bookModel.getCategory());
         jsonMap.put("price", bookModel.getPrice());
         jsonMap.put("sellTime", bookModel.getSellTime());
         jsonMap.put("sellReason", bookModel.getSellReason());
         jsonMap.put("status", bookModel.getStatus());

         indexRequest = new IndexRequest(INDEX_NAME);
         indexRequest.source(jsonMap);
         request.add(indexRequest);

         BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);

         for (BulkItemResponse bulkItemResponse : bulkResponse) {
             DocWriteResponse itemResponse = bulkItemResponse.getResponse();
             if (bulkItemResponse.isFailed()) {
                 BulkItemResponse.Failure failure =
                         bulkItemResponse.getFailure();
                 System.out.println(failure.getCause());
             }
             switch (bulkItemResponse.getOpType()) {
                 case INDEX:
                 case CREATE:
                     IndexResponse indexResponse = (IndexResponse) itemResponse;
                     break;
                 case UPDATE:
                     UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                     break;
                 case DELETE:
                     DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
             }
         }
         try {
             client.close();
         } catch (IOException e) {

         }
     }

}
