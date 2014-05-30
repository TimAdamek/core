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
package de.cubeisland.engine.core.recipe.effect.logic;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.condition.general.ChanceCondition;

public abstract class Effect
{
    // Effects:
    // - sound / visual
    // - summon
    // - setblock (drop existing)
    // - potionreffect
    // - firework

    // - modify money (needs Economy Service)

    /**
     * Returns true if an effect ran
     *
     * @param core
     * @param player
     * @return
     */
    public abstract boolean runEffect(Core core, Player player);

    public final Effect delayedBy(long ticks)
    {
        return new DelayedEffect(ticks, this);
    }

    public final Effect and(Effect effect)
    {
        return new AndEffect(this, effect);
    }

    public final Effect or(Effect effect)
    {
        return new OrEffect(this, effect);
    }

    public final Effect when(Condition condition)
    {
        return new ConditionEffect(condition, this);
    }

    public final Effect withChance(float chance)
    {
        return new ConditionEffect(ChanceCondition.of(chance), this);
    }
}
