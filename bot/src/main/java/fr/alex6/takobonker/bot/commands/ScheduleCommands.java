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

package fr.alex6.takobonker.bot.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSDeclarationList;
import com.helger.css.decl.CSSExpressionMemberTermSimple;
import com.helger.css.reader.CSSReaderDeclarationList;
import fr.alex6.takobonker.bot.HololiveChannel;
import fr.alex6.takobonker.bot.commands.entities.UpcomingStream;
import fr.alex6.takobonker.bot.http.HttpFactory;
import fr.alex6.takobonker.bot.utils.CacheManager;
import fr.alex6.takobonker.bot.utils.CachedResource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.*;

public class ScheduleCommands extends ListenerAdapter {
    private static final DateTimeFormatter HOLODULE_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("MM/dd")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter();
    private static final DateTimeFormatter HOLODULE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Logger logger = LoggerFactory.getLogger(ScheduleCommands.class);

    private final HololiveChannel[] channels;
    private final CacheManager cacheManager;

    public ScheduleCommands(@NotNull CacheManager cacheManager) throws IOException {
        this.cacheManager = cacheManager;
        ObjectMapper mapper = cacheManager.getObjectMapper();
        this.channels = mapper.readValue(getClass().getClassLoader().getResource("channels.json"), HololiveChannel[].class);
    }

    private String buildYoutubeUrl(String endpoint, String ids) {
        return String.format("/%s?key=%s&part=snippet&id=%s", endpoint, System.getProperty("youtube.key"), ids);
    }

