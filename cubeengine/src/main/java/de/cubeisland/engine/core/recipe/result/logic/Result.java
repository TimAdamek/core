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
package de.cubeisland.engine.core.recipe.result.logic;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.general.ChanceCondition;
import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.result.item.AmountResult;

/**
 * Modifies a resulting ItemStack
 * <p>This may be used as result for Ingredients BUT ALSO as result for the crafted item
 */
public abstract class Result
{
    public abstract ItemStack getResult(Player player, BlockState block, ItemStack itemStack);

    public final Result or(Result other)
    {
        return new OrResult(this, other);
    }

    public final Result and(Result other)
    {
        return new AndResult(this, other);
    }

    public final Result withChance(float chance)
    {
        return new ConditionResult(ChanceCondition.of(chance), this);
    }

    public final Result withCondition(Condition condition)
    {
        return new ConditionResult(condition, this);
    }

    public final Result reduceByOne()
    {
        return this.and(AmountResult.remove(1));
    }

    // cloneingredient (data/amount/enchants/name/lore/special(leatherdye/firework/book/skull...)/allmeta(ench/name/lore/special)/all(allmeta/data/amount))
    // leathercolor rgb
    // bookitem title / author / pages
    // firework / firework charge item
    //  color rgb,...
    //  fadecolor rgb,...
    //  type ball ball_large star burst creeper
    //  trail / flicker
    // power 0-128
    // skullowner
    // potionitem type/lv/extended/splash  + moar custom effects (type,duration,amplify,ambient?)
    // enchantitem / book

}
