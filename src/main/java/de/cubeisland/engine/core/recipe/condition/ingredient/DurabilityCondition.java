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
package de.cubeisland.engine.core.recipe.condition.ingredient;

import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.recipe.condition.logic.Condition;

/**
 * Checks the durability(data)
 */
public class DurabilityCondition extends IngredientCondition implements MaterialProvider
{
    private enum Type
    {
        EXACT, MORE, LESS, BIT, ANY
    }

    private Type type;
    private short data;

    private DurabilityCondition(Type type, short data)
    {
        this.type = type;
        this.data = data;
    }

    public static Condition less(short data)
    {
        return new DurabilityCondition(Type.LESS, data);
    }

    public static Condition more(short data)
    {
        return new DurabilityCondition(Type.MORE, data);
    }

    public static Condition exact(short data)
    {
        return new DurabilityCondition(Type.EXACT, data);
    }

    public static Condition notRange(short from, short to)
    {
        return DurabilityCondition.range(from, to).not();
    }

    public static Condition range(short from, short to)
    {
        return DurabilityCondition.more(from).and(DurabilityCondition.less(to));
    }

    public static Condition bitSet(short bit)
    {
        return new DurabilityCondition(Type.BIT, bit);
    }

    @Override
    public boolean check(Player player, ItemStack itemStack)
    {
        switch (this.type)
        {
            case EXACT:
                return itemStack.getDurability() == data;
            case MORE:
                return itemStack.getDurability() > data;
            case LESS:
                return itemStack.getDurability() < data;
            case BIT:
                return (itemStack.getDurability() & data) == data;
            case ANY:
                return true;
        }
        throw new IllegalStateException();
    }

    @Override
    public LinkedList<MaterialData> getMaterials(LinkedList<MaterialData> list)
    {
        if (this.type == Type.EXACT)
        {
            list.getLast().setData((byte)this.data);
        }
        else
        {
            list.getLast().setData((byte)-1); // wildcard
        }
        return list;
    }

    public static DurabilityCondition any()
    {
        return new DurabilityCondition(Type.ANY, (short)-1);
    }
}
