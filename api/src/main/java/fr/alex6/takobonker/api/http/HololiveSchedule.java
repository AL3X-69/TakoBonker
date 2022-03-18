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
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSDeclarationList;
import com.helger.css.decl.CSSExpressionMemberTermSimple;
import com.helger.css.reader.CSSReaderDeclarationList;
import fr.alex6.takobonker.api.entities.HololiveChannel;
import fr.alex6.takobonker.api.entities.UpcomingStream;
import fr.alex6.takobonker.api.utils.CacheManager;
import fr.alex6.takobonker.api.utils.CachedResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HololiveSchedule {
    public static final DateTimeFormatter HOLODULE_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("MM/dd")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter();
    public static final DateTimeFormatter HOLODULE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final HololiveChannel[] channels;
    private final CacheManager cacheManager;

    public HololiveSchedule(@NotNull CacheManager cacheManager) throws IOException {
        this.cacheManager = cacheManager;
        this.channels = cacheManager.getObjectMapper().readValue(getClass().getClassLoader().getResource("channels.json"), HololiveChannel[].class);
    }

    public @NotNull List<UpcomingStream> getUpcomingStreams(@NotNull String route) throws IOException {
        String cacheName = route.equals("") ? "up-all" : "up-" + route.substring(1);
        CachedResource<UpcomingStream[]> cachedResource = cacheManager.getCachedResource(cacheName, UpcomingStream[].class);
        if (cachedResource == null || Duration.between(cachedResource.getCacheDateTime(), LocalDateTime.now()).getSeconds() > 300) {
            Document document = HttpFactory.SCHEDULE_HOLOLIVE_TV.getHtmlDocument("/lives"+route);
            LocalDate date = LocalDate.MIN;
            LocalDate now = LocalDate.now();
            List<UpcomingStream> upcomingStreams = new LinkedList<>();
            HttpFactory factory = HttpFactory.YOUTUBE_DATA_API_V3;
            for (Element container : document.getElementsByClass("container")) {
                if (container.getElementsByClass("holodule").size() > 0) {
                    String holoduleDate = container.getElementsByClass("holodule").get(0).text().split(" ")[0];
                    date = LocalDate.parse(holoduleDate, HOLODULE_DATE_FORMAT);
                }
                if (date.isEqual(now) || date.isAfter(now)) {
                    for (Element thumbnail : container.getElementsByClass("thumbnail")) {
                        if (!thumbnail.attr("href").contains("youtube.com") || thumbnail.getElementsByClass("datetime").size() == 0) continue;
                        LocalTime time = LocalTime.parse(thumbnail.getElementsByClass("datetime").get(0).ownText(), HOLODULE_TIME_FORMAT);
                        if (date.atTime(time).isBefore(LocalDateTime.now()) && !thumbnail.attr("style").contains("red")) continue;
                        UpcomingStream upcomingStream = new UpcomingStream();
                        upcomingStream.setTime(date.atTime(time));
                        upcomingStream.setLink(thumbnail.attr("href"));
                        upcomingStream.setThumbnail(thumbnail.getElementsByTag("img").get(1).attr("src"));
                        upcomingStream.setLive(thumbnail.attr("style").contains("red"));
                        List<UpcomingStream.Participant> participants = new ArrayList<>();
                        for (Element child : thumbnail.getElementsByClass("justify-content-between").get(0).children()) {
                            Element img = child.child(0);
                            UpcomingStream.Participant participant = new UpcomingStream.Participant();
                            participant.setIcon(img.attr("src"));
                            CSSDeclarationList cssDeclarationList = CSSReaderDeclarationList.readFromString(img.attr("style"), ECSSVersion.LATEST);
                            if (cssDeclarationList == null) {
                                participant.setColor(Color.WHITE);
                            } else {
                                CSSDeclaration cssDeclaration = cssDeclarationList.getDeclarationOfPropertyName("border");
                                if (cssDeclaration == null) {
                                    participant.setColor(Color.WHITE);
                                } else {
                                    CSSExpressionMemberTermSimple member = (CSSExpressionMemberTermSimple) cssDeclaration.getExpression().getMemberAtIndex(1);
                                    if (member == null) {
                                        participant.setColor(Color.WHITE);
                                    } else {
                                        participant.setColor(Color.decode(member.getValue()));
                                    }
                                }
                            }
                            HololiveChannel channel = getChannelByColor(participant.getColor());
                            assert channel != null;
                            participant.setName(channel.getName());
                            participant.setChannel(channel);
                            participants.add(participant);
                        }
                        upcomingStream.setChannel(participants.get(0).getChannel());
                        upcomingStream.setParticipants(participants.toArray(new UpcomingStream.Participant[0]));
                        upcomingStreams.add(upcomingStream);
                    }
                }
            }

            StringBuilder ids = new StringBuilder();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                ids.append(upcomingStream.getLink().substring(upcomingStream.getLink().indexOf("v=")+2)).append(",");
            }
            JsonNode items = factory.getJson(buildYoutubeUrl("videos", ids.toString())).get("items");
            int c = 0;
            for (JsonNode item : items) {
                upcomingStreams.get(c).setName(item.get("snippet").get("title").asText());
                c++;
            }

            cacheManager.cacheResource(cacheName, upcomingStreams.toArray());
            return upcomingStreams;
        } else {
            return Arrays.asList(cachedResource.getCachedResource());
        }
    }

    private String buildYoutubeUrl(String endpoint, String ids) {
        return String.format("/%s?key=%s&part=snippet&id=%s", endpoint, System.getProperty("youtube.key"), ids);
    }

    public @Nullable HololiveChannel getChannelByColor(Color color) {
        for (HololiveChannel channel : channels) {
            if (channel.getColor().equals(color)) {
                return channel;
            }
        }
        return null;
    }

    public @Nullable HololiveChannel getChannelByName(String name) {
        for (HololiveChannel channel : channels) {
            if (channel.getName().equals(name)) {
                return channel;
            }
        }
        return null;
    }
}
