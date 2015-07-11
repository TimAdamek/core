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
package de.cubeisland.engine.service.i18n.formatter;

import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.formatter.Context;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.Format;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.Names;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.ReflectedFormatter;
import de.cubeisland.engine.service.i18n.formatter.component.StyledComponent;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.util.command.CommandSource;

import static org.spongepowered.api.text.format.TextColors.DARK_GREEN;

@Names({"user","sender","tamer"})
public class CommandSenderFormatter extends ReflectedFormatter
{
    @Format
    public MessageComponent format(String string, Context context)
    {
        return new StyledComponent(DARK_GREEN, string);
    }

    @Format
    public MessageComponent format(CommandSource sender, Context context)
    {
        return this.format(sender.getName(), context);
    }

    @Format
    public MessageComponent format(de.cubeisland.engine.butler.CommandSource sender, Context context)
    {
        return this.format(sender.getName(), context);
    }

    @Format
    public MessageComponent format(Tamer tamer, Context context) // includes OfflinePlayer as it implements AnimalTamer
    {
        return this.format(tamer.getName(), context);
    }

}
