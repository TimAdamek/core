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
import java.util.Map.Entry;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class Craft
{
    private final Player crafter;
    private final ItemStack[] startMatrix;
    private final ItemStack[] startInventory;
    private final CubeWorkbenchRecipe recipe;
    private final boolean isShift;

    private boolean finished = false;

    BlockState block = null; // TODO

    private int prepared = 0;

    public Craft(ItemStack[] startMatrix, Player crafter, CubeWorkbenchRecipe recipe, boolean isShift)
    {
        this.startMatrix = deepClone(startMatrix);
        this.startInventory = deepClone(crafter.getInventory().getContents());
        this.crafter = crafter;
        this.recipe = recipe;
        this.isShift = isShift;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public static ItemStack[] deepClone(ItemStack[] matrix)
    {
        ItemStack[] clone = matrix.clone();
        for (int i = 0 ; i < clone.length ; i++)
        {
            if (clone[i] != null)
            {
                clone[i] = clone[i].clone();
            }
        }
        return clone;
    }

    public void finish()
    {
        this.finished = true;
    }

    public ItemStack prepareItemCraft(ItemStack result)
    {
        prepared++;
        if (prepared % recipe.getSize() == 0)
        {
            if (!this.reduceMyMatrix())
            {
                this.finish();
                System.out.print("Craft End (prep)");
                return null;
            }
            System.out.print("next Craft");
            return recipe.getResult(crafter, null);
        }
        // else removed one from matrix
        return result;
    }

    private boolean reduceMyMatrix()
    {
        try
        {

            // TODO retain items put in inventory on shift
            Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(crafter, block, startMatrix);
            for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
            {
                if (entry.getValue() != null)
                {
                    startMatrix[entry.getKey()] = entry.getValue();
                }
            }

            for (ItemStack item : startMatrix) // reduce
            {
                if (item != null)
                {
                    int amount = item.getAmount() - 1;
                    item.setAmount(amount < 0 ? 0 : amount);
                }
            }

        }
        catch (InvalidIngredientsException e)
        {
            System.out.print("Not enough items! END Crafting");
            return false;
        }
        return true;
    }

    public void finalize(CraftingInventory inventory)
    {
        this.finish();
        if (isShift || prepared < recipe.getSize())
        {
            System.out.println("last Craft");
            this.reduceMyMatrix();
        }
        System.out.print("Craft End " + isShift + prepared);
        showMatrix(startMatrix);
        inventory.setMatrix(startMatrix);
    }



    static void showMatrix(ItemStack[] matrix)
    {
        String s = "";
        int i = 0;
        for (ItemStack itemStack : matrix)
        {
            if (itemStack == null)
            {
                s += "[ x0]";
            }
            else
            {
                s += "[" + itemStack.getType().name().substring(0,1) + "x" + itemStack.getAmount() + "]";
            }
            i++;
            if (i % 3 == 0)
            {
                System.out.println(s);
                s = "";
            }
        }
        if (!s.isEmpty())
        {
            System.out.println("R: " + s);
        }
    }
}
