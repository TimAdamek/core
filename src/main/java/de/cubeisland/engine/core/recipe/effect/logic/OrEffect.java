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
package de.cubeisland.engine.core.recipe.effect.logic;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;

public class OrEffect extends Effect
{
    private Effect left;
    private Effect right;

    OrEffect(Effect left, Effect right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        if (left.runEffect(core, player))
        {
            return true;
        }
        return right.runEffect(core, player);
    }
}
