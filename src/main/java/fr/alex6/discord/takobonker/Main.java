/*
 * Copyright 2021 Alex6
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

import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.alex6.discord.cmx.CommandManager;
import fr.alex6.discord.cmx.impl.TestModule;
import fr.alex6.discord.takobonker.commands.ScheduleCommands;
import fr.alex6.discord.takobonker.jackson.HololiveChannelJsonDeserializer;
import fr.alex6.discord.takobonker.jackson.HololiveChannelJsonSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) throws LoginException {
        CommandManager commandManager = new CommandManager(".");
        SimpleModule takoModule = new SimpleModule();
        takoModule.addSerializer(new HololiveChannelJsonSerializer());
        takoModule.addDeserializer(HololiveChannel.class, new HololiveChannelJsonDeserializer());
        commandManager.addJacksonModule(takoModule);
        commandManager.registerModules(new TestModule(), new ScheduleCommands());
        JDA jda = JDABuilder.create(System.getProperty("bot.token"), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).addEventListeners(commandManager).build();
    }
}
