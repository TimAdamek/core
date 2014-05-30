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
package de.cubeisland.engine.core.recipe.condition.general;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.logic.Condition;

public class BiomeCondition extends Condition
{
    private Collection<Biome> biomes;

    public BiomeCondition(Biome... biomes)
    {
        this.biomes = Arrays.asList(biomes);
    }

    @Override
    public boolean check(Player player, ItemStack itemStack)
    {
        return this.biomes.contains(player.getLocation().getBlock().getBiome());
    }
}
