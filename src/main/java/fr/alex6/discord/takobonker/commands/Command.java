package fr.alex6.discord.takobonker.commands;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String name();
    String[] aliases() default {};
    ExecutionRule executionRule() default ExecutionRule.TEXT_CHANNEL;
    Permission[] permissions() default {};
    boolean replyToBot() default false;
}
