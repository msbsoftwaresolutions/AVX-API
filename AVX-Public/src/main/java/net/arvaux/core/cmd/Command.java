package net.arvaux.core.cmd;

import net.arvaux.core.group.Group;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String[] aliases() default {};

    String description() default "An Arvaux provided command.";

    boolean inGameOnly() default true;

    String name();

    Group permission() default Group.REGULAR;

    String usage() default "/<command>";
}