    private @NotNull List<UpcomingStream> getUpcomingStreams(@NotNull String route) throws IOException {
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

    private HololiveChannel getChannelByColor(Color color) {
        for (HololiveChannel channel : channels) {
            if (channel.getColor().equals(color)) {
                return channel;
            }
        }
        return null;
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().startsWith("up")) {
            String[] routes = new String[] {"", "english", "indonesia", "innk", "china", "holostars", "hololive"};
            InteractionHook hook = event.deferReply().complete();
            List<UpcomingStream> upcomingStreams;
            String route = event.getOption("route", OptionMapping::getAsString);
            try {
                if (route != null) {
                    if (!Arrays.asList(routes).contains(route)) {
                        hook.editOriginal(":x: Unknown route \""+route+"\"").queue();
                        return;
                    }
                    upcomingStreams = getUpcomingStreams("/" + route);

                } else {
                    upcomingStreams = getUpcomingStreams("");
                }
            } catch (IOException e) {
                e.printStackTrace();
                hook.editOriginal(":x: Unknown route \""+route+"\"").queue();
                return;
            }
            boolean multiplePages = false;
            if (upcomingStreams.size() > 5) {
                upcomingStreams = upcomingStreams.subList(0, 5);
                multiplePages = true;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Next Hololive Upcoming Streams");
            embedBuilder.setColor(Color.decode("#5fddef"));
            StringBuilder builder = embedBuilder.getDescriptionBuilder();
            List<ItemComponent> components = new LinkedList<>();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                HololiveChannel hololiveChannel = upcomingStream.getChannel();
                assert hololiveChannel != null;
                builder.append(upcomingStream.isLive() ? "\uD83D\uDD34 **__ON AIR__ " : "⏲️ **").append(upcomingStream.getName()).append("** on [``").append(upcomingStream.getChannel().getName()).append("``](https://youtube.com/channel/").append(upcomingStream.getChannel().getId()).append(")'s channel ");
                if (!upcomingStream.isLive()) builder.append("<t:").append(upcomingStream.getTime().toEpochSecond(ZoneOffset.UTC)).append(":R>");
                if (upcomingStream.getParticipants().length > 1) {
                    builder.append("\n> With: ");
                    int c = 0;
                    for (UpcomingStream.Participant participant : upcomingStream.getParticipants()) {
                        c++;
                        builder.append(participant.getName());
                        if (c < upcomingStream.getParticipants().length) builder.append(", ");
                    }
                }
                builder.append("\n\n");
                Button button = Button.link(upcomingStream.getLink(), upcomingStream.getChannel().getName()+"'s live");
                if (!upcomingStream.getChannel().getEmoji().equals("")) button = button.withEmoji(Emoji.fromUnicode(upcomingStream.getChannel().getEmoji()));
                components.add(button);
            }
            if (builder.length() == 0) builder.append(":x: No live scheduled");
            if (multiplePages) embedBuilder.setFooter("Page 1/"+Math.ceil(upcomingStreams.size()/5.));
            WebhookMessageAction<Message> messageAction = hook.sendMessageEmbeds(embedBuilder.build());
            if (multiplePages){
                messageAction = messageAction.addActionRow(Button.primary("page_previous_"+(route == null ? "" : route), "Previous page").asDisabled(), Button.primary("page_next_"+(route == null ? "" : route), "Next Page"));
            }
            if (components.size() > 0) messageAction = messageAction.addActionRow(components);
            messageAction.queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("page_previous_")) {
            MessageEmbed.Footer footer = event.getMessage().getEmbeds().get(0).getFooter();
            if (footer == null) return;
            String footerText = footer.getText();
            if (footerText == null) return;
            int n = Integer.parseInt(footerText.split("/")[0].replace("Page ", ""));
            String route = event.getComponentId().replace("page_previous_", "");
            event.editMessageEmbeds(buildLoadingEmbed()).complete();

            List<UpcomingStream> upcomingStreams;
            try {
                upcomingStreams = getUpcomingStreams(route);
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("An error occured "+e.getMessage()).queue();
                return;
            }

            int total = upcomingStreams.size();
            upcomingStreams = upcomingStreams.subList((n-1)*5, n*5);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Next Hololive Upcoming Streams");
            embedBuilder.setColor(Color.decode("#5fddef"));
            StringBuilder builder = embedBuilder.getDescriptionBuilder();
            List<ItemComponent> components = new LinkedList<>();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                HololiveChannel hololiveChannel = upcomingStream.getChannel();
                assert hololiveChannel != null;
                builder.append(upcomingStream.isLive() ? "\uD83D\uDD34 **__ON AIR__ " : "⏲️ **").append(upcomingStream.getName()).append("** on [``").append(upcomingStream.getChannel().getName()).append("``](https://youtube.com/channel/").append(upcomingStream.getChannel().getId()).append(")'s channel ");
                if (!upcomingStream.isLive()) builder.append("<t:").append(upcomingStream.getTime().toEpochSecond(ZoneOffset.UTC)).append(":R>");
                if (upcomingStream.getParticipants().length > 1) {
                    builder.append("\n> With: ");
                    int c = 0;
                    for (UpcomingStream.Participant participant : upcomingStream.getParticipants()) {
                        c++;
                        builder.append(participant.getName());
                        if (c < upcomingStream.getParticipants().length) builder.append(", ");
                    }
                }
                builder.append("\n\n");
                Button button = Button.link(upcomingStream.getLink(), upcomingStream.getChannel().getName()+"'s live");
                if (!upcomingStream.getChannel().getEmoji().equals("")) button = button.withEmoji(Emoji.fromUnicode(upcomingStream.getChannel().getEmoji()));
                components.add(button);
            }
            if (builder.length() == 0) builder.append(":x: No live scheduled");
            embedBuilder.setFooter(String.format("Page %s/%s", n-1, Math.ceil(total/5.)));
            MessageAction messageAction = event.getMessage().editMessageEmbeds(embedBuilder.build());
            Set<ActionRow> actionRows = new LinkedHashSet<>();
            actionRows.add(ActionRow.of(Button.primary("page_previous_"+route, "Previous page").withDisabled(n<2), Button.primary("page_next_"+route, "Next Page")));
            if (components.size() > 0) actionRows.add(ActionRow.of(components));
            messageAction.setActionRows(actionRows).queue();
        } else if (event.getComponentId().startsWith("page_next_")) {
            MessageEmbed.Footer footer = event.getMessage().getEmbeds().get(0).getFooter();
            if (footer == null) return;
            String footerText = footer.getText();
            if (footerText == null) return;
            int n = Integer.parseInt(footerText.split("/")[0].replace("Page ", ""));
            String route = event.getComponentId().replace("page_next_", "");
            event.editMessageEmbeds(buildLoadingEmbed()).complete();

            List<UpcomingStream> upcomingStreams;
            try {
                upcomingStreams = getUpcomingStreams(route.equals("") ? "" : "/"+route);
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("An error occured "+e.getMessage()).queue();
                return;
            }

            int total = upcomingStreams.size();
            upcomingStreams = upcomingStreams.subList(n*5, Math.min((n + 1) * 5, upcomingStreams.size()));

            logger.debug(upcomingStreams.toString());

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Next Hololive Upcoming Streams");
            embedBuilder.setColor(Color.decode("#5fddef"));
            StringBuilder builder = embedBuilder.getDescriptionBuilder();
            List<ItemComponent> components = new LinkedList<>();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                HololiveChannel hololiveChannel = upcomingStream.getChannel();
                assert hololiveChannel != null;
                builder.append(upcomingStream.isLive() ? "\uD83D\uDD34 **__ON AIR__ " : "⏲️ **").append(upcomingStream.getName()).append("** on [``").append(upcomingStream.getChannel().getName()).append("``](https://youtube.com/channel/").append(upcomingStream.getChannel().getId()).append(")'s channel ");
                if (!upcomingStream.isLive()) builder.append("<t:").append(upcomingStream.getTime().toEpochSecond(ZoneOffset.UTC)).append(":R>");
                if (upcomingStream.getParticipants().length > 1) {
                    builder.append("\n> With: ");
                    int c = 0;
                    for (UpcomingStream.Participant participant : upcomingStream.getParticipants()) {
                        c++;
                        builder.append(participant.getName());
                        if (c < upcomingStream.getParticipants().length) builder.append(", ");
                    }
                }
                builder.append("\n\n");
                Button button = Button.link(upcomingStream.getLink(), upcomingStream.getChannel().getName()+"'s live");
                if (!upcomingStream.getChannel().getEmoji().equals("")) button = button.withEmoji(Emoji.fromUnicode(upcomingStream.getChannel().getEmoji()));
                components.add(button);
            }
            if (builder.length() == 0) builder.append(":x: No live scheduled");
            embedBuilder.setFooter(String.format("Page %s/%s", n+1, Math.ceil(total/5.)));
            MessageAction messageAction = event.getMessage().editMessageEmbeds(embedBuilder.build());
            Set<ActionRow> actionRows = new HashSet<>();
            logger.debug(String.valueOf(n));
            actionRows.add(ActionRow.of(Button.primary("page_previous_"+route, "Previous page"), Button.primary("page_next_"+route, "Next Page").withDisabled(n>=Math.ceil(total/5.))));
            if (components.size() > 0) actionRows.add(ActionRow.of(components));
            messageAction.setActionRows(actionRows).queue();
        }
    }

    public static @NotNull MessageEmbed buildLoadingEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.BLUE);
        embedBuilder.setDescription("<a:loading:906914357470961665> Loading...");
        return embedBuilder.build();
    }
}
