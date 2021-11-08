package fr.alex6.discord.takobonker.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSDeclarationList;
import com.helger.css.decl.CSSExpressionMemberTermSimple;
import com.helger.css.reader.CSSReaderDeclarationList;
import fr.alex6.discord.cmx.CacheManager;
import fr.alex6.discord.cmx.CachedResource;
import fr.alex6.discord.cmx.Command;
import fr.alex6.discord.cmx.CommandModule;
import fr.alex6.discord.takobonker.HololiveChannel;
import fr.alex6.discord.takobonker.commands.entities.UpcomingStream;
import fr.alex6.discord.takobonker.http.HttpFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScheduleCommands extends CommandModule {
    private static final DateTimeFormatter HOLODULE_DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("MM/dd")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
            .toFormatter();
    private static final DateTimeFormatter HOLODULE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final List<HololiveChannel> channels = new ArrayList<>();

    private String buildYoutubeUrl(String endpoint, String ids) {
        return String.format("/%s?key=%s&part=snippet&id=%s", endpoint, System.getProperty("youtube.key"), ids);
    }

    private List<UpcomingStream> getUpcomingStreams(CacheManager cacheManager, String route) throws IOException {
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
                        if (!thumbnail.attr("href").contains("youtube.com")) continue;
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

    @Command(value = "upcoming", aliases = {"up"})
    public void upcoming(TextChannel channel, String[] args, CacheManager cacheManager, Message message) throws IOException {
        String[] routes = new String[] {"", "english", "indonesia", "innk", "china", "holostars", "hololive"};
        message.addReaction("loading:906914357470961665").queue();
        List<UpcomingStream> upcomingStreams;
        try {
            if (args.length > 0) {
                if (!Arrays.asList(routes).contains(args[0])) {
                    message.reply(":x: Unknown route \""+args[0]+"\"").queue();
                    return;
                }
                upcomingStreams = getUpcomingStreams(cacheManager, "/" + args[0]);

            } else {
                upcomingStreams = getUpcomingStreams(cacheManager, "");
            }
        } catch (HttpStatusException e) {
            message.reply(":x: Unknown route \""+args[0]+"\"").queue();
            return;
        }
        if (upcomingStreams.size() > 5) upcomingStreams = upcomingStreams.subList(0, 5);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Next Hololive Upcoming Streams");
        embedBuilder.setColor(Color.decode("#5fddef"));
        StringBuilder builder = embedBuilder.getDescriptionBuilder();
        List<Component> components = new LinkedList<>();
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
        message.clearReactions().complete();
        MessageAction messageAction = channel.sendMessageEmbeds(embedBuilder.build());
        if (components.size() > 0) messageAction = messageAction.setActionRow(components);
        messageAction.queue();
    }

    @Override
    public void onRegister() {
        for (Field field : HololiveChannel.class.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod) && field.getType() == HololiveChannel.class) {
                try {
                    HololiveChannel channel = (HololiveChannel) field.get(null);
                    channels.add(channel);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
