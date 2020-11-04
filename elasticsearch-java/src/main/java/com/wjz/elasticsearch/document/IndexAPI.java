package com.wjz.elasticsearch.document;

import com.wjz.elasticsearch.Clients;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IndexAPI {

    public IndexRequest index_request() {
        IndexRequest request = new IndexRequest("posts");
        request.id("1");
        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        return request.source(jsonString, XContentType.JSON);
    }

    public IndexRequest providing_the_document_source_Map() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "trying out Elasticsearch");
        IndexRequest indexRequest = new IndexRequest("posts")
                .id("1").source(jsonMap);
        return indexRequest;
    }

    public IndexRequest providing_the_document_source_XContentBuilder() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("user", "kimchy");
            builder.timeField("postDate", new Date());
            builder.field("message", "trying out Elasticsearch");
        }
        builder.endObject();
        IndexRequest indexRequest = new IndexRequest("posts")
                .id("1").source(builder);
        return indexRequest;
    }

    public IndexRequest providing_the_document_source_Keypairs() throws IOException {
        IndexRequest indexRequest = new IndexRequest("posts")
                .id("1")
                .source("user", "kimchy",
                        "postDate", new Date(),
                        "message", "trying out Elasticsearch");
        return indexRequest;
    }

    public void optional_arguments() {
        IndexRequest indexRequest = index_request();
        indexRequest.routing("routing");
        indexRequest.timeout(TimeValue.timeValueSeconds(1));
        indexRequest.timeout("1s");
        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
        indexRequest.setRefreshPolicy("wait_for");
        indexRequest.version(2);
        indexRequest.versionType(VersionType.EXTERNAL);
        indexRequest.opType(DocWriteRequest.OpType.CREATE);
        indexRequest.opType("create");
        indexRequest.setPipeline("pipeline");
    }

    public IndexResponse index_sync() throws IOException {
        IndexResponse indexResponse = Clients.CLIENT.index(index_request(), RequestOptions.DEFAULT);
        return indexResponse;
    }

    public void index_async() {
        Clients.CLIENT.indexAsync(index_request(), RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void index_response() throws IOException {
        IndexResponse indexResponse = index_sync();
        String index = indexResponse.getIndex();
        String id = indexResponse.getId();
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {

        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {

        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                String reason = failure.reason();
            }
        }
    }

    public void version_conflict() {
        IndexRequest request = new IndexRequest("posts")
                .id("1")
                .source("field", "value")
                .setIfSeqNo(10L)
                .setIfPrimaryTerm(20);
        try {
            IndexResponse response = Clients.CLIENT.index(request, RequestOptions.DEFAULT);
        } catch(ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {

            }
        } catch (IOException e) {

        }
    }

    public void existed() {
        IndexRequest request = new IndexRequest("posts")
                .id("1")
                .source("field", "value")
                .opType(DocWriteRequest.OpType.CREATE);
        try {
            IndexResponse response = Clients.CLIENT.index(request, RequestOptions.DEFAULT);
        } catch(ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {

            }
        } catch (IOException e) {

        }
    }
}
