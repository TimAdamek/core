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
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.module.CoreModule;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.Triplet;

public class FurnaceManager implements Listener
{
    // TODO allow smelting only ONCE then need to take out (set result direct no preview)
    // TODO recipe fallbacks
    protected final CoreModule coreModule;
    protected RecipeManager manager;

    protected Map<Location, FuelIngredient> fuelMap = new HashMap<>();
    protected Map<Location, CustomSmelting> smeltMap = new HashMap<>();

    protected Map<Location, Triplet<CustomSmelting, ItemStack, Integer>> resultMap = new HashMap<>();

    public FurnaceManager(RecipeManager manager)
    {
        this.manager = manager;
        this.coreModule = this.manager.core.getModuleManager().getCoreModule();
    }

    protected CubeFurnaceRecipe findRecipeFor(Furnace furnace)
    {
        FuelIngredient fuel = this.getCurrentFuel(furnace); // if null No custom Fuel OR empty
        ItemStack smelting = furnace.getInventory().getSmelting();
        for (CubeFurnaceRecipe recipe : this.matchRecipes(smelting))
        {
            if (recipe.ingredients.hasFuel(fuel))
            {
                if (recipe.ingredients.isSmeltable(smelting))
                {
                    return recipe;
                }
            }
        }
        return null;
    }

    protected Set<CubeFurnaceRecipe> matchRecipes(ItemStack smelting)
    {
        Set<CubeFurnaceRecipe> result = new HashSet<>();
        for (CubeFurnaceRecipe recipe : this.manager.furnaceRecipes)
        {
            if (recipe.matchesRecipe(smelting))
            {
                result.add(recipe);
            }
        }
        return result;
    }

    public FuelIngredient getCurrentFuel(Furnace furnace)
    {
        if (furnace.getBurnTime() <= 0)
        {
            this.fuelMap.remove(furnace.getLocation());
            return null;
        }
        return this.fuelMap.get(furnace.getLocation());
    }

    @EventHandler
    public void onBurn(FurnaceBurnEvent event)
    {
        if (event.getBlock().getState() instanceof Furnace)
        {
            final Location location = event.getBlock().getLocation();
            final Furnace furnace = (Furnace)event.getBlock().getState();
            fuelMap.remove(location);
            boolean invalidRecipe = false;
            for (CubeFurnaceRecipe recipe : this.matchRecipes(furnace.getInventory().getSmelting()))
            {
                Pair<FuelIngredient, Boolean> fuel = recipe.ingredients.matchFuelIngredient(event.getFuel(),
                                                                    furnace.getInventory().getSmelting());
                if (fuel != null && fuel.getRight())
                {
                    if (furnace.getInventory().getResult() != null && !furnace.getInventory().getResult().isSimilar(recipe.getPreview(null, furnace)))
                    {
                        System.out.print("Try new Fuel | Result Blocked");
                        return;
                    }
                    event.setBurnTime(fuel.getLeft().fuelTicks);
                    fuelMap.put(location, fuel.getLeft());
                    System.out.print("New Fuel | BurnTime +" + event.getBurnTime());
                    this.startSmelting(recipe, furnace, fuel.getLeft(), event.getBurnTime());
                    // TODO fuelResult
                    return;
                }
                invalidRecipe = true;
            }
            if (invalidRecipe)
            {
                event.setCancelled(true);
            }
        }
    }

    private void startSmelting(final CubeFurnaceRecipe recipe, final Furnace furnace, final FuelIngredient fuel, int furnaceBurnTime)
    {
        Location location = furnace.getLocation();
        CustomSmelting smelting = smeltMap.get(location);
        if (smelting == null)
        {
            smeltMap.put(location, new CustomSmelting(FurnaceManager.this, furnace, recipe, fuel)
                .updateLastFuelTick(furnaceBurnTime));
            System.out.print("#" +smeltMap.get(location).N + " Start Smelt | Fuel " + furnaceBurnTime + " | " + smeltMap.get(location).curSmeltTime +
                "/" + smeltMap.get(location).totalSmeltTime);
        }
        else
        {
            if (smelting.isDone())
            {
                smelting.updateForNewRecipe(furnace, recipe, fuel);
                System.out.print("#" + smelting.N + " ReStart Smelt | Fuel " + furnaceBurnTime +" | " + smelting.curSmeltTime + "/" + smelting.totalSmeltTime);
                smelting.updateLastFuelTick(furnaceBurnTime);
            }
            else
            {
                System.out.print("#" + smelting.N + " Continue Smelt | Fuel " + furnaceBurnTime +" | " + smelting.curSmeltTime + "/" + smelting.totalSmeltTime);
                smelting.updateLastFuelTick(furnaceBurnTime + smelting.getLastFuelTick());
            }

        }
    }

