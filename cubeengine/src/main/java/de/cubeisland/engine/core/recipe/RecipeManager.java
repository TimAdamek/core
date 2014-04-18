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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.Pair;

import static org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY;
import static org.bukkit.event.inventory.InventoryAction.NOTHING;

public class RecipeManager implements Listener
{
    protected Map<Module, Set<CubeRecipe>> recipes = new HashMap<>();
    protected Set<CubeWorkbenchRecipe> workbenchRecipes = new HashSet<>();
    protected Set<CubeFurnaceRecipe> furnaceRecipes = new HashSet<>();

    protected Map<CubeRecipe, Pair<Set<Recipe>, Boolean>> replaceMap = new HashMap<>();

    private final FurnaceManager furnaceManager;

    protected Core core;

    public RecipeManager(Core core)
    {
        this.core = core;
        this.furnaceManager = new FurnaceManager(this);
    }

    public void init()
    {
        this.core.getEventManager().registerListener(core.getModuleManager().getCoreModule(), this);
        this.core.getEventManager().registerListener(core.getModuleManager().getCoreModule(), this.furnaceManager);
    }

    @SuppressWarnings("unchecked")
    public void registerRecipe(Module module, CubeRecipe recipe)
    {
        Set<Recipe> bukkitRecipes = recipe.getBukkitRecipes();
        Set<Recipe> oldRecipes = this.removeBukkitRecipe(recipe);
        if (!oldRecipes.isEmpty())
        {
            Pair<Set<Recipe>, Boolean> prev = replaceMap.put(recipe, new Pair<>(oldRecipes, recipe.isOldRecipeAllowed()));
            if (prev != null)
            {
                module.getLog().warn("A Recipe with the same footprint has been registered already!");
            }
            System.out.print("Replaced old Recipes");
            for (Recipe oldRecipe : oldRecipes)
            {
                System.out.print(" - " + oldRecipe.getResult());
            }
        }
        System.out.print("Registered new Recipe");
        for (Recipe bukkitRecipe : bukkitRecipes)
        {
            Bukkit.getServer().addRecipe(bukkitRecipe);
            System.out.print(" - " + bukkitRecipe.getResult());
        }
        this.getRecipes(module).add(recipe);
        if (recipe instanceof CubeWorkbenchRecipe)
        {
            this.workbenchRecipes.add((CubeWorkbenchRecipe)recipe);
        }
        else if (recipe instanceof CubeFurnaceRecipe)
        {
            this.furnaceRecipes.add((CubeFurnaceRecipe)recipe);
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    protected Set<Recipe> removeBukkitRecipe(CubeRecipe recipe)
    {
        Set<Recipe> removed = new HashSet<>();
        Iterator<Recipe> it = Bukkit.getServer().recipeIterator();
        while (it.hasNext())
        {
            Recipe oldRecipe = it.next();
            if (oldRecipe instanceof FurnaceRecipe && recipe instanceof CubeFurnaceRecipe)
            {
                if (((CubeFurnaceRecipe)recipe).matchesRecipe(((FurnaceRecipe)oldRecipe).getInput()))
                {
                    it.remove();
                    removed.add(oldRecipe);
                }
            }
            else if ((oldRecipe instanceof ShapedRecipe || oldRecipe instanceof ShapelessRecipe) && recipe instanceof CubeWorkbenchRecipe)
            {
                if (((CubeWorkbenchRecipe)recipe).matchesRecipe(oldRecipe))
                {
                    it.remove();
                    removed.add(oldRecipe);
                }
            }
        }
        return removed;
    }

    public void unregisterRecipe(Module module, CubeRecipe recipe)
    {
        this.getRecipes(module).remove(recipe);
        if (recipe instanceof CubeWorkbenchRecipe)
        {
            this.workbenchRecipes.remove(recipe);
        }
        else if (recipe instanceof CubeFurnaceRecipe)
        {
            this.furnaceRecipes.remove(recipe);
        }
        this.finishUnregister(recipe);
    }

    private void finishUnregister(CubeRecipe recipe)
    {
        System.out.print("Unregistered Recipe");
        this.removeBukkitRecipe(recipe);
        Pair<Set<Recipe>, Boolean> pair = replaceMap.get(recipe);
        if (pair != null)
        {
            System.out.print("Reregistered previous Recipes");
            for (Recipe prevRecipe : pair.getLeft())
            {
                System.out.print(" - "+ prevRecipe.getResult());
                Bukkit.getServer().addRecipe(prevRecipe);
            }
        }
    }

    public void unregisterAllRecipes(Module module)
    {
        Set<CubeRecipe> remove = this.recipes.remove(module);
        this.workbenchRecipes.removeAll(remove);
        this.furnaceRecipes.removeAll(remove);
        for (CubeRecipe recipe : remove)
        {
            this.finishUnregister(recipe);
        }
    }

    public void unregisterAllRecipes()
    {
        this.recipes.clear();
        for (CubeRecipe recipe : this.workbenchRecipes)
        {
            this.finishUnregister(recipe);
        }
        for (CubeRecipe recipe : this.furnaceRecipes)
        {
            this.finishUnregister(recipe);
        }
    }

    private Set<CubeRecipe> getRecipes(Module module)
    {
        Set<CubeRecipe> recipeSet = this.recipes.get(module);
        if (recipeSet == null)
        {
            recipeSet = new HashSet<>();
            this.recipes.put(module, recipeSet);
        }
        return recipeSet;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event)
    {
        if (event.getViewers().size() > 1)
        {
            event.getInventory().setResult(null);
            CubeEngine.getLog().warn("Aborted PrepareItemCraftEvent because {} players were looking into the same CraftingInventory!", event.getViewers().size());
            return;
        }
        HumanEntity player = event.getViewers().get(0);
        if (player instanceof Player)
        {
            for (CubeWorkbenchRecipe recipe : workbenchRecipes)
            {
                if (recipe.matchesRecipe(event.getRecipe()))
                {
                    prepare(event, event.getInventory().getMatrix(), (Player)player, recipe);
                    return;
                }
            }
            return;
        }
        CubeEngine.getLog().warn("A non Player tried to craft");
    }

    private void prepare(PrepareItemCraftEvent event, ItemStack[] matrix, Player player, CubeWorkbenchRecipe recipe)
    {
        if (recipe.matchesConditions(player, matrix))
        {
            Craft craft = this.crafting.get(player);
            if (craft == null)
            {
                System.out.println("Prepare Craft");
                event.getInventory().setResult(recipe.getPreview(player, null)); // TODO block
                return;
            }
            if (!craft.isFinished())
            {
                event.getInventory().setResult(craft.prepareItemCraft(event.getInventory().getResult()));
                return;
            }
        }

        Pair<Set<Recipe>, Boolean> pair = this.replaceMap.get(recipe);
        if (pair.getRight())
        {
            ItemStack result = null;
            for (Recipe check : pair.getLeft())
            {
                if (CubeWorkbenchRecipe.isMatching(check, event.getRecipe()))
                {
                    if (result != null)
                    {
                        core.getLog().warn("Custom Recipe has multiple valid fallback!");
                    }
                    result = check.getResult();
                }
            }
            event.getInventory().setResult(result);
            System.out.print("Used FallBack Recipe");
            return;
        }
        System.out.print("No more match!");
    }

    private ItemStack[] reduceMatrix(ItemStack[] matrix)
    {
        if (matrix == null)
        {
            return null;
        }
        for (ItemStack item : matrix) // reduce
        {
            if (item != null)
            {
                int amount = item.getAmount() - 1;
                item.setAmount(amount < 0 ? 0 : amount);
            }
        }
        return matrix;
    }

    private boolean reduceMyMatrix(ItemStack[] matrix, CubeWorkbenchRecipe recipe, Player player, BlockState block)
    {
        try
        {
            this.reduceMatrix(matrix);
            Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(player, block, matrix);
            for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
            {
                if (entry.getValue() != null)
                {
                    matrix[entry.getKey()] = entry.getValue();
                }
            }
        }
        catch (InvalidIngredientsException e)
        {
            System.out.print("STOP Shift CRAFT");
            return false;
        }
        return true;
    }

    private Map<Player, Craft> crafting = new HashMap<>();






    @EventHandler
    public void onItemCraft(CraftItemEvent event)
    {
        HumanEntity player = event.getWhoClicked();
        if (event.getAction() == NOTHING)
        {
            return;
        }
        if (player instanceof Player)
        {
            for (CubeWorkbenchRecipe recipe : workbenchRecipes)
            {
                if (recipe.matchesRecipe(event.getRecipe()))
                {
                    this.craft(event, (Player)player, recipe);
                    return;
                }
            }
            return;
        }
        CubeEngine.getLog().warn("A non Player tried to craft");
    }

    private void craft(CraftItemEvent event, final Player player, final CubeWorkbenchRecipe recipe)
    {
        CraftingInventory table = event.getInventory();
        System.out.println("Craft");
        Craft.showMatrix(table.getMatrix());
        if (recipe.matchesConditions(player, table.getMatrix()))
        {
            this.crafting.put(player, new Craft(table.getMatrix(), player, recipe, event.getAction() == MOVE_TO_OTHER_INVENTORY));
            table.setResult(recipe.getResult(player, null)); // TODO block
            final Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(player, null, table.getMatrix()); // TODO block
            if (!ingredientResults.isEmpty() || event.getAction() == MOVE_TO_OTHER_INVENTORY)
            {
                recipe.runEffects(core, player);
                final CraftingInventory inventory = table;
                core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                                                     new Runnable()
                                                     {
                                                         @Override
                                                         public void run()
                                                         {
                                                             Craft craft = crafting.remove(player);
                                                             craft.finalize(inventory);
                                                         }
                                                     }, 1);
            }
            core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                                                 new Runnable()
                                                 {
                                                     @Override
                                                     public void run()
                                                     {
                                                         player.updateInventory();
                                                     }
                                                 }, 2);

            return;
        }
        Pair<Set<Recipe>, Boolean> pair = this.replaceMap.get(recipe);
        if (pair.getRight())
        {
            ItemStack result = null;
            for (Recipe check : pair.getLeft())
            {
                if (CubeWorkbenchRecipe.isMatching(check, event.getRecipe()))
                {
                    if (result != null)
                    {
                        core.getLog().warn("Custom Recipe has multiple valid fallback!");
                    }
                    result = check.getResult();
                }
            }
            table.setResult(result);
            System.out.print("Used FallBack Recipe");
        }
    }

    public void shutdown()
    {
        this.core.getEventManager().removeListener(core.getModuleManager().getCoreModule(), this);
        this.furnaceManager.shutdown();
        this.unregisterAllRecipes();
    }
}
