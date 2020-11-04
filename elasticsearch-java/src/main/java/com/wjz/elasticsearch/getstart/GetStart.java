package com.wjz.elasticsearch.getstart;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class GetStart {

    private static final String hostname = "192.168.88.128";
    private static final int port = 9200;

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(hostname, port, "http")));
        client.close();
    }
}
