package com.wjz.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class Clients {

    private static final String hostname = "192.168.88.128";
    private static final int port = 9200;
    public static final RestHighLevelClient CLIENT = new RestHighLevelClient(
            RestClient.builder(
                    new HttpHost(hostname, port, "http")));

}
