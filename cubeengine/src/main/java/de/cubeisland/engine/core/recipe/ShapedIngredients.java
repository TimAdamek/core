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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.util.math.BlockVector2;

public class ShapedIngredients implements WorkbenchIngredients
{
    private final String[] shape;
    private final BlockVector2 size;
    private final Map<Character, Ingredient> ingredientMap;

    public ShapedIngredients(String... shape)
    {
        this.shape = shape;
        int width = 0;
        for (String s : shape)
        {
            if (width < s.length())
            {
                width = s.length();
            }
        }
        this.size = new BlockVector2(width, shape.length);
        ingredientMap = new HashMap<>();
        for (String s : shape)
        {
            for (char c : s.toCharArray())
            {
                ingredientMap.put(c, null);
            }
        }
    }

    public final ShapedIngredients setIngredient(char c, Ingredient ingredient)
    {
        if (ingredientMap.keySet().contains(c))
        {
            ingredientMap.put(c, ingredient);
        }
        else
        {
            throw new IllegalArgumentException("Invalid Character! " + c);
        }
        return this;
    }

    @Override
    public boolean check(Player player, ItemStack[] matrix)
    {
        return checkShape(player, matrix, getSize(matrix)) != null;
    }

    private BlockVector2 checkShape(Player player, ItemStack[] matrix, BlockVector2 size)
    {
        if (size.equals(this.size))
        {
            for (int x = 0 ; x <= 3 - size.x ; x++)
            {
                for (int z = 0; z <= 3 - size.z; z++)
                {
                    if (this.checkShape(player, matrix, x, z))
                    {
                        return new BlockVector2(x, z);
                    }
                    // else check next possible pattern
                }
            }
        }
        return null;
    }

    private boolean checkShape(Player player, ItemStack[] matrix, int xOffset, int zOffset)
    {
        for (int x = 0; x < this.size.x - 1; x++)
        {
            for (int z = 0; z < this.size.z - 1; z++)
            {
                ItemStack item = matrix[3 * (zOffset + z) + xOffset + x];
                Ingredient ingredient = this.getIngredientAt(x, z);
                if (item == null && ingredient != null || ingredient == null || !ingredient.check(player, item))
                {
                    if (item != null && item.getType() == Material.AIR && ingredient == null)
                    {
                        continue;
                    }
                    return false;
                }
                // else correct ingredient -> check next
            }
        }
        return true;
    }

    private Ingredient getIngredientAt(int x, int z)
    {
        return ingredientMap.get(this.shape[z].charAt(x));
    }

    protected static BlockVector2 getSize(ItemStack[] matrix)
    {
        int lowX = -1;
        int lowY = -1;
        int highX = -1;
        int highY = -1;
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                if (matrix[i*3 + j] != null)
                {
                    if (lowX == -1 || i < lowX)
                    {
                        lowX = i;
                    }
                    if (lowY == -1 || i < lowY)
                    {
                        lowY = j;
                    }
                    if (highX == -1 || i > highX)
                    {
                        highX = i;
                    }
                    if (highY == -1 || i > highY)
                    {
                        highY = j;
                    }
                }
            }
        }
        if (lowX == -1)
        {
            throw new IllegalArgumentException("Empty Matrix!");
        }
        return new BlockVector2(highX-lowX+1, highY-lowY+1);
    }

    @Override
    public Set<Recipe> getBukkitRecipes(MaterialData resultMaterial)
    {
        Set<Recipe> recipes = new HashSet<>();
        Set<Map<Character, MaterialData>> endMaps = new HashSet<>();
        endMaps.add(new HashMap<Character, MaterialData>());
        Set<Map<Character, MaterialData>> tempMaps;
        for (Entry<Character, Ingredient> entry : this.ingredientMap.entrySet())
        {
            tempMaps = new HashSet<>();
            if (entry.getValue() != null)
            {
                for (MaterialData material : entry.getValue().getMaterials())
                {
                    for (Map<Character, MaterialData> materials : endMaps)
                    {
                        Map<Character, MaterialData> mat = new HashMap<>(materials);
                        mat.put(entry.getKey(), material);
                        tempMaps.add(mat);
                    }
                }
                endMaps = tempMaps;
            }

        }
        for (Map<Character, MaterialData> mats : endMaps)
        {
            ItemStack result = resultMaterial.toItemStack();
            result.setAmount(1);
            ShapedRecipe shapedRecipe = new ShapedRecipe(new ItemStack(result)).shape(this.shape);
            for (Entry<Character, MaterialData> entry : mats.entrySet())
            {
                shapedRecipe.setIngredient(entry.getKey(), entry.getValue());
            }
            recipes.add(shapedRecipe);
        }
        return recipes;
    }

    @Override
    public Map<Integer, ItemStack> getIngredientResults(Player player, BlockState block, ItemStack[] matrix)
    {
        Map<Integer, ItemStack> map = new HashMap<>();
        BlockVector2 offSet = this.checkShape(player, matrix, getSize(matrix));
        if (offSet == null)
        {
            throw new InvalidIngredientsException();
        }
        for (int x = 0; x < this.size.x - 1; x++)
        {
            for (int z = 0; z < this.size.z - 1; z++)
            {
                ItemStack item = matrix[3 * (offSet.x + x) + offSet.z + z];
                if (item != null)
                {
                    Ingredient ingredient = this.getIngredientAt(x, z);
                    if (ingredient != null)
                    {
                        map.put(3 * (offSet.x + x) + offSet.z + z, ingredient.getResult(player, block, item.clone()));
                    } // else ignore because empty
                }
                // else no ingredient here
            }
        }
        return map;
    }

    @Override
    public int getSize()
    {
        int result = 0;
        for (String s : this.shape)
        {
            for (char c : s.toCharArray())
            {
                if (this.ingredientMap.get(c) != null)
                {
                    result++;
                }
            }
        }
        return result;
    }
}
