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
package de.cubeisland.engine.core.recipe.result.logic;

import java.util.LinkedList;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;

public class AndResult extends Result implements MaterialProvider
{
    private final Result left;
    private final Result right;

    AndResult(Result left, Result right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public ItemStack getResult(Player player, BlockState block, ItemStack itemStack)
    {
        return right.getResult(player, block, left.getResult(player, block, itemStack));
    }

    @Override
    public LinkedList<MaterialData> getMaterials(LinkedList<MaterialData> list)
    {
        int size = list.size();
        if (left instanceof MaterialProvider)
        {
            list = ((MaterialProvider)left).getMaterials(list);
        }
        boolean change = size != list.size();
        size = list.size();
        if (right instanceof MaterialProvider)
        {
            list = ((MaterialProvider)right).getMaterials(list);
        }
        if (change && size != list.size())
        {
            throw new IllegalStateException("Invalid condition! Cannot combine 2 Materials with AND");
        }
        return list;
    }
}
