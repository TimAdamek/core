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
package de.cubeisland.engine.core.command.readers;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.DefaultValue;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

/**
 * This argument is used to get users
 */
public class UserReader implements ArgumentReader<User>, DefaultValue<User>
{
    private final Core core;

    public UserReader(Core core)
    {
        this.core = core;
    }

    @Override
    public User read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        User user = this.core.getUserManager().findUser(arg);
        if (user == null)
        {
            throw new ReaderException(CubeEngine.getI18n().translate(invocation.getLocale(), NEGATIVE, "Player {user} not found!", arg));
        }
        return user;
    }

    @Override
    public User getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof User)
        {
            return (User)invocation.getCommandSource();
        }
        throw new ReaderException("You need to provide a player");
    }
}
