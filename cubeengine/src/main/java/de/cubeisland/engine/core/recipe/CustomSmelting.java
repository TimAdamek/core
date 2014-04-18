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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;

import de.cubeisland.engine.core.module.CoreModule;

import static de.cubeisland.engine.core.recipe.FuelIngredient.DEFAULT_SMELT_TIME;

public class CustomSmelting
{
    private final Location location;
    private final CoreModule coreModule;
    private final FurnaceManager manager;

    protected CubeFurnaceRecipe recipe;
    protected FuelIngredient fuel;
    protected int totalSmeltTime;
    protected int curSmeltTime;
    private float factor;

    private Runnable runner;
    private Integer taskID = null;

    private int lastFuelTick = -1;
    private int endFuel;

    public int getLastFuelTick()
    {
        return lastFuelTick;
    }

    public int N = 0; // TODO make private

    public CustomSmelting(FurnaceManager manager, Furnace furnace, CubeFurnaceRecipe recipe, FuelIngredient fuel)
    {
        this.location = furnace.getLocation();
        this.coreModule = manager.coreModule;
        this.manager = manager;

        this.updateForNewRecipe(furnace, recipe, fuel);
    }

    public CustomSmelting updateForNewRecipe(Furnace furnace, CubeFurnaceRecipe recipe, FuelIngredient fuel)
    {
        this.recipe = recipe;
        this.fuel = fuel;
        this.totalSmeltTime = fuel.smeltTicks;
        this.factor = (float)totalSmeltTime / DEFAULT_SMELT_TIME;
        this.curSmeltTime = 0;
        this.done(); // JUST TO BE SURE
        this.endFuel = furnace.getBurnTime();
        this.lastFuelTick = furnace.getBurnTime();
        this.runSmelting(furnace);
        return this;
    }

    public CustomSmelting updateLastFuelTick(int ticks)
    {
        lastFuelTick = ticks;
        return this;
    }

    public int getEndFuel()
    {
        return endFuel;
    }

    public void done()
    {
        runner = null;
        if (taskID != null)
        {
            this.coreModule.getCore().getTaskManager().cancelTask(this.coreModule, this.taskID);
            this.taskID = null;
            System.out.print("TASK STOPPED");
        }
        N++;
    }

    public boolean isDone()
    {
        return this.runner == null;
    }

    private void runSmelting(final Furnace furnace)
    {
        this.runner = new Runnable()
        {
            private boolean noFuel = false;
            private final int n = N;

            @Override
            public void run()
            {
                if (N != n)
                {
                    done();
                    return;
                }
                if (noFuel || furnace.getInventory().getSmelting() == null || furnace.getInventory().getSmelting().getType() == Material.AIR)
                {
                    manager.smeltMap.remove(location);
                    System.out.print("#" +n + " Abort Smelt | Fuel " + furnace.getBurnTime() + " | " + curSmeltTime + "/" + totalSmeltTime);
                    done();
                    return;
                }
                if (furnace.getBurnTime() <= 0 && lastFuelTick <= 0)
                {
                    this.noFuel = true;
                }
                FuelIngredient customFuel = manager.fuelMap.get(location);
                if (customFuel == fuel)
                {
                    if (!recipe.getIngredients().isSmeltable(furnace.getInventory().getSmelting()))
                    {
                        System.out.print("#" +n + " Abort Smelt: Smelt changed"); // restarts elsewhere
                        done();
                        return;
                    }
                }
                else
                {
                    if (recipe.ingredients.hasFuel(customFuel)) // valid fuel?
                    {
                        totalSmeltTime = customFuel.smeltTicks;
                        System.out.print("#" +n + " Fuel changed | " + curSmeltTime + "/" + totalSmeltTime);
                    }
                    else
                    {
                        // TODO handle fuel change
                        System.out.print("#" +n + " Abort Smelt: Fuel changed");
                        done();
                        return;
                    }
                }
                if (furnace.getInventory().getResult() != null && !furnace.getInventory().getResult().isSimilar(recipe.getPreview(null, furnace)))
                {
                    System.out.print("#"+n+" Abort Smelt: Result is blocked!");
                    furnace.setCookTime((short)0);
                    manager.preventSmelting(furnace);
                    done();
                    return;
                }
                curSmeltTime += lastFuelTick - furnace.getBurnTime();
                short cookTime = (short)(curSmeltTime / factor);
                //System.out.print("#" + n + " | " + curSmeltTime + "/" + totalSmeltTime  +
                //                     " | RealFuel:" + furnace.getBurnTime());
                updateLastFuelTick(furnace.getBurnTime());
                if (curSmeltTime < totalSmeltTime)
                {
                    if (totalSmeltTime > DEFAULT_SMELT_TIME)
                    {
                        if (cookTime >= DEFAULT_SMELT_TIME - 5)
                        {
                            cookTime -= 5; // prevent smelting too soon
                        }
                    }
                    furnace.setCookTime(cookTime);
                }
                else
                {
                    endFuel = (short)(furnace.getBurnTime() + curSmeltTime - totalSmeltTime);
                    if (furnace.getBurnTime() <= 0)
                    {
                        System.out.print("#" +n + " Last Smelt! (Fuel +1)");
                        furnace.setBurnTime((short)1);
                    }
                    furnace.setCookTime((short)(DEFAULT_SMELT_TIME));
                    done();
                    System.out.print("#" +n + " Smelt Done | EndFuel " + endFuel +"(+" +(curSmeltTime - totalSmeltTime) + ")");
                }
            }
        };
        this.taskID = manager.coreModule.getCore().getTaskManager().runTimer(manager.coreModule, runner, 1, 1);
    }
}
