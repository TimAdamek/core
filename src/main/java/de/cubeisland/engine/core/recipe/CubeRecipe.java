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
import java.util.Set;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;
import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;
import de.cubeisland.engine.core.recipe.result.logic.Result;

/**
 * Represents some type of crafting recipe.
 */
public abstract class CubeRecipe<T extends Ingredients>
{
    protected T ingredients;
    protected Condition condition;
    protected Result result;
    protected Effect effect;

    protected Set<org.bukkit.inventory.Recipe> bukkitRecipes;
    private boolean allowOldRecipe = false;

    public CubeRecipe(T ingredients, Result result)
    {
        this.ingredients = ingredients;
        this.result = result;
    }

    public CubeRecipe withCondition(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    public final Set<org.bukkit.inventory.Recipe> getBukkitRecipes()
    {
        if (bukkitRecipes == null)
        {
            bukkitRecipes = ingredients.getBukkitRecipes(this.getResultMaterial());
        }
        return bukkitRecipes;
    }

    protected final MaterialData getResultMaterial()
    {
        if (result instanceof MaterialProvider)
        {
            LinkedList<MaterialData> materials = ((MaterialProvider)result).getMaterials(new LinkedList<MaterialData>());
            if (!materials.isEmpty())
            {
                return materials.iterator().next();
            }
        }
        throw new IllegalStateException("Recipe has no Material as Result");
    }

    public final ItemStack getResult(Player player, BlockState block)
    {
        return result.getResult(player, block, null);
    }

    public final void runEffects(Core core, Player player)
    {
        if (effect == null)
        {
            return;
        }
        this.effect.runEffect(core, player);
    }

    /**
     * Defaulted to false
     *
     * @param b
     * @return
     */
    public final CubeRecipe<T> allowOldRecipe(boolean b)
    {
        this.allowOldRecipe = b;
        return this;
    }

    public boolean isOldRecipeAllowed()
    {
        return allowOldRecipe;
    }

    public final T getIngredients()
    {
        return ingredients;
    }
}
