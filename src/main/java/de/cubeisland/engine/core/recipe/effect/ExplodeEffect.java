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
package de.cubeisland.engine.core.recipe.effect;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;

public class ExplodeEffect extends Effect
{
    private float power;
    private boolean setFire;
    private boolean breakBlocks;
    // TODO no entity damage

    private ExplodeEffect(float power, boolean setFire, boolean breakBlocks)
    {
        this.power = power;
        this.setFire = setFire;
        this.breakBlocks = breakBlocks;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        Location location = player.getLocation();
        return player.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(),
                                          this.power, this.setFire, this.breakBlocks);
    }

    public static ExplodeEffect ofTnt()
    {
        return new ExplodeEffect(4f, false, true);
    }

    public static ExplodeEffect ofSafeTnt()
    {
        return new ExplodeEffect(4f, false, false);
    }

    /**
     * 4f is normal tnt explosion
     *
     * @param power
     * @return
     */
    public ExplodeEffect force(float power)
    {
        this.power = power;
        return this;
    }

    public ExplodeEffect setsFire(boolean setFire)
    {
        this.setFire = setFire;
        return this;
    }

    public ExplodeEffect breaksBlocks(boolean breakBlocks)
    {
        this.breakBlocks = breakBlocks;
        return this;
    }
}
