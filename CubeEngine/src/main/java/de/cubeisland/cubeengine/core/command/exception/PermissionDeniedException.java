package de.cubeisland.cubeengine.core.command.exception;

import de.cubeisland.cubeengine.core.command.CommandContext;
import org.bukkit.command.CommandSender;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

//TODO DOCU
public class PermissionDeniedException extends CommandException
{
    private PermissionDeniedException(String message)
    {
        super(message);
    }

    public static void denyAccess(CommandContext context, String category, String message, Object... params)
    {
        denyAccess(context.getSender(), category, message, params);
    }

    public static void denyAccess(CommandSender sender, String category, String message, Object... params)
    {
        throw new PermissionDeniedException(_(sender, category, message, params));
    }
}
