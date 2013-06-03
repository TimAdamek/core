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
package de.cubeisland.cubeengine.log.action.logaction.block.interaction;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Changing NoteblockNotes
 * <p>Events: {@link RightClickActionType}</p>
 */
public class NoteBlockChange extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "noteblock-change";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        Long oldClicks = logEntry.getData();
        Integer newClicks = logEntry.getNewData();
        if (logEntry.hasAttached())
        {
            LogEntry last = logEntry.getAttached().last();
            newClicks = last.getNewData();
        }
        if (oldClicks.intValue() == newClicks)
        {
            user.sendTranslated("%s&2&s &afiddled around with the noteblock but did not change anything%s%a!",
                                time,logEntry.getCauserUser().getDisplayName(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aset the noteblock to &6%d&a clicks%s&a!",
                                time, logEntry.getCauserUser().getDisplayName(), newClicks,loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).NOTEBLOCK_CHANGE_enable;
    }

    @Override
    protected boolean nearTimeFrame(LogEntry logEntry, LogEntry other)
    {
        return Math.abs(TimeUnit.MILLISECONDS.toMinutes(logEntry.timestamp.getTime() - other.timestamp.getTime())) < 2;
    }
}
