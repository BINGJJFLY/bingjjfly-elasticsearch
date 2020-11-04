package com.wjz.elasticsearch.document;

import com.wjz.elasticsearch.Clients;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Map;

public class GetAPI {

    public GetRequest get_request() {
        GetRequest getRequest = new GetRequest(
                "posts",
                "1");
        return getRequest;
    }

    public void optional_arguments() throws IOException {
        GetRequest request = get_request();

        request.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

        String[] includes = new String[]{"message", "*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        request.fetchSourceContext(fetchSourceContext);

        includes = Strings.EMPTY_ARRAY;
        excludes = new String[]{"message"};
        fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        request.fetchSourceContext(fetchSourceContext);

        request.storedFields("message");
        GetResponse getResponse = Clients.CLIENT.get(request, RequestOptions.DEFAULT);
        String message = getResponse.getField("message").getValue();

        request.routing("routing");
        request.preference("preference");
        request.realtime(false);
        request.refresh(true);
        request.version(2);
        request.versionType(VersionType.EXTERNAL);
    }

    public GetResponse get_sync() throws IOException {
        GetResponse getResponse = Clients.CLIENT.get(get_request(), RequestOptions.DEFAULT);
        return getResponse;
    }

    public void get_async() {
        Clients.CLIENT.getAsync(get_request(), RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse documentFields) {
            }
            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void get_response() throws IOException {
        GetResponse getResponse = get_sync();
        String index = getResponse.getIndex();
        String id = getResponse.getId();
        if (getResponse.isExists()) {
            long version = getResponse.getVersion();
            String sourceAsString = getResponse.getSourceAsString();
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            byte[] sourceAsBytes = getResponse.getSourceAsBytes();
        } else {

        }
    }

    public void does_not_exist() {
        GetRequest request = new GetRequest("does_not_exist", "1");
        try {
            GetResponse getResponse = Clients.CLIENT.get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {

            }
        } catch (IOException e) {

        }
    }

    public void version_conflict() {
        try {
            GetRequest request = new GetRequest("posts", "1").version(2);
            GetResponse getResponse = Clients.CLIENT.get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.CONFLICT) {

            }
        } catch (IOException e) {

        }
    }
}
