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
package de.cubeisland.engine.core.recipe.condition.ingredient;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.logic.Condition;

public class AmountCondition extends IngredientCondition
{
    private enum Type
    {
        EXACT, MORE, LESS;
    }

    private Type type;
    private int amount;

    private AmountCondition(Type type, int amount)
    {
        this.type = type;
        this.amount = amount;
    }

    public static AmountCondition less(int data)
    {
        // TODO handle impossible
        return new AmountCondition(Type.LESS, data);
    }

    public static AmountCondition more(int data)
    {
        // TODO handle impossible
        return new AmountCondition(Type.MORE, data);
    }

    public static AmountCondition exact(int data)
    {
        return new AmountCondition(Type.EXACT, data);
    }

    public static Condition notRange(int from, int to)
    {
        return AmountCondition.range(from, to).not();
    }

    public static Condition range(int from, int to)
    {
        return AmountCondition.more(from).and(AmountCondition.less(to));
    }

    @Override
    public boolean check(Player player, ItemStack itemStack)
    {
        switch (this.type)
        {
        case EXACT:
            return itemStack.getAmount() == amount;
        case MORE:
            return itemStack.getAmount() >= amount;
        case LESS:
            return itemStack.getAmount() <= amount;
        }
        throw new IllegalStateException();
    }
}
