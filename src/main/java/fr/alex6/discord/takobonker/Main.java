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

package fr.alex6.discord.takobonker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.alex6.discord.takobonker.commands.ScheduleCommands;
import fr.alex6.discord.takobonker.jackson.ColorJsonDeserializer;
import fr.alex6.discord.takobonker.jackson.ColorJsonSerializer;
import fr.alex6.discord.takobonker.jackson.HololiveChannelJsonDeserializer;
import fr.alex6.discord.takobonker.jackson.HololiveChannelJsonSerializer;
import fr.alex6.discord.takobonker.utils.CacheManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class Main {
    public static void main(String[] args) throws LoginException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule takoModule = new SimpleModule();
        takoModule.addSerializer(new HololiveChannelJsonSerializer());
        takoModule.addDeserializer(HololiveChannel.class, new HololiveChannelJsonDeserializer());
        takoModule.addSerializer(new ColorJsonSerializer());
        takoModule.addDeserializer(Color.class, new ColorJsonDeserializer());
        objectMapper.registerModules(takoModule, new JavaTimeModule());
        CacheManager cacheManager = new CacheManager(objectMapper);
        JDA jda = JDABuilder.create(System.getProperty("bot.token"), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).addEventListeners(new ScheduleCommands(cacheManager)).build();
        jda.updateCommands().addCommands(
                Commands.slash("upcoming", "Get Upcoming Streams")
                        .addOption(OptionType.STRING, "route", "Specify a filter, available filters: english, indonesia, holostars, hololive", false),
                Commands.slash("up", "Get Upcoming Streams")
                        .addOption(OptionType.STRING, "route", "Specify a filter, available filters: english, indonesia, holostars, hololive", false)
        ).queue();
    }
}
