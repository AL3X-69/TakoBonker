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

import fr.alex6.takobonker.api.entities.HololiveChannel;
import fr.alex6.takobonker.api.entities.UpcomingStream;
import fr.alex6.takobonker.api.http.HololiveSchedule;
import fr.alex6.takobonker.api.utils.Commons;
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

import java.awt.*;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;

public class ScheduleCommands extends ListenerAdapter {
    private final HololiveSchedule schedule;

    public ScheduleCommands(@NotNull HololiveSchedule schedule) {
        this.schedule = schedule;
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
                    upcomingStreams = schedule.getUpcomingStreams("/" + route);

                } else {
                    upcomingStreams = schedule.getUpcomingStreams("");
                }
            } catch (IOException e) {
                e.printStackTrace();
                hook.editOriginal(":x: Unknown route \""+route+"\"").queue();
                return;
            }
            boolean multiplePages = false;
            int total = upcomingStreams.size();
            if (upcomingStreams.size() > 5) {
                upcomingStreams = upcomingStreams.subList(0, 5);
                multiplePages = true;
            }
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Next Hololive Upcoming Streams");
            embedBuilder.setColor(Commons.HOLOLIVE_COLOR);
            StringBuilder builder = embedBuilder.getDescriptionBuilder();
            List<ItemComponent> components = new LinkedList<>();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                HololiveChannel hololiveChannel = upcomingStream.getChannel();
                assert hololiveChannel != null;
                builder.append(upcomingStream.isLive() ? "\uD83D\uDD34 **__LIVE__ " : "⏲️ **").append(upcomingStream.getName()).append("** on [``").append(upcomingStream.getChannel().getName()).append("``](https://youtube.com/channel/").append(upcomingStream.getChannel().getId()).append(")'s channel ");
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
            if (multiplePages) embedBuilder.setFooter("Page 1/"+total/5);
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
                upcomingStreams = schedule.getUpcomingStreams(route);
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("An error occured "+e.getMessage()).queue();
                return;
            }

            int total = upcomingStreams.size();
            upcomingStreams = upcomingStreams.subList((n-2)*5, (n-1)*5);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Next Hololive Upcoming Streams");
            embedBuilder.setColor(Commons.HOLOLIVE_COLOR);
            StringBuilder builder = embedBuilder.getDescriptionBuilder();
            List<ItemComponent> components = new LinkedList<>();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                HololiveChannel hololiveChannel = upcomingStream.getChannel();
                assert hololiveChannel != null;
                builder.append(upcomingStream.isLive() ? "\uD83D\uDD34 **__LIVE__ " : "⏲️ **").append(upcomingStream.getName()).append("** on [``").append(upcomingStream.getChannel().getName()).append("``](https://youtube.com/channel/").append(upcomingStream.getChannel().getId()).append(")'s channel ");
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
            embedBuilder.setFooter(String.format("Page %s/%s", n-1, total/5));
            MessageAction messageAction = event.getMessage().editMessageEmbeds(embedBuilder.build());
            Set<ActionRow> actionRows = new LinkedHashSet<>();
            actionRows.add(ActionRow.of(Button.primary("page_previous_"+route, "Previous page").withDisabled(n<3), Button.primary("page_next_"+route, "Next Page")));
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
                upcomingStreams = schedule.getUpcomingStreams(route.equals("") ? "" : "/"+route);
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("An error occured "+e.getMessage()).queue();
                return;
            }

            int total = upcomingStreams.size();
            upcomingStreams = upcomingStreams.subList(n*5, Math.min((n + 1) * 5, upcomingStreams.size()));

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Next Hololive Upcoming Streams");
            embedBuilder.setColor(Commons.HOLOLIVE_COLOR);
            StringBuilder builder = embedBuilder.getDescriptionBuilder();
            List<ItemComponent> components = new LinkedList<>();
            for (UpcomingStream upcomingStream : upcomingStreams) {
                HololiveChannel hololiveChannel = upcomingStream.getChannel();
                assert hololiveChannel != null;
                builder.append(upcomingStream.isLive() ? "\uD83D\uDD34 **__LIVE__ " : "⏲️ **").append(upcomingStream.getName()).append("** on [``").append(upcomingStream.getChannel().getName()).append("``](https://youtube.com/channel/").append(upcomingStream.getChannel().getId()).append(")'s channel ");
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
            embedBuilder.setFooter(String.format("Page %s/%s", n+1, total/5));
            MessageAction messageAction = event.getMessage().editMessageEmbeds(embedBuilder.build());
            Set<ActionRow> actionRows = new HashSet<>();
            actionRows.add(ActionRow.of(Button.primary("page_previous_"+route, "Previous page"), Button.primary("page_next_"+route, "Next Page").withDisabled(n+1>=total/5)));
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
