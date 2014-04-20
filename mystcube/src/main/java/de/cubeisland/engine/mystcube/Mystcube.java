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
package de.cubeisland.engine.mystcube;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.recipe.CubeFurnaceRecipe;
import de.cubeisland.engine.core.recipe.CubeWorkbenchRecipe;
import de.cubeisland.engine.core.recipe.FuelIngredient;
import de.cubeisland.engine.core.recipe.FurnaceIngredients;
import de.cubeisland.engine.core.recipe.Ingredient;
import de.cubeisland.engine.core.recipe.RecipeManager;
import de.cubeisland.engine.core.recipe.ShapedIngredients;
import de.cubeisland.engine.core.recipe.ShapelessIngredients;
import de.cubeisland.engine.core.recipe.condition.ingredient.AmountCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.NameCondition;
import de.cubeisland.engine.core.recipe.result.item.AdditionalItemResult;
import de.cubeisland.engine.core.recipe.result.item.AmountResult;
import de.cubeisland.engine.core.recipe.result.item.ItemStackResult;
import de.cubeisland.engine.core.recipe.result.item.LoreResult;
import de.cubeisland.engine.core.recipe.result.item.NameResult;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.mystcube.chunkgenerator.FlatMapGenerator;

public class Mystcube extends Module implements Listener
{
    private MystcubeConfig config;

    @Override
    public void onLoad()
    {
        this.getCore().getWorldManager().registerGenerator(this, "flat", new FlatMapGenerator());
    }

    @Override
    public void onStartupFinished()
    {
        WorldCreator worldCreator = WorldCreator.name("world_myst_flat")
                        .generator("CubeEngine:mystcube:flat")
                        .generateStructures(false)
                        .type(WorldType.FLAT)
                        .environment(Environment.NORMAL);
        World world = this.getCore().getWorldManager().createWorld(worldCreator);
        if (world != null)
        {
            world.setAmbientSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
            world.setSpawnFlags(false, false);
        }
    }

