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
package de.cubeisland.engine.core.recipe.condition.logic;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.general.PermissionCondition;

public abstract class Condition
{
    public final Condition and(Condition condition)
    {
        return new AndCondition(this, condition);
    }

    public final Condition or(Condition condition)
    {
        return new OrCondition(this, condition);
    }

    public final Condition not()
    {
        return new NotCondition(this);
    }

    public final Condition perm(String perm, boolean need)
    {
        return this.and(new PermissionCondition(perm, need));
    }
    public abstract boolean check(Player player, ItemStack itemStack);
}

// general Conditions
// - generated Permission
// - world
// - terrain-height
// - lightlevel
// - weather
// - exp/level
// - money (needs Economy Service)
// - itemInHand /w ingredient OR itemstack
// - cooldown (needs some sort of manager)
// - powered by redstone (get block from playerinteract save later remove on death/tp/quit/close etc.)
