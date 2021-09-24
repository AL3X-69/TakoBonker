package fr.alex6.discord.takobonker.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HttpFactory {
    public static final HttpFactory SCHEDULE_HOLOLIVE_TV = new HttpFactory("https://schedule.hololive.tv");
    public static final HttpFactory YOUTUBE_DATA_API_V3 = new HttpFactory("N/A");

    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    private HttpFactory(String baseUrl) {
        this.baseUrl = baseUrl;
        List<Header> headers = Collections.singletonList(new BasicHeader(HttpHeaders.USER_AGENT, "TakoBonker (1.0.0)"));
        this.client = HttpClientBuilder.create().setDefaultHeaders(headers).build();
        this.objectMapper = new ObjectMapper();
    }

    public static HttpFactory from(String baseUrl) {
        return new HttpFactory(baseUrl);
    }

    public JsonNode get(String endpoint) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl+endpoint);
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() > 299) {
            throw new HttpStatusException("Client encountered invalid non-OK status code: "+response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode(), baseUrl+endpoint);
        }
        return objectMapper.readTree(EntityUtils.toString(response.getEntity()));
    }

    public Document getHtmlDocument(String endpoint) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl+endpoint);
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() > 299) {
            throw new HttpStatusException("Client encountered invalid non-OK status code: "+response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode(), baseUrl+endpoint);
        }
        return Jsoup.parse(EntityUtils.toString(response.getEntity()));
    }
}
