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
package de.cubeisland.engine.core.recipe.ingredients;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public class MaterialCondition extends IngredientCondition
{
    private Material material;

    public MaterialCondition(Material material)
    {
        super(); // MaterialCondition is needed
        this.material = material;
    }

    public static IngredientCondition of(Material... materials)
    {
        IngredientCondition condition = new MaterialCondition(materials[0]);
        for (int i = 1; i < materials.length; i++)
        {
            condition = condition.or(new MaterialCondition(materials[i]));
        }
        return condition;
    }

    @Override
    protected boolean process(Permissible permissible, ItemStack itemStack)
    {
        return itemStack.getType() == this.material;
    }
}