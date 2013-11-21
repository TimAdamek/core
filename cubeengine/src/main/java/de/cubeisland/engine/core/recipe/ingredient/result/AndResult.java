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
package de.cubeisland.engine.core.recipe.ingredient.result;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.recipe.ingredient.condition.MaterialProvider;

public class AndResult extends IngredientResult implements MaterialProvider
{
    private final IngredientResult left;
    private final IngredientResult right;

    public AndResult(IngredientResult left, IngredientResult right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public ItemStack getResult(Permissible permissible, ItemStack itemStack)
    {
        if (left.check(permissible, itemStack))
        {
            itemStack = left.getResult(permissible, itemStack);
        }
        if (right.check(permissible, itemStack))
        {
            itemStack = right.getResult(permissible, itemStack);
        }
        return itemStack;
    }

    @Override
    public Set<Material> getMaterials(Set<Material> set)
    {

        int size = set.size();
        if (left instanceof MaterialProvider)
        {
            set = ((MaterialProvider)left).getMaterials(set);
        }
        boolean change = size != set.size();
        size = set.size();
        if (right instanceof MaterialProvider)
        {
            set = ((MaterialProvider)right).getMaterials(set);
        }
        if (change && size != set.size())
        {
            throw new IllegalStateException("Invalid condition! Cannot combine 2 Materials with AND");
        }
        return set;
    }
}
