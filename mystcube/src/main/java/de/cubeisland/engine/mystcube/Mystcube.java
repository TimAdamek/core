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

import java.util.Random;

import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.recipe.FuelIngredient;
import de.cubeisland.engine.core.recipe.FurnaceIngredients;
import de.cubeisland.engine.core.recipe.FurnaceRecipe;
import de.cubeisland.engine.core.recipe.Ingredient;
import de.cubeisland.engine.core.recipe.RecipeManager;
import de.cubeisland.engine.core.recipe.ShapedIngredients;
import de.cubeisland.engine.core.recipe.ShapelessIngredients;
import de.cubeisland.engine.core.recipe.WorkbenchRecipe;
import de.cubeisland.engine.core.recipe.condition.general.BiomeCondition;
import de.cubeisland.engine.core.recipe.condition.general.GamemodeCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.AmountCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.DurabilityCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialCondition;
import de.cubeisland.engine.core.recipe.condition.ingredient.NameCondition;
import de.cubeisland.engine.core.recipe.effect.CommandEffect;
import de.cubeisland.engine.core.recipe.effect.ExplodeEffect;
import de.cubeisland.engine.core.recipe.result.EffectResult;
import de.cubeisland.engine.core.recipe.result.item.AmountResult;
import de.cubeisland.engine.core.recipe.result.item.DurabilityResult;
import de.cubeisland.engine.core.recipe.result.item.ItemStackResult;
import de.cubeisland.engine.core.recipe.result.item.KeepResult;
import de.cubeisland.engine.core.recipe.result.item.LoreResult;
import de.cubeisland.engine.core.recipe.result.item.NameResult;
import de.cubeisland.engine.mystcube.blockpopulator.VillagePopulator;
import de.cubeisland.engine.mystcube.chunkgenerator.FlatMapGenerator;

public class Mystcube extends Module implements Listener
{
    private MystcubeConfig config;

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

            new VillagePopulator().populate(world, new Random(), world.getSpawnLocation().getChunk());
        }
    }

    @Override
    public void onEnable()
    {
        Bukkit.getServer().addRecipe(new org.bukkit.inventory.FurnaceRecipe(new ItemStack(Material.GOLD_INGOT), Material.OBSIDIAN));
        RecipeManager recipeManager = this.getCore().getRecipeManager();
        recipeManager.registerRecipe(this,
                  new WorkbenchRecipe(
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

        recipeManager.registerRecipe(this, new WorkbenchRecipe(
            new ShapelessIngredients(magicPaperIngredient, Ingredient
                     .withMaterial(Material.DIAMOND)), new ItemStackResult(Material.PAPER)
                        .and(NameResult.of("&9Raw Linking Panel"))
                        .and(LoreResult.of("&eAn unfinished linking panel.",
                                           "&eGreat heat is needed to",
                                           "&emake it usable in a book"))));

        FuelIngredient obsidianFuel =  new FuelIngredient(Ingredient.withMaterial(Material.OBSIDIAN), 64 * 20, 20);

        recipeManager.registerRecipe(this, new FurnaceRecipe(new FurnaceIngredients(
            Ingredient.withMaterial(Material.BLAZE_ROD), obsidianFuel),
                                                      new ItemStackResult(Material.BLAZE_POWDER).and(AmountResult.set(8))));

        recipeManager.registerRecipe(this,
                  new FurnaceRecipe(new FurnaceIngredients(
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
                                          new WorkbenchRecipe(
                                              new ShapelessIngredients(magicPaperIngredient, magicPaperIngredient,
                                                                       linkingPanelIngredient,
                                                                       Ingredient.withMaterial(Material.LEATHER))
                                              ,new ItemStackResult(Material.BOOK).and(NameResult.of("&6Kortee'nea"))
                                                                                  .and(LoreResult.of("&eA Blank Book just",
                                                                                               "&ewaiting to be written"))
                                          ).allowOldRecipe(true));

        this.getCore().getEventManager().registerListener(this, this);

        // TODO remove RecipeManager TEST

        de.cubeisland.engine.core.recipe.Recipe recipe = new WorkbenchRecipe(
            new ShapelessIngredients(Ingredient.withMaterial(Material.PAPER),
                                     Ingredient.withMaterial(Material.SAND).withResult(
                                         new KeepResult().withCondition(new BiomeCondition(Biome.DESERT, Biome.DESERT_HILLS))
                                                         .withChance(0.8f))),
            new ItemStackResult(Material.PAPER).and(NameResult.of("Sandpaper")).withChance(0.99f).
                or(new ItemStackResult(Material.PAPER).and(NameResult.of("Fine Sandpaper")).
                    and(new EffectResult(new CommandEffect("broadcast A lucky Player crafted Fine SandPaper!"))).
                                          and(new EffectResult(ExplodeEffect.ofSafeTnt().force(1f)))))
            .withPreview(new ItemStackResult(Material.PAPER).
                            and(NameResult.of("Sandpaper")).
                            and(LoreResult.of("1% Chance to get Fine Sandpaper",
                                              "80% Chance to keep Sand",
                                              "when crafting in Desert Biome")));
        recipeManager.registerRecipe(this, recipe);

        ShapelessIngredients ingredients = new ShapelessIngredients(Ingredient.withCondition(
            MaterialCondition.of(Material.WOOL).and(DurabilityCondition.exact((short)14)))); // RED WOOL
        ingredients.addIngredient(Ingredient.withCondition(MaterialCondition.of(Material.BED, Material.RED_MUSHROOM,
                        Material.TNT, Material.REDSTONE, Material.REDSTONE_BLOCK, Material.REDSTONE_ORE, Material.REDSTONE_TORCH_ON,
                        Material.NETHERRACK, Material.NETHER_BRICK, Material.NETHER_BRICK_ITEM, Material.NETHER_BRICK_STAIRS,
                        Material.NETHER_FENCE, Material.NETHER_STALK, Material.APPLE, Material.MELON, Material.RAW_BEEF,
                        Material.SPIDER_EYE, Material.FERMENTED_SPIDER_EYE, Material.RECORD_4)));
        recipeManager.registerRecipe(this,
                                          new WorkbenchRecipe(ingredients,
                                          new ItemStackResult(Material.WOOL).and(DurabilityResult.set((short)14)).
                                              and(NameResult.of("&cVery Red Wool")))
                                         );

        recipeManager.registerRecipe(this, new WorkbenchRecipe(
            new ShapedIngredients(" x ", "   ", "x x")
                            .setIngredient('x', Ingredient.withCondition(MaterialCondition.of(Material.IRON_INGOT))),
            new ItemStackResult(Material.GOLD_INGOT).and(AmountResult.set(3)))
            .withCondition(GamemodeCondition.creative()).
            withPreview(new ItemStackResult(Material.GOLD_INGOT).and(LoreResult
                                                                         .of("&eYour creative mode", "&eis so awesome, you can", "&econvert iron to gold"))
                                                                .and(AmountResult.set(3))));
    }

    // Blank Book Kortee'nea

    // Descriptive Book: Kor-mahn
    // Linking Book: Kor'vahkh
    // Ink: lem // Use brewing if possible (water glowstone redstone inksack) (using weakness / slowness or no effect)
    // potion data 32 = thick potion

    @Override
    public void onLoad()
    {
        this.getCore().getWorldManager().registerGenerator(this, "flat", new FlatMapGenerator());
    }
}
