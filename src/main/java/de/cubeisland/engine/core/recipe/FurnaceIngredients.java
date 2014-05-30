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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.util.Pair;

public class FurnaceIngredients implements Ingredients
{
    private Ingredient smeltable;
    private final List<FuelIngredient> fuels;

    public FurnaceIngredients(Ingredient smeltable, FuelIngredient... fuels)
    {
        this.smeltable = smeltable;
        this.fuels = Arrays.asList(fuels);
    }

    public FurnaceIngredients(Ingredient smeltable)
    {
        this.smeltable = smeltable;
        this.fuels = Collections.emptyList();
    }

    public boolean isSmeltable(ItemStack check)
    {
        return this.smeltable.check(null, check);
    }

    public Pair<FuelIngredient, Boolean> matchFuelIngredient(ItemStack fuel, ItemStack smeltable)
    {
        if (this.isSmeltable(smeltable))
        {
            if (fuels.isEmpty()) // ignore fuel
            {
                return new Pair<>(null , true);
            }
            else
            {
                for (FuelIngredient fuelIngredient : this.fuels)
                {
                    if (fuelIngredient.ingredient.check(null, fuel))
                    {
                        return new Pair<>(fuelIngredient, true);
                    }
                }
            }
            return new Pair<>(null, false); // No burning
        }
        else
        {
            return null;
        }
    }

    @Override
    public Set<Recipe> getBukkitRecipes(MaterialData resultMaterial)
    {
        Set<Recipe> recipes = new HashSet<>();
        ItemStack result = resultMaterial.toItemStack();
        result.setAmount(1);
        for (MaterialData ingredient : this.smeltable.getMaterials())
        {
            recipes.add(new FurnaceRecipe(result, ingredient));
        }
        return recipes;
    }

    public ItemStack getIngredientResult(ItemStack bukkitResult, BlockState block)
    {
        return this.smeltable.getResult(null, block, new ItemStack(bukkitResult));
    }

    public boolean hasFuel(FuelIngredient customFuel)
    {
        if (customFuel == null)
        {
            return this.fuels.isEmpty();
        }
        return this.fuels.contains(customFuel);
    }
}
