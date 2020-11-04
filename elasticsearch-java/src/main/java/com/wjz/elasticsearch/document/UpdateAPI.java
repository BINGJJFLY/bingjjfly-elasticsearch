package com.wjz.elasticsearch.document;

import com.wjz.elasticsearch.Clients;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateAPI {

    public UpdateRequest update_request() {
        UpdateRequest request = new UpdateRequest(
                "posts",
                "1");
        return request;
    }

    public void updates_with_a_script() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("count", 4);

        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.field += params.count", parameters);
        update_request().script(inline);
    }

    public void updates_with_a_partial_document() throws IOException {
        UpdateRequest request = new UpdateRequest("posts", "1");
        String jsonString = "{" +
                "\"updated\":\"2017-01-01\"," +
                "\"reason\":\"daily update\"" +
                "}";
        request.doc(jsonString, XContentType.JSON);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("updated", new Date());
        jsonMap.put("reason", "daily update");
        request = new UpdateRequest("posts", "1")
                .doc(jsonMap);

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.timeField("updated", new Date());
            builder.field("reason", "daily update");
        }
        builder.endObject();
        request = new UpdateRequest("posts", "1")
                .doc(builder);

        request = new UpdateRequest("posts", "1")
                .doc("updated", new Date(),
                        "reason", "daily update");
    }

    public void upserts() {
        String jsonString = "{\"created\":\"2017-01-01\"}";
        update_request().upsert(jsonString, XContentType.JSON);
    }

    public void UpdateResponse() throws IOException {
        UpdateResponse updateResponse = Clients.CLIENT.update(update_request(), RequestOptions.DEFAULT);
        GetResult result = updateResponse.getGetResult();
        if (result.isExists()) {
            String sourceAsString = result.sourceAsString();
            Map<String, Object> sourceAsMap = result.sourceAsMap();
            byte[] sourceAsBytes = result.source();
        } else {

        }
    }
}
