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
package de.cubeisland.engine.basics.command.general;

import java.sql.Timestamp;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.TimeUtil;
import de.cubeisland.engine.core.util.converter.DurationConverter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.StringNode;
import org.joda.time.Duration;

import static de.cubeisland.engine.core.command.sender.WrappedCommandSender.NON_PLAYER_UUID;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class ChatCommands
{
    private final DurationConverter converter = new DurationConverter();
    private final UserManager um;
    private final Basics module;

    private UUID lastWhisperOfConsole = null;

    public ChatCommands(Basics basics)
    {
        this.module = basics;
        this.um = basics.getCore().getUserManager();
    }



    @Command(desc = "Sends a private message to someone",
             names = {"tell", "message", "msg", "pm", "m", "t", "whisper", "w"},
             indexed = { @Grouped(@Indexed(label = {"player","!console"}, type = {User.class, String.class})),
                         @Grouped(value = @Indexed(label = "message"), greedy = true)})
    public void msg(CommandContext context)
    {
        if ("console".equalsIgnoreCase(context.getArg(0).toString()))
        {
            sendWhisperTo(NON_PLAYER_UUID, context.getStrings(1), context);
            return;
        }
        User user = context.getArg(0);
        if (!this.sendWhisperTo(user.getUniqueId(), context.getStrings(1), context))
        {
            context.sendTranslated(NEGATIVE, "Could not find the player {user} to send the message to. Is the player offline?", user);
        }
    }

    @Command(names = {"reply", "r"},
             desc = "Replies to the last person that whispered to you.",
             indexed = @Grouped(value = @Indexed(label = "message"), greedy = true))
    public void reply(CommandContext context)
    {
        UUID lastWhisper;
        if (context.getSender() instanceof User)
        {
            lastWhisper = ((User)context.getSender()).get(BasicsAttachment.class).getLastWhisper();
        }
        else
        {
            lastWhisper = lastWhisperOfConsole;
        }
        if (lastWhisper == null)
        {
            context.sendTranslated(NEUTRAL, "No one has sent you a message that you could reply to!");
            return;
        }
        if (!this.sendWhisperTo(lastWhisper, context.getStrings(0), context))
        {
            context.sendTranslated(NEGATIVE, "Could not find the player to reply to. Is the player offline?");
        }
    }

    private boolean sendWhisperTo(UUID whisperTarget, String message, CommandContext context)
    {
        if (NON_PLAYER_UUID.equals(whisperTarget))
        {
            if (context.getSender() instanceof ConsoleCommandSender)
            {
                context.sendTranslated(NEUTRAL, "Talking to yourself?");
                return true;
            }
            if (context.getSender() instanceof User)
            {
                ConsoleCommandSender console = context.getCore().getCommandManager().getConsoleSender();
                console.sendTranslated(NEUTRAL, "{sender} -> {text:You}: {message:color=WHITE}", context.getSender(), message);
                context.sendTranslated(NEUTRAL, "{text:You} -> {user}: {message:color=WHITE}", console.getDisplayName(), message);
                this.lastWhisperOfConsole = context.getSender().getUniqueId();
                ((User)context.getSender()).get(BasicsAttachment.class).setLastWhisper(NON_PLAYER_UUID);
                return true;
            }
            context.sendTranslated(NONE, "Who are you!?");
            return true;
        }
        User user = um.getExactUser(whisperTarget);
        if (!user.isOnline())
        {
            return false;
        }
        if (context.getSender().equals(user))
        {
            context.sendTranslated(NEUTRAL, "Talking to yourself?");
            return true;
        }
        user.sendTranslated(NONE, "{sender} -> {text:You}: {message:color=WHITE}", context.getSender().getName(), message);
        if (user.get(BasicsAttachment.class).isAfk())
        {
            context.sendTranslated(NEUTRAL, "{user} is afk!", user);
        }
        context.sendTranslated(NEUTRAL, "{text:You} -> {user}: {message:color=WHITE}", user, message);
        if (context.getSender() instanceof User)
        {
            ((User)context.getSender()).get(BasicsAttachment.class).setLastWhisper(user.getUniqueId());
        }
        else
        {
            this.lastWhisperOfConsole = user.getUniqueId();
        }
        user.get(BasicsAttachment.class).setLastWhisper(context.getSender().getUniqueId());
        return true;
    }

    @Command(desc = "Broadcasts a message",
             indexed = @Grouped(value = @Indexed(label = "message"), greedy = true))
    public void broadcast(CommandContext context)
    {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (context.hasArg(i))
        {
            sb.append(context.getArg(i++)).append(" ");
        }
        this.um.broadcastMessage(NEUTRAL, "[{text:Broadcast}] {}", sb.toString());
    }

    @Command(desc = "Mutes a player",
             indexed = { @Grouped(@Indexed(label = "player", type = User.class)),
                         @Grouped(value = @Indexed(label = "duration"), req = false)})
    public void mute(CommandContext context)
    {
        User user = context.getArg(0);
        BasicsUserEntity basicsUserEntity = user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().getbUEntity();
        if (basicsUserEntity.getMuted() != null && basicsUserEntity.getMuted().getTime() < System.currentTimeMillis())
        {
            context.sendTranslated(NEUTRAL, "{user} was already muted!", user);
        }
        Duration dura = module.getConfiguration().commands.defaultMuteTime;
        if (context.hasArg(1))
        {
            try
            {
                dura = converter.fromNode(StringNode.of(context.<String>getArg(1)), null);
            }
            catch (ConversionException e)
            {
                context.sendTranslated(NEGATIVE, "Invalid duration format!");
                return;
            }
        }
        basicsUserEntity.setMuted(new Timestamp(System.currentTimeMillis() +
            (dura.getMillis() == 0 ? TimeUnit.DAYS.toMillis(9001) : dura.getMillis())));
        basicsUserEntity.update();
        String timeString = dura.getMillis() == 0 ? user.getTranslation(NONE, "ever") : TimeUtil.format(user.getLocale(), dura.getMillis());
        user.sendTranslated(NEGATIVE, "You are now muted for {input#amount}!", timeString);
        context.sendTranslated(NEUTRAL, "You muted {user} globally for {input#amount}!", user, timeString);
    }

    @Command(desc = "Unmutes a player",
             indexed = @Grouped(@Indexed(label = "player", type = User.class)))
    public void unmute(CommandContext context)
    {
        User user = context.getArg(0);
        BasicsUserEntity basicsUserEntity = user.attachOrGet(BasicsAttachment.class, module).getBasicsUser().getbUEntity();
        basicsUserEntity.setMuted(null);
        basicsUserEntity.update();
        context.sendTranslated(POSITIVE, "{user} is no longer muted!", user);
    }

    @Command(names = {"rand","roll"}, desc = "Shows a random number from 0 to 100")
    public void rand(CommandContext context)
    {
        this.um.broadcastStatus(YELLOW, "rolled a {integer}!", context.getSender(), new Random().nextInt(100));
    }

    @Command(desc = "Displays the colors")
    public void chatcolors(CommandContext context)
    {
        context.sendTranslated(POSITIVE, "The following chat codes are available:");
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (ChatFormat chatFormat : ChatFormat.values())
        {
            if (i++ % 3 == 0)
            {
                builder.append("\n");
            }
            builder.append(" ").append(chatFormat.getChar()).append(" ").append(chatFormat.toString()).append(chatFormat.name()).append(ChatFormat.RESET);
        }
        context.sendMessage(builder.toString());
        context.sendTranslated(POSITIVE, "To use these type {text:&} followed by the code above");
    }
}
