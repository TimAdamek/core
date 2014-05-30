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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.util.InventoryUtil;

import static de.cubeisland.engine.core.util.InventoryUtil.deepClone;

public class CraftingHandler
{
    private final Player crafter;
    private final ItemStack[] startMatrix;
    private final CubeWorkbenchRecipe recipe;
    private final boolean isShift;
    private final Core core;
    private final CraftingInventory table;

    private boolean finished = false;

    BlockState block = null; // TODO set the block

    private int prepared = 0;

    public CraftingHandler(Core core, CraftItemEvent event, CraftingInventory table, Player player, CubeWorkbenchRecipe recipe, boolean isShift)
    {
        this.core = core;
        this.table = table;
        this.startMatrix = deepClone(table.getMatrix());
        this.crafter = player;
        this.recipe = recipe;
        this.isShift = isShift;

        if (this.consume())
        {
            table.setResult(recipe.getResult(player, block));
            core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(), new Runnable()
            {
                @Override
                public void run()
                {
                    crafter.updateInventory();
                }
            }, 2);
        }
        else
        {
            event.setCancelled(true);
            System.out.println("Craft Cancelled");
        }
    }

    public boolean isFinished()
    {
        return finished;
    }

    private void finish()
    {
        this.finished = true;
    }

    public ItemStack prepareItemCraft(ItemStack noChange)
    {
        prepared++;
        if (prepared % recipe.getSize() == 0)
        {
            ItemStack result;
            if (isShift)
            {
                result = recipe.getResult(crafter, block);
                if (!InventoryUtil.checkForPlace(crafter.getInventory(), result.clone()))
                {
                    System.out.print("Craft End (prep inv full)");
                    return null;
                }
                if (!this.consume())
                {
                    this.finish();
                    System.out.print("Craft End (prep not enough)");
                    return null;
                }
                System.out.print("next Craft");
            }
            else
            {
                result = recipe.getPreview(crafter, block);
            }
            return result;
        }
        // else removed one from matrix
        return noChange;
    }

    private boolean consume()
    {
        try
        {
            System.out.println("consume items");
            Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(crafter, block, startMatrix);
            for (ItemStack item : startMatrix) // reduce
            {
                if (item != null)
                {
                    int amount = item.getAmount() - 1;
                    item.setAmount(amount < 0 ? 0 : amount);
                }
            }
            for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
            {
                if (entry.getValue() != null)
                {
                    startMatrix[entry.getKey()] = entry.getValue();
                }
            }
        }
        catch (RecipeException e)
        {
            System.out.print("force END Crafting");
            return false;
        }
        return true;
    }

    public void finishCrafting()
    {
        System.out.print("Craft End " + isShift + prepared);

        this.finish();
        recipe.runEffects(core, crafter);
        table.setMatrix(startMatrix);
    }
}
