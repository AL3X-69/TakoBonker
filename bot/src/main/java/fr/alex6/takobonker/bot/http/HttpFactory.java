/*
 * Copyright 2022 Alex6
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alex6.takobonker.bot.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HttpFactory {
    public static final HttpFactory SCHEDULE_HOLOLIVE_TV = new HttpFactory("https://schedule.hololive.tv");
    public static final HttpFactory YOUTUBE_DATA_API_V3 = new HttpFactory("https://www.googleapis.com/youtube/v3");

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

    public JsonNode getJson(String endpoint) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl+endpoint);
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() > 299) {
            throw new HttpStatusException("Client encountered invalid non-OK status code: "+response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode(), baseUrl+endpoint);
        }
        return objectMapper.readTree(EntityUtils.toString(response.getEntity()));
    }

    public Document getHtmlDocument(String endpoint) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl+endpoint);
        HttpResponse response;
        if (baseUrl.equals(SCHEDULE_HOLOLIVE_TV.baseUrl)) {
            BasicCookieStore cookieStore = new BasicCookieStore();
            BasicClientCookie cookie = new BasicClientCookie("timezone", "UTC");
            cookie.setDomain("schedule.hololive.tv");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
            response = client.execute(httpGet, localContext);
        } else {
            response = client.execute(httpGet);
        }
        if (response.getStatusLine().getStatusCode() > 299) {
            throw new HttpStatusException("Client encountered invalid non-OK status code: "+response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode(), baseUrl+endpoint);
        }
        return Jsoup.parse(EntityUtils.toString(response.getEntity()));
    }
}
