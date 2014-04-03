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
package de.cubeisland.engine.log.action.newaction.block.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ENTITY;

/**
 * Represents an entity changing a block
 */
public class EntityChange extends ActionEntityBlock<ListenerEntityBlock>
{
    @Override
    public boolean canAttach(BaseAction action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        return user.getTranslation(POSITIVE, "{name#entity} changed {name#block} to {name#block}", this.entity.name(),
                                   this.oldBlock.name(), this.newBlock.name());
    }

    @Override
    public ActionCategory getCategory()
    {
        return ENTITY;
    }

    @Override
    public String getName()
    {
        return "change";
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return false;
    }
}
