/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.command;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandException;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.SilentException;
import de.cubeisland.engine.butler.filter.RestrictedSourceException;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parameter.TooManyArgumentsException;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class ExceptionHandler implements de.cubeisland.engine.butler.ExceptionHandler
{
    private Core core;

    public ExceptionHandler(Core core)
    {
        this.core = core;
    }

    @Override
    public boolean handleException(Throwable t, CommandBase command, CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSender))
        {
            core.getLog().info("An unknown CommandSource ({}) caused an exception: {}",
                               invocation.getCommandSource().getClass().getName(), t.getMessage());
            return true;
        }

        if (t instanceof InvocationTargetException || t instanceof ExecutionException)
        {
            t = t.getCause();
        }

        CommandSender sender = (CommandSender)invocation.getCommandSource();
        if (t instanceof CommandException)
        {
            core.getLog().debug("Command failed: {}: {}", t.getClass(), t.getMessage());
            if (t instanceof PermissionDeniedException)
            {
                PermissionDeniedException e = (PermissionDeniedException)t;
                if (e.getMessage() != null)
                {
                    sender.sendTranslated(NEGATIVE, e.getMessage(), e.getArgs());
                }
                else
                {
                    sender.sendTranslated(NEGATIVE, "You're not allowed to do this!");
                    sender.sendTranslated(NEGATIVE, "Contact an administrator if you think this is a mistake!");
                }
                sender.sendTranslated(NEGATIVE, "Missing permission: {name}", e.getPermission().getName());
            }
            else if (t instanceof TooFewArgumentsException)
            {
                sender.sendTranslated(NEGATIVE, "You've given too few arguments.");
                sender.sendTranslated(NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));

            }
            else if (t instanceof TooManyArgumentsException)
            {
                sender.sendTranslated(NEGATIVE, "You've given too many arguments.");
                sender.sendTranslated(NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));
            }
            else if (t instanceof ReaderException)
            {
                sender.sendTranslated(NEGATIVE, t.getMessage(), ((ReaderException)t).getArgs());
            }
            else if (t instanceof RestrictedSourceException)
            {
                sender.sendTranslated(NEGATIVE, "You cannot execute this command!");
                if (t.getMessage() != null)
                {
                    sender.sendTranslated(NEUTRAL, t.getMessage());
                }
            }
            else if (t instanceof SilentException)
            {
                // do nothing
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Command failure: {input}: {input}", t.getClass().getName(), String.valueOf(t.getMessage()));
            }
        }
        else
        {
            core.getLog().error(t, "Unexpected Command Exception: " + t.getMessage());
            sender.sendTranslated(CRITICAL, "Unexpected command failure: {text}", t.getMessage());
        }
        return true;
    }
}
