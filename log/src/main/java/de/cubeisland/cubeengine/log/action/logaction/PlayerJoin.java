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
package de.cubeisland.cubeengine.log.action.logaction;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ArrayNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;

/**
 * player joins
 * <p>Events: {@link PlayerJoinEvent}</p>
 */
public class PlayerJoin extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER);
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "player-join";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {

        if (this.isActive(event.getPlayer().getWorld()))
        {
            ArrayNode data = null;
            if (this.lm.getConfig(event.getPlayer().getWorld()).PLAYER_JOIN_ip)
            {
                data = this.om.createArrayNode();
                data.add(event.getPlayer().getAddress().getAddress().getHostAddress());
            }
            this.logSimple(event.getPlayer(), data == null ? null : data.toString());
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        //TODO attach multiple join at same loc
        user.sendTranslated("%s&2%s&a joined the server%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),loc);
        //TODO ip if known
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.location.equals(other.location)
            && logEntry.causer == other.causer;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).PLAYER_JOIN_enable;
    }
}