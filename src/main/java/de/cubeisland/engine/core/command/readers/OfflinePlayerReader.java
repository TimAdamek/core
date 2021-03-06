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
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerReader implements ArgumentReader<OfflinePlayer>
{
    private final Core core;

    public OfflinePlayerReader(Core core)
    {
        this.core = core;
    }

    @Override
    public OfflinePlayer read(Class type, CommandInvocation invocation) throws ReaderException
    {
        if (invocation.currentToken().startsWith("-"))
        {
            throw new ReaderException("Players do not start with -");
        }
        return ((BukkitCore)this.core).getServer().getOfflinePlayer(invocation.consume(1));
    }
}
