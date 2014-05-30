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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import org.apache.commons.lang.Validate;

public class ShapelessIngredients implements WorkbenchIngredients
{
    private List<Ingredient> ingredients = new ArrayList<>();

    public ShapelessIngredients(Ingredient... ingredients)
    {
        for (Ingredient ingredient : ingredients)
        {
            this.addIngredient(ingredient);
        }
    }

    public final ShapelessIngredients addIngredient(Ingredient ingredient)
    {
        Validate.isTrue(ingredients.size() < 9, "Shapeless recipes cannot have more than 9 ingredients");
        ingredients.add(ingredient);
        return this;
    }

    @Override
    public boolean check(Player player, ItemStack[] matrix)
    {
        for (Ingredient ingredient : ingredients)
        {
            int index = ingredient.find(player, matrix);
            if (index == -1)
            {
                return false;
            }
            matrix[index] = null;
        }
        return true;
    }

    @Override
    public Set<Recipe> getBukkitRecipes(MaterialData resultMaterial)
    {
        Set<Recipe> recipes = new HashSet<>();
        Set<List<MaterialData>> endSets = new HashSet<>();
        endSets.add(new ArrayList<MaterialData>());
        Set<List<MaterialData>> tempSets;
        for (Ingredient ingredient : ingredients)
        {
            tempSets = new HashSet<>();
            for (MaterialData material : ingredient.getMaterials())
            {
                for (List<MaterialData> materials : endSets)
                {
                    List<MaterialData> mat = new ArrayList<>(materials);
                    mat.add(material);
                    tempSets.add(mat);
                }
            }
            endSets = tempSets;
        }
        for (List<MaterialData> materials : endSets)
        {
            ItemStack result = resultMaterial.toItemStack();
            result.setAmount(1);
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(result);
            for (MaterialData material : materials)
            {
                shapelessRecipe.addIngredient(1, material);
            }
            recipes.add(shapelessRecipe);
        }
        return recipes;
    }

    @Override
    public Map<Integer, ItemStack> getIngredientResults(Player player, BlockState block, ItemStack[] matrix)
    {
        Map<Integer, ItemStack> map = new HashMap<>();
        for (Ingredient ingredient : ingredients)
        {
            int index = ingredient.find(player, matrix);
            if (index == -1)
            {
                throw new InvalidIngredientsException();
            }
            ItemStack result = ingredient.getResult(player, block, matrix[index].clone());
            if (result != null)
            {
                map.put(index, result);
            } // else ignore
            matrix[index] = null;
        }
        return map;
    }

    @Override
    public int getSize()
    {
        return this.ingredients.size();
    }
}
