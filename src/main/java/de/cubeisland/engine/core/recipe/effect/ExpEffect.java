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

public class ExpEffect extends Effect
{
    private enum Type
    {
        ADD, SET
    }

    private Type type;
    private float amount;
    private boolean lvl;

    private ExpEffect(Type type, float amount, boolean lvl)
    {
        this.type = type;
        this.amount = amount;
        this.lvl = lvl;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        switch (this.type)
        {

        case ADD:
            if (lvl)
            {
                player.setLevel((int)(player.getLevel() + amount));
            }
            else
            {
                player.setExp(player.getExp() + amount);
            }
            break;
        case SET:
            if (lvl)
            {
                player.setLevel((int)amount);
            }
            else
            {
                player.setExp(amount);
            }
            break;
        }
        return true;
    }

    public static ExpEffect setLvl(int amount)
    {
        return new ExpEffect(Type.SET, amount, true);
    }

    public static ExpEffect setExp(float amount)
    {
        return new ExpEffect(Type.SET, amount, false);
    }

    public static ExpEffect modifyLvl(int amount, boolean add)
    {
        return new ExpEffect(Type.ADD, add ? amount : -amount, true);
    }

    public static ExpEffect modifyExp(float amount, boolean add)
    {
        return new ExpEffect(Type.ADD, add ? amount : -amount, false);
    }
}
