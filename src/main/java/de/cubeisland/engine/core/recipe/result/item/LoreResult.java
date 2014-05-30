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
package de.cubeisland.engine.core.recipe.result.item;

import java.util.Arrays;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.cubeisland.engine.core.recipe.result.logic.Result;
import de.cubeisland.engine.core.util.ChatFormat;

public class LoreResult extends Result
{
    private String[] lines;

    // TODO \n

    private LoreResult(String... lines)
    {
        this.lines = lines;
        for (int i = 0; i < lines.length; i++)
        {
            lines[i] = ChatFormat.parseFormats(lines[i]);
        }
    }

    @Override
    public ItemStack getResult(Player player, BlockState block, ItemStack itemStack)
    {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Arrays.asList(lines));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static LoreResult of(String... lines)
    {
        return new LoreResult(lines);
    }
}
