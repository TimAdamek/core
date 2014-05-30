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
package de.cubeisland.engine.core.recipe.result.item;

import java.util.LinkedList;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;
import de.cubeisland.engine.core.recipe.result.logic.Result;

public class DurabilityResult extends Result implements MaterialProvider
{
    private enum Type
    {
        SET, ADD, SET_BIT, UNSET_BIT;
    }

    private Type type;
    private short data;

    private DurabilityResult(Type type, short data)
    {
        this.type = type;
        this.data = data;
    }

    @Override
    public ItemStack getResult(Player player, BlockState block, ItemStack itemStack)
    {
        short durability = itemStack.getDurability();
        switch (this.type)
        {
        case SET:
            durability = data;
            break;
        case ADD:
            durability += data;
            break;
        case SET_BIT:
            durability |= data;
            break;
        case UNSET_BIT:
            durability &= ~data;
            break;
        }
        itemStack.setDurability(durability);
        return itemStack;
    }

    public static DurabilityResult add(short data)
    {
        return new DurabilityResult(Type.ADD, data);
    }

    public static DurabilityResult remove(short data)
    {
        return new DurabilityResult(Type.ADD, (short)-data);
    }

    public static DurabilityResult set(short data)
    {
        return new DurabilityResult(Type.SET, data);
    }

    public static DurabilityResult setBit(short data)
    {
        return new DurabilityResult(Type.SET_BIT, data);
    }

    public static DurabilityResult unsetBit(short data)
    {
        return new DurabilityResult(Type.UNSET_BIT, data);
    }

    @Override
    public LinkedList<MaterialData> getMaterials(LinkedList<MaterialData> list)
    {
        if (this.type == Type.SET)
        {
            list.getLast().setData((byte)this.data);
        }
        return list;
    }
}
