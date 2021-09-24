package fr.alex6.discord.cmx;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotation to register a command inside a {@link CommandModule} class, this annotation takes the following arguments
 * - value: command name
 * - aliases: command aliases default {}
 * - executionRule: define if a command can be executed in private and/or text channel default {@link ExecutionRule}.TEXT_CHANNEL
 *
 * @see java.lang.annotation.Annotation
 * @author Alex6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String value();
    String[] aliases() default {};
    ExecutionRule executionRule() default ExecutionRule.TEXT_CHANNEL;
    Permission[] permissions() default {};
    boolean replyToBot() default false;
}
