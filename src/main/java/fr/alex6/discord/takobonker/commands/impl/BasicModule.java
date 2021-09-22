package fr.alex6.discord.takobonker.commands.impl;

import fr.alex6.discord.takobonker.commands.Command;
import fr.alex6.discord.takobonker.commands.CommandModule;
import net.dv8tion.jda.api.entities.Message;

public class BasicModule extends CommandModule {
    @Command(name = "test")
    public void test(Message message) {
        message.reply("Ok !").queue();
    }
}
