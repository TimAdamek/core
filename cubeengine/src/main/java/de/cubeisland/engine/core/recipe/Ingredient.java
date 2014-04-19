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
package de.cubeisland.engine.core.recipe;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;
import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.result.logic.Result;

/**
 * A crafting ingredient
 */
public class Ingredient
{
    private Condition condition;

    private Result result;

    private Ingredient(Condition condition)
    {
        this.condition = condition;
    }

    public final int find(Player player, ItemStack[] matrix)
    {
        for (int i = 0; i < matrix.length; i++)
        {
            if (condition.check(player, matrix[i]))
            {
                return i;
            }
        }
        return -1;
    }

    public final boolean check(Player player, ItemStack itemStack)
    {
        return condition.check(player, itemStack);
    }

    /**
     * Returns the resulting itemStack
     * <p>will return null if no result is given -> use default behaviour
     *
     *
     * @param player
     * @param block
     * @param item
     * @return
     */
    public final ItemStack getResult(Player player, BlockState block, ItemStack item)
    {
        if (result == null)
        {
            return null;
        }
        return result.getResult(player, block, item);
    }

    public final LinkedList<MaterialData> getMaterials()
    {
        if (condition instanceof MaterialProvider)
        {
            return ((MaterialProvider)condition).getMaterials(new LinkedList<MaterialData>());
        }
        throw new IllegalStateException("No Material given for ingredient!");
    }

    public final Ingredient withResult(Result result)
    {
        this.result = result;
        return this;
    }

    /**
     * Creates a new Ingredient matching only the given material
     *
     * @param material
     * @return
     */
    public static Ingredient withMaterial(Material material)
    {
        return new Ingredient(MaterialCondition.of(material));
    }

    /**
     * Creates a new Ingredient matching the given condition
     *
     * @param condition
     * @return
     */
    public static Ingredient withCondition(Condition condition)
    {
        return new Ingredient(condition);
    }

}
