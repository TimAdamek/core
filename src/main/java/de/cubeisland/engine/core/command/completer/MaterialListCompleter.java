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
package de.cubeisland.engine.core.command.completer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import de.cubeisland.engine.command.completer.Completer;
import de.cubeisland.engine.command.methodic.context.BaseCommandContext;
import de.cubeisland.engine.core.util.StringUtils;

public class MaterialListCompleter implements Completer
{
    @Override
    public List<String> complete(BaseCommandContext context, String token)
    {
        List<String> tokens = Arrays.asList(StringUtils.explode(",", token));
        String lastToken = token.substring(token.lastIndexOf(",")+1,token.length()).toUpperCase();
        List<String> matches = new ArrayList<>();
        for (Material material : Material.values())
        {
            if (material.isBlock() && material != Material.AIR && material.name().startsWith(lastToken) && !tokens.contains(material.name()))
            {
                matches.add(token.substring(0, token.lastIndexOf(",") + 1) + material.name());
            }
        }
        return matches;
    }
}