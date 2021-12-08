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
