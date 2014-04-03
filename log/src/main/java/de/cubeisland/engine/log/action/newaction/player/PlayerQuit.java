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
package de.cubeisland.engine.log.action.newaction.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.PLAYER;

/**
 * Represents a Player leaving the server
 */
public class PlayerQuit extends ActionPlayer<PlayerActionListener>
{
    public String reason;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerQuit && this.player.equals(((PlayerQuit)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} left the server x{amount}", this.player.name,
                                       this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} left the server", this.player.name);
        // TODO reason
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    @Override
    public ActionCategory getCategory()
    {
        return PLAYER;
    }

    @Override
    public String getName()
    {
        return "quit";
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.player.quit;
    }
}
