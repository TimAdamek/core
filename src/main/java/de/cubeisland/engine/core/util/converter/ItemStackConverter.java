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
package de.cubeisland.engine.core.util.converter;

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.NullNode;
import de.cubeisland.engine.converter.node.StringNode;
import de.cubeisland.engine.core.util.matcher.Match;
import org.bukkit.inventory.ItemStack;

public class ItemStackConverter extends SimpleConverter<ItemStack>
{
    @Override
    public Node toNode(ItemStack object) throws ConversionException
    {
        if (object == null)
        {
            return NullNode.emptyNode();
        }
        return StringNode.of(object.getType().name() + ":" + object.getDurability());
    }

    @Override
    public ItemStack fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return Match.material().itemStack(((StringNode)node).getValue());
        }
        if (node instanceof NullNode)
        {
            return null;
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
