package fr.alex6.discord.takobonker.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class CommandManager extends ListenerAdapter {
    private final List<CommandModule> modules = new ArrayList<>();
    private final Map<String, CommandInfo> commands = new HashMap<>();
    private final String prefix;

    public CommandManager(String prefix) {
        this.prefix = prefix;
    }

    private BiConsumer<MessageReceivedEvent, Permission[]> permissionsErrorHandler = (messageReceivedEvent, permissions) -> messageReceivedEvent.getMessage().reply(":x: Missing permissions: "+ Arrays.toString(permissions)).queue();
    private BiConsumer<MessageReceivedEvent, Throwable> runtimeErrorHandler = (messageReceivedEvent, throwable) -> messageReceivedEvent.getMessage().reply(":x: An error occurred: "+throwable.getMessage()).queue();

    public void registerModule(CommandModule module) {
        mapCommands(module);
        modules.add(module);
    }

    public List<CommandModule> getModules() {
        return modules;
    }

    public CommandInfo getCommand(String command) {
        return commands.get(command);
    }

    public void setPermissionsErrorHandler(BiConsumer<MessageReceivedEvent, Permission[]> permissionsErrorHandler) {
        this.permissionsErrorHandler = permissionsErrorHandler;
    }

    public void setRuntimeErrorHandler(BiConsumer<MessageReceivedEvent, Throwable> runtimeErrorHandler) {
        this.runtimeErrorHandler = runtimeErrorHandler;
    }

    private void mapCommands(CommandModule module) {
        for (Method method : module.getClass().getMethods()) {
            if (method.isAccessible() && method.isAnnotationPresent(Command.class)) {
                Command command = method.getAnnotation(Command.class);
                if (commands.containsKey(command.name())) throw new IllegalArgumentException("Duplicated command: Command "+command.name()+" is already mapped");
                commands.put(command.name(), new CommandInfo(command, method, module));
                for (String alias : command.aliases()) {
                    if (commands.containsKey(command.name())) throw new IllegalArgumentException("Duplicated command: Alias "+command.name()+" is already mapped");
                    commands.put(alias, new CommandInfo(command, method, module));
                }
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().equals(event.getJDA().getSelfUser())) return;
        if (event.getMessage().getContentRaw().startsWith(prefix)) {
            String command = event.getMessage().getContentRaw().replaceFirst(Pattern.quote(prefix), "").split(" ")[0];
            System.out.println(commands);
            String[] args = event.getMessage().getContentRaw().split(" ", 2).length == 1 ? new String[0] : event.getMessage().getContentRaw().split(" ", 2)[1].split(" ");
            if (commands.containsKey(command)) {
                CommandInfo commandInfo = commands.get(command);
                if ((event.getAuthor().isBot() && !commandInfo.getCommand().replyToBot()) || event.isWebhookMessage()) return;
                if (event.getChannel() instanceof PrivateChannel && commandInfo.getCommand().executionRule() == ExecutionRule.TEXT_CHANNEL) return;
                if (event.getChannel() instanceof TextChannel && commandInfo.getCommand().executionRule() == ExecutionRule.PRIVATE) return;
                if (event.getChannel() instanceof TextChannel) {
                    assert event.getMember() != null;
                    if (!event.getMember().getPermissions().containsAll(Arrays.asList(commandInfo.getCommand().permissions()))) {
                        permissionsErrorHandler.accept(event, (Permission[]) Arrays.stream(commandInfo.getCommand().permissions()).filter(event.getMember().getPermissions()::contains).toArray());
                        return;
                    }
                }

                // All verified, execution
                Method method = commandInfo.getMethod();
                Parameter[] parameters = method.getParameters();
                Object[] values = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].getType() == String[].class) values[i] = args;
                    else if (parameters[i].getType() == User.class) values[i] = event.getAuthor();
                    else if (parameters[i].getType() == Member.class) values[i] = event.getMember();
                    else if (parameters[i].getType() == MessageReceivedEvent.class) values[i] = event;
                    else if (parameters[i].getType() == MessageChannel.class) values[i] = event.getChannel();
                    else if (parameters[i].getType() == TextChannel.class) values[i] = event.getChannel() instanceof TextChannel ? (TextChannel) event.getChannel() : null;
                    else if (parameters[i].getType() == PrivateChannel.class) values[i] = event.getChannel() instanceof PrivateChannel ? (PrivateChannel) event.getChannel() : event.getAuthor().openPrivateChannel().complete();
                    else if (parameters[i].getType() == Guild.class) values[i] = event.getChannel() instanceof TextChannel ? event.getGuild() : null;
                    else if (parameters[i].getType() == Message.class) values[i] = event.getMessage();
                    else if (parameters[i].getType() == JDA.class) values[i] = event.getJDA();
                    else if (parameters[i].getType() == ShardManager.class) values[i] = event.getJDA().getShardManager();
                    else values[i] = null;
                }
                try {
                    method.invoke(commandInfo.getModule(), values);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    runtimeErrorHandler.accept(event, e);
                }
            }
        }
    }
}
