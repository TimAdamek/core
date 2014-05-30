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

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.result.logic.Result;

public class AmountResult extends Result
{
    private enum Type
    {
        SET, ADD;
    }

    private Type type;
    private int amount;

    private AmountResult(Type type, int amount)
    {
        this.type = type;
        this.amount = amount;
    }

    @Override
    public ItemStack getResult(Player player, BlockState block, ItemStack itemStack)
    {
        int amount = itemStack.getAmount();
        switch (this.type)
        {
        case SET:
            amount = this.amount;
            break;
        case ADD:
            amount+= this.amount;
            break;
        }
        if (amount < 0)
        {
            amount = 0;
        }
        if (amount > 64)
        {
            amount = 64; // TODO ???
        }
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static AmountResult add(int amount)
    {
        return new AmountResult(Type.ADD, amount);
    }

    public static AmountResult remove(int amount)
    {
        return new AmountResult(Type.ADD, -amount);
    }

    public static AmountResult set(int amount)
    {
        return new AmountResult(Type.SET, amount);
    }
}
