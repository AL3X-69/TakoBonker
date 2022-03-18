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
import fr.alex6.takobonker.api.entities.WikiSection;
import fr.alex6.takobonker.api.http.HololiveInfo;
import fr.alex6.takobonker.api.http.HololiveSchedule;
import fr.alex6.takobonker.api.utils.Commons;
import fr.alex6.takobonker.api.utils.WikiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfoCommand extends ListenerAdapter {
    private final HololiveInfo info = new HololiveInfo();
    private final HololiveSchedule schedule;

    public InfoCommand(HololiveSchedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("info")) {
            OptionMapping mapping = event.getOption("streamer");
            if (mapping != null) {
                InteractionHook hook = event.deferReply().complete();
                try {
                    String page = info.searchPage(mapping.getAsString());
                    if (page != null) {
                        WikiSection[] sections = info.getWikiSections(page);
                        List<WikiSection> level1Sections = new ArrayList<>();
                        WikiSection bio = sections[0];
                        for (WikiSection section : sections) {
                            if (section.getAnchor().equals("Official_Bio")) {
                                bio = section;
                            }
                            if (section.getToclevel() == 1) {
                                level1Sections.add(section);
                            }
                        }
                        String wikitext = info.getWikiSectionContent(page, Integer.parseInt(bio.getIndex()));
                        String markdown = WikiUtils.wikiTextToMarkdown(wikitext);
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        HololiveChannel channel = schedule.getChannelByName(page);
                        embedBuilder.setColor(channel == null ? Commons.HOLOLIVE_COLOR : channel.getColor());
                        embedBuilder.setTitle(page);
                        embedBuilder.setDescription(markdown);
                        embedBuilder.setThumbnail(WikiUtils.getMemberThumbnail(page));
                        SelectMenu.Builder builder = SelectMenu.create("sections_list")
                                .setMinValues(1)
                                .setMinValues(1);
                        for (WikiSection section : level1Sections) {
                            builder.addOption(section.getLine(), "section__"+page+"__"+section.getIndex());
                        }
                        builder.setDefaultValues(Collections.singleton("section__"+page+"__"+bio.getIndex()));
                        hook.editOriginalEmbeds(embedBuilder.build()).setActionRow(builder.build()).queue();
                    } else hook.editOriginal(":x: No streamer found for \""+mapping.getAsString()+"\"").queue();
                } catch (IOException e) {
                    event.reply(":x: "+e.getMessage()).queue();
                }
            } else {
                event.reply(":x: Please specify a streamer").queue();
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String[] parts = event.getSelectedOptions().get(0).getValue().split("__");
        if (parts[0].equals("section")) {
            String page = parts[1];
            String sectionIndex = parts[2];
            InteractionHook hook = event.deferEdit().complete();
            try {
                String wikitext = info.getWikiSectionContent(page, Integer.parseInt(sectionIndex));
                String markdown = WikiUtils.wikiTextToMarkdown(wikitext);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                HololiveChannel channel = schedule.getChannelByName(page);
                embedBuilder.setColor(channel == null ? Commons.HOLOLIVE_COLOR : channel.getColor());
                embedBuilder.setTitle(page);
                embedBuilder.setDescription(markdown);
                embedBuilder.setThumbnail(WikiUtils.getMemberThumbnail(page));
                SelectMenu.Builder builder = SelectMenu.fromData(event.getComponent().toData())
                        .setDefaultValues(Collections.singleton("section__" + page + "__" + sectionIndex));
                hook.editOriginalEmbeds(embedBuilder.build()).setActionRow(builder.build()).queue();
            } catch (IOException e) {
                event.reply(":x: "+e.getMessage()).queue();
            }
        }
    }
}