    private void restartSmelting(final CubeFurnaceRecipe recipe, final Furnace furnace, final Location location, final int endFuel)
    {
        if (endFuel <= 0)
        {
            System.out.print("Waiting for new Fuel to restart!");
            return;
        }
        final CustomSmelting smelt = smeltMap.get(location);
        if (smelt == null || smelt.isDone())
        {
            this.manager.core.getTaskManager().runTask(coreModule,
               new Runnable()
               {
                   @Override
                   public void run()
                   {
                       if (recipe.ingredients.isSmeltable(furnace.getInventory().getSmelting()))
                       {
                           startSmelting(recipe, furnace, fuelMap.get(location), endFuel);
                       }
                       else
                       {
                           // TODO search recipe if not found noSmelting
                           if (furnace.getInventory().getSmelting() != null)
                           {
                               smeltMap.remove(location);
                               preventSmelting(furnace);
                           }
                           System.out.print("No Restart!");
                       }
                   }
               });
        }
    }

    protected void preventSmelting(Furnace furnace)
    {
        new NoSmelting(furnace);
    }

    protected void preventSmelting(Furnace furnace, ItemStack itemStack)
    {
        new NoSmelting(furnace, itemStack);
    }

    public void shutdown()
    {
        this.coreModule.getCore().getEventManager().removeListener(coreModule, this);
        // TODO finish results when unloading / stopping
    }

    private class NoSmelting
    {
        private final ItemStack smelt;
        private final Furnace furnace;
        private final short EMPTY = 0;

