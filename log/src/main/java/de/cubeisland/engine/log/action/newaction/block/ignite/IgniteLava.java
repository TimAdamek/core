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
package de.cubeisland.engine.log.action.newaction.block.ignite;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents lava setting a block on fire
 */
public class IgniteLava extends ActionBlockIgnite
{
    public Coordinate source;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof IgniteLava;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "A fire got set by lava", "{amount} fires got set by lava", count);
    }

    public void setSource(Location source)
    {
        this.source = new Coordinate(source);
    }

    @Override
    public String getName()
    {
        return "lava";
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.ignite.lava;
    }
}