    @Override
    public void onEnable()
    {
        Bukkit.getServer().addRecipe(new org.bukkit.inventory.FurnaceRecipe(new ItemStack(Material.GOLD_INGOT), Material.OBSIDIAN));
        RecipeManager recipeManager = this.getCore().getRecipeManager();
        recipeManager.registerRecipe(this,
                  new CubeWorkbenchRecipe(
                      new ShapedIngredients("ppp","prp","ppp")
                          .setIngredient('p', Ingredient.withMaterial(Material.PAPER))
                          .setIngredient('r', Ingredient.withMaterial(Material.REDSTONE))
                      ,new ItemStackResult(Material.PAPER).and(NameResult.of("&3Magic Paper"))
                          .and(LoreResult.of("&eThe D'ni used this kind of",
                                             "&epaper to write their Ages")
                          .and(AmountResult.set(8)))
                  ));

        Ingredient magicPaperIngredient = Ingredient
            .withCondition(MaterialCondition.of(Material.PAPER).and(NameCondition.of("&3Magic Paper")));
        // TODO LoreCondition
        ItemStack item = new ItemStack(Material.PAPER, 8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatFormat.parseFormats("&6Magic Paper"));
        meta.setLore(Arrays.asList(ChatFormat.parseFormats("&eThe D'ni used this kind of"), ChatFormat.parseFormats(
            "&epaper to write their Ages")));
        item.setItemMeta(meta);
        //MAGIC_PAPER = item.clone();
        //MAGIC_PAPER.setAmount(1);
        ShapedRecipe magicPaper = new ShapedRecipe(item).shape("ppp", "prp", "ppp").setIngredient('p', Material.PAPER).setIngredient('r', Material.REDSTONE);
        //this.registerRecipe(magicPaper);

        recipeManager.registerRecipe(this, new CubeWorkbenchRecipe(
            new ShapelessIngredients(magicPaperIngredient, Ingredient
                     .withMaterial(Material.DIAMOND)), new ItemStackResult(Material.PAPER)
                        .and(NameResult.of("&9Raw Linking Panel"))
                        .and(LoreResult.of("&eAn unfinished linking panel.",
                                           "&eGreat heat is needed to",
                                           "&emake it usable in a book"))));

        FuelIngredient obsidianFuel =  new FuelIngredient(Ingredient.withMaterial(Material.OBSIDIAN), 64 * 20, 20);

        recipeManager.registerRecipe(this, new CubeFurnaceRecipe(new FurnaceIngredients(
            Ingredient.withMaterial(Material.BLAZE_ROD), obsidianFuel),
                                                      new ItemStackResult(Material.BLAZE_POWDER).and(AmountResult.set(8))));

        recipeManager.registerRecipe(this,
                  new CubeFurnaceRecipe(new FurnaceIngredients(
                      Ingredient.withCondition(MaterialCondition.of(Material.PAPER).and(NameCondition.of("&9Raw Linking Panel")
                                                           .and(AmountCondition.more(2))))
                      .withResult(AmountResult.remove(2))
                      , new FuelIngredient(Ingredient.withMaterial(Material.BLAZE_POWDER), 20 , 4 * 20),
                       obsidianFuel // TODO remove long burning test
                  ), new ItemStackResult(Material.PAPER).and(NameResult.of("&6Linking Panel")
                                                            .and(LoreResult.of("&eWhen used in an age or linking book",
                                                                               "&eyou will get teleported",
                                                                               "&eby merely touching the panel"))))
                                         .withPreview(new ItemStackResult(Material.PAPER).and(NameResult.of("&6Linking Panel"))
                                                                                             .and(LoreResult.of("&eTwo Raw Linking Panels",
                                                                                                  "&eforged together by great",
                                                                                                  "&eheat. It is still warm."))))
        ;
        Ingredient linkingPanelIngredient = Ingredient
            .withCondition(MaterialCondition.of(Material.PAPER).and(NameCondition.of("&6Linking Panel")));
            // TODO LoreCondition

        recipeManager.registerRecipe(this,
                                          new CubeWorkbenchRecipe(
                                              new ShapelessIngredients(magicPaperIngredient, magicPaperIngredient,
                                                                       linkingPanelIngredient,
                                                                       Ingredient.withMaterial(Material.LEATHER))
                                              ,new ItemStackResult(Material.BOOK).and(NameResult.of("&6Kortee'nea"))
                                                                                  .and(LoreResult.of("&eA Blank Book just",
                                                                                               "&ewaiting to be written"))
                                          ).allowOldRecipe(true));


        // TODO remove test
        recipeManager.registerRecipe(this, new CubeWorkbenchRecipe(new ShapelessIngredients(Ingredient.withCondition(MaterialCondition.of(Material.SANDSTONE).and(AmountCondition.more(2))).withResult(AmountResult.remove(2))),
                                                                   new ItemStackResult(Material.SAND).and(AmountResult.set(6))).withPreview(
                                                                   new ItemStackResult(Material.SAND).and(
                                                                       AmountResult.set(6).and(LoreResult.of(
                                                                           "Consumes 2 Sandstone")))));

        recipeManager.registerRecipe(this, new CubeWorkbenchRecipe(new ShapelessIngredients(
            Ingredient.withMaterial(Material.COOKIE),
            Ingredient.withMaterial(Material.INK_SACK),
            Ingredient.withMaterial(Material.WOOL).withResult(new AdditionalItemResult(new ItemStack(Material.STRING, 1)).reduceByOne())),
         new ItemStackResult(Material.COOKIE).and(NameResult.of("Black Cookie"))));

        this.getCore().getEventManager().registerListener(this, this);
    }

    // Blank Book Kortee'nea

    // Descriptive Book: Kor-mahn
    // Linking Book: Kor'vahkh
    // Ink: lem // Use brewing if possible (water glowstone redstone inksack) (using weakness / slowness or no effect)
    // potion data 32 = thick potion

}
