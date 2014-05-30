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
package de.cubeisland.engine.core.recipe.effect;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class MessageEffect extends Effect
{
    private String msg;
    private Object[] args;

    private UserManager manager;

    public MessageEffect(UserManager manager, String msg, Object[]... args)
    {
        this.manager = manager;
        this.msg = msg;
        this.args = args;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        User user = manager.getExactUser(player.getName());
        user.sendTranslated(MessageType.NONE, msg, args);
        return true;
    }
}