        private Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                if (furnace.getBurnTime() <= 0)
                {
                    return;
                }
                if (smelt.equals(furnace.getInventory().getSmelting()))
                {
                    furnace.setCookTime(EMPTY);
                    manager.core.getTaskManager().runTaskDelayed(coreModule, this, 1);
                }
                else System.out.println("No Smelting STOPPED");
            }
        };

        private NoSmelting(Furnace furnace)
        {
            this(furnace, furnace.getInventory().getSmelting());
        }

        private NoSmelting(Furnace furnace, ItemStack smelt)
        {
            System.out.println("No Smelting STARTED");
            this.smelt = smelt.clone();
            this.furnace = furnace;
            manager.core.getTaskManager().runTaskDelayed(coreModule, runner, 1);
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event)
    {
        if (event.getBlock().getState() instanceof Furnace)
        {
            final Furnace furnace = (Furnace)event.getBlock().getState();
            final Location location = furnace.getLocation();
            CustomSmelting smelt = this.smeltMap.remove(location);
            if (smelt == null)
            {
                if (fuelMap.get(location) != null)
                {
                    System.out.print("Custom Fuel but no Custom Smelting detected!");
                    this.preventSmelting(furnace);
                    event.setCancelled(true);

                    CubeFurnaceRecipe newRecipe = this.findRecipeFor(furnace);
                    if (newRecipe != null)
                    {
                        System.out.print("Recipe Matched for Custom Fuel... restart");
                        this.restartSmelting(newRecipe, furnace, location, furnace.getBurnTime());
                        return;
                    }
                }
                // TODO vanilla recipe?
                return;
            }
            if (!smelt.recipe.ingredients.isSmeltable(furnace.getInventory().getSmelting()))
            {
                System.out.print("SMELTING NOT SMELTABLE");
                event.setCancelled(true);
                this.preventSmelting(furnace);
                return;
            }
            ItemStack result = smelt.recipe.getPreview(null, furnace);
            if (result == null)
            {
                event.setCancelled(true);
                return;
            }
            smelt.done();
            if (furnace.getInventory().getResult() == null
                || furnace.getInventory().getResult().getType() == Material.AIR
                || furnace.getInventory().getResult().isSimilar(result))
            {
                event.setResult(result);
                Triplet<CustomSmelting, ItemStack, Integer> results = this.resultMap.get(location);
                if (results == null)
                {
                    results = new Triplet<>(smelt, event.getSource().clone(), 0);
                    this.resultMap.put(location, results);
                }
                results.setThird(results.getThird() + 1);
            }
            else
            {
                event.setCancelled(true);
                System.out.println("#" + smelt.N + " RESULT NOT STACKABLE!");
                return;
            }
            System.out.print("### SMELT ###");
            final ItemStack ingredientResult = smelt.recipe.getIngredients().getIngredientResult(event.getSource().clone(), furnace);
            if (ingredientResult != null)
            {
                this.manager.core.getTaskManager().runTask(coreModule,
                   new Runnable()
                   {
                       @Override
                       public void run()
                       {
                           furnace.getInventory().setSmelting(ingredientResult);
                           for (HumanEntity humanEntity : furnace.getInventory().getViewers())
                           {
                               if (humanEntity instanceof Player)
                               {
                                   ((Player)humanEntity).updateInventory();
                               }
                           }
                       }
                   });
            }

            if (furnace.getBurnTime() > 0)
            {
                this.smeltMap.put(location, smelt);
                this.restartSmelting(smelt.recipe, furnace, location, smelt.getEndFuel());
            }
            else
            {
                this.fuelMap.remove(location);
            }
        }
    }

    @EventHandler
    public void onNewSmeltable(InventoryClickEvent event)
    {
        if (event.getInventory() instanceof FurnaceInventory)
        {
            if (event.getInventory().getHolder() instanceof Furnace)
            {
                Furnace furnace = (Furnace)event.getInventory().getHolder();
                if (event.getSlotType() == SlotType.RESULT
                    || (event.getAction() == InventoryAction.COLLECT_TO_CURSOR &&
                event.getCursor().isSimilar(furnace.getInventory().getResult()))) // taking result
                {
                    this.onExtractItem(event, furnace);
                    return;
                }

                if (furnace.getBurnTime() <= 0)
                {
                    return;
                }
                ItemStack oldItem = furnace.getInventory().getSmelting();
                ItemStack newItem = null;
                switch (event.getAction())
                {
                case SWAP_WITH_CURSOR:
                case PLACE_ALL:
                    if (event.getRawSlot() == 0)
                    {
                        if (event.getCurrentItem().isSimilar(event.getCursor()))
                        {
                            newItem = event.getCurrentItem().clone();
                            newItem.setAmount(newItem.getAmount() + event.getCursor().getAmount());
                            if (newItem.getAmount() > newItem.getMaxStackSize())
                            {
                                newItem.setAmount(newItem.getMaxStackSize());
                            }
                            break;
                        }
                        newItem = event.getCursor();
                        break;
                    }
                    return;
                case HOTBAR_SWAP:
                    if (event.getRawSlot() == 0)
                    {
                        newItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                        break;
                    }
                    return;
                case PLACE_ONE:
                    if (event.getRawSlot() != 0)
                    {
                        return;
                    }
                    if (event.getCurrentItem().isSimilar(event.getCursor()))
                    {
                        newItem = event.getCurrentItem().clone();
                        newItem.setAmount(newItem.getAmount() + 1);
                    }
                    else
                    {
                        newItem = event.getCursor().clone();
                        newItem.setAmount(1);
                    }
                    break;
                case PICKUP_ALL:
                    if (event.getRawSlot() == 0)
                    {
                        break;
                    }
                    return;
                case PICKUP_HALF:
                    if (event.getRawSlot() == 0)
                    {
                        newItem = oldItem.clone();
                        newItem.setAmount(oldItem.getAmount() / 2);
                        break;
                    }
                    return;
                case COLLECT_TO_CURSOR:
                    if (oldItem == null)
                    {
                        return;
                    }
                    if (oldItem.isSimilar(event.getCursor()))
                    {
                        int missing = event.getCursor().getMaxStackSize() - event.getCursor().getAmount();
                        if (oldItem.getAmount() <= missing)
                        {
                            newItem = null;
                        }
                        else
                        {
                            newItem = oldItem.clone();
                            newItem.setAmount(newItem.getAmount() - missing);
                        }
                        break;
                    }
                    return;
                case MOVE_TO_OTHER_INVENTORY:
                    if (event.getRawSlot() == 0)
                    {
                        break;
                    }
                    else if (event.getRawSlot() > event.getView().getTopInventory().getSize())
                    {
                        newItem = event.getCurrentItem();
                    }
                    else if (event.getSlotType() == SlotType.RESULT)
                    {
                        return; // shift click out result
                    }
                    break;
                default: return;
                }
                Location location = furnace.getLocation();
                FuelIngredient customFuel = this.fuelMap.get(location);
                CustomSmelting customSmelting = this.smeltMap.get(location);
                if (customSmelting != null)
                {
                    if (customSmelting.recipe.ingredients.isSmeltable(newItem))
                    {
                        System.out.print("Changed smelting | Recipe valid");
                        if (customSmelting.isDone())
                        {
                            System.out.print("but done! Restart...");
                            this.restartSmelting(customSmelting.recipe, furnace, location, furnace.getBurnTime());
                        }
                        return;
                    }
                    for (CubeFurnaceRecipe newRecipe : this.matchRecipes(newItem))
                    {
                        if (newRecipe.ingredients.hasFuel(customFuel))
                        {
                            customSmelting.done();
                            if (newRecipe.ingredients.isSmeltable(newItem))
                            {
                                System.out.print("Changed smelting | Recipe Changed");
                                this.restartSmelting(newRecipe, furnace, location, furnace.getBurnTime());
                            }
                            else
                            {
                                System.out.print("Changed smelting | Recipe Match Incomplete");
                                this.preventSmelting(furnace, newItem);
                            }
                        }
                        else
                        {
                            System.out.print("Changed smelting | Recipe INVALID");
                            event.setCancelled(true);
                            // smelting aborts itself => search new recipe & restart if no recipe STOP progress
                        }
                    }
                    return;
                }
                if (newItem == null)
                {
                    return;
                }
                boolean invalidRecipe = false;
                for (CubeFurnaceRecipe recipe : this.matchRecipes(newItem))
                {
                    if (recipe.ingredients.hasFuel(customFuel) &&
                        recipe.ingredients.isSmeltable(newItem))
                    {
                        System.out.print("New Smeltable");
                        this.restartSmelting(recipe, furnace, location, furnace.getBurnTime());
                        return;
                    }
                    invalidRecipe = true;
                }
                if (invalidRecipe || customFuel != null)
                {
                    System.out.print("No valid Recipe");
                    this.preventSmelting(furnace, newItem);
                }
            }
        }
    }

    public void onExtractItem(final InventoryClickEvent event, Furnace furnace)
    {
        if (!(event.getWhoClicked() instanceof Player))
        {
            return;
        }
        Location location = furnace.getLocation();
        Triplet<CustomSmelting, ItemStack, Integer> smelted = this.resultMap.remove(location);
        if (smelted != null)
        {
            System.out.print("Extract Item Prep");
            int times = smelted.getThird();
            ItemStack result = smelted.getFirst().recipe.getResult((Player)event.getWhoClicked(), furnace);
            result.setAmount(result.getAmount() * times);
            if (event.getSlotType() == SlotType.RESULT)
            {
                switch (event.getAction())
                {
                case PICKUP_HALF:
                case PICKUP_ALL:
                case MOVE_TO_OTHER_INVENTORY:
                case HOTBAR_SWAP:
                    furnace.getInventory().setResult(result);
                }
            }
            else if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR)
            {
                furnace.getInventory().setResult(result);
            }
            else return;
            this.coreModule.getCore().getTaskManager().runTaskDelayed(this.coreModule, new Runnable()
            {
                @Override
                public void run()
                {
                    ((Player)event.getWhoClicked()).updateInventory();
                }
            }, 1);
        }
    }

    @EventHandler
    public void onExtractItem(FurnaceExtractEvent event)
    {
        System.out.print("Extracted Item!");
        if (event.getBlock().getState() instanceof Furnace)
        {
            Furnace furnace = (Furnace)event.getBlock().getState();
            Location location = furnace.getLocation();
            CustomSmelting smelting = this.smeltMap.get(location);
            if (smelting == null || smelting.isDone())
            {
                CubeFurnaceRecipe recipe = this.findRecipeFor(furnace);
                if (recipe != null)
                {
                    this.restartSmelting(recipe, furnace, location, furnace.getBurnTime());
                    return;
                }
            }
            System.out.print("No Restart after extract");
        }
    }

    // TODO intercept putting new items in furnace when smelting a custom recipe

    // exp gained etc
}
