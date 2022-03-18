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

package fr.alex6.takobonker.api.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.alex6.takobonker.api.entities.WikiSection;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;

public class HololiveInfo {
    public static final String WIKI_SECTIONS_URL = "https://hololive.wiki/w/api.php?action=parse&format=json&page=%s&redirects=1&prop=sections";
    public static final String WIKI_SECTION_CONTENT_URL = "https://hololive.wiki/w/api.php?action=query&format=json&prop=revisions&titles=%s&redirects=1&rvprop=content&rvslots=*&rvsection=%s";
    public static final String WIKI_SEARCH_PAGE = "https://hololive.wiki/w/api.php?action=query&format=json&list=search&srsearch=*%s*";

    private final HttpClient http = HttpClients.createDefault();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(HololiveInfo.class);

    public String searchPage(String query) throws IOException {
        HttpGet get = new HttpGet(String.format(WIKI_SEARCH_PAGE, query));
        HttpResponse response = http.execute(get);
        if (response.getStatusLine().getStatusCode() > 299) throw new IOException("Received NON-OK Status Code");
        JsonNode node = mapper.readTree(EntityUtils.toString(response.getEntity()));
        logger.debug(node.toString());
        if (node.get("query").get("searchinfo").get("totalhits").asInt() > 0) {
            for (JsonNode item : node.get("query").get("search")) {
                if (item.get("size").asInt() > 100) return item.get("title").asText();
            }
        }
        return null;
    }

    public WikiSection[] getWikiSections(String page) throws IOException {
        HttpGet get = new HttpGet(String.format(WIKI_SECTIONS_URL, URLEncoder.encode(page, "UTF-8")));
        HttpResponse response = http.execute(get);
        if (response.getStatusLine().getStatusCode() > 299) throw new IOException("Received NON-OK Status Code");
        JsonNode node = mapper.readTree(EntityUtils.toString(response.getEntity()));
        return mapper.treeToValue(node.get("parse").get("sections"), WikiSection[].class);
    }

    public String getWikiSectionContent(String page, int index) throws IOException {
        HttpGet get = new HttpGet(String.format(WIKI_SECTION_CONTENT_URL, URLEncoder.encode(page, "UTF-8"), index));
        HttpResponse response = http.execute(get);
        if (response.getStatusLine().getStatusCode() > 299) throw new IOException("Received NON-OK Status Code");
        JsonNode node = mapper.readTree(EntityUtils.toString(response.getEntity()));
        logger.debug(node.toString());
        return node.get("query").get("pages").fields().next().getValue().get("revisions").get(0).get("slots").get("main").get("*").asText();
    }
}
