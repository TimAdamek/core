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

import java.util.Map;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.result.logic.Result;

public class CubeWorkbenchRecipe extends CubeRecipe<WorkbenchIngredients>
{
    private Result preview;
    private int size;

    public CubeWorkbenchRecipe(WorkbenchIngredients ingredients, Result result)
    {
        super(ingredients, result);
        this.size = ingredients.getSize();
    }

    public final Map<Integer, ItemStack> getIngredientResults(Player player, BlockState block, ItemStack[] matrix)
    {
        return ingredients.getIngredientResults(player, block, matrix.clone());
    }

    public final CubeWorkbenchRecipe withPreview(Result preview)
    {
        this.preview = preview;
        return this;
    }

    @Override
    public final CubeWorkbenchRecipe withCondition(Condition condition)
    {
        return (CubeWorkbenchRecipe)super.withCondition(condition);
    }

    public final boolean matchesConditions(Player player, ItemStack[] matrix)
    {
        if (this.condition != null)
        {
            if (!this.condition.check(player, null))
            {
                return false;
            }
        }
        return ingredients.check(player, matrix);
    }

    public final ItemStack getPreview(Player player, BlockState block)
    {
        if (this.preview == null)
        {
            return this.getResult(player, block);
        }
        return this.preview.getResult(player, block, null);
    }

    public static <R extends Recipe> boolean isMatching(R r1, R r2)
    {
        if (r1 instanceof ShapelessRecipe)
        {
            if (((ShapelessRecipe)r1).getIngredientList().equals(
                ((ShapelessRecipe)r2).getIngredientList()))
            {
                return true;
            }
        }
        else if (r1 instanceof ShapedRecipe)
        {
            String[] checkShape = ((ShapedRecipe)r1).getShape();
            String[] myShape = ((ShapedRecipe)r2).getShape();
            Map<Character, ItemStack> checkMap = ((ShapedRecipe)r1).getIngredientMap();
            Map<Character, ItemStack> myMap = ((ShapedRecipe)r2).getIngredientMap();
            try
            {
                for (int i = 0; i < checkShape.length; i++)
                {
                    for (int j = 0; j < checkShape[i].length(); j++)
                    {
                        ItemStack checkItem = checkMap.get(checkShape[i].charAt(j));
                        ItemStack myItem = myMap.get(myShape[i].charAt(j));
                        if (checkItem != myItem)
                        {
                            if (checkItem == null || !checkItem.getData().equals(myItem.getData()))
                            {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
            catch (IndexOutOfBoundsException ignore) // wrong shape
            {}
        }
        return false;
    }

    public final boolean matchesRecipe(Recipe checkRecipe)
    {
        for (Recipe bRecipe : this.bukkitRecipes)
        {
            if (bRecipe.getClass().isAssignableFrom(checkRecipe.getClass())) // same type of recipe
            {
                if (checkRecipe.getResult().isSimilar(bRecipe.getResult()))
                {
                    if (isMatching(bRecipe, checkRecipe))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getSize()
    {
        return this.size;
    }

    // TODO possibility to prevent shift-crafting
}
