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
package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockForm;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.cubeengine.core.util.BlockUtil.DIRECTIONS;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;
import static org.bukkit.Material.*;

/**
 * Water-flow
 * <p>Events: {@link FlowActionType}</p>
 * <p>External Actions:
 * {@link WaterFlow},  {@link BlockForm}
 */
public class WaterFlow extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "water-flow";
    }

    public void logWaterFlow(BlockFromToEvent event, BlockState toBlock, BlockState newToBlock, BlockState fromBlock)
    {
        if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
        {
            int sources = 0;
            for (BlockFace face : DIRECTIONS)
            {
                Block nearBlock = event.getToBlock().getRelative(face);
                if (nearBlock.getType().equals(Material.STATIONARY_WATER) && nearBlock.getData() == 0)
                {
                    sources++;
                }
            }
            if (sources >= 2) // created new source block
            {
                this.logBlockForm(toBlock,newToBlock,WATER);
            }// else only changing water-level do not log
            return;
        }
        if (newToBlock.getType().equals(Material.LAVA) || newToBlock.getType().equals(Material.STATIONARY_LAVA) && newToBlock.getRawData() <= 2)
        {
            this.logBlockForm(toBlock,newToBlock,COBBLESTONE);
            return;
        }
        for (final BlockFace face : BLOCK_FACES)
        {
            if (face.equals(BlockFace.UP))continue;
            final Block nearBlock = event.getToBlock().getRelative(face);
            if (nearBlock.getType().equals(Material.LAVA) && nearBlock.getState().getRawData() <=4 || nearBlock.getType().equals(Material.STATIONARY_LAVA))
            {
                BlockState oldNearBlock = nearBlock.getState();
                BlockState newNearBlock = nearBlock.getState();
                this.logBlockForm(oldNearBlock,newNearBlock,nearBlock.getData() == 0 ? OBSIDIAN : COBBLESTONE);
            }
        }
        newToBlock.setType(Material.WATER);
        newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
        if (toBlock.getType().equals(AIR))
        {
            if (this.isActive(toBlock.getWorld()))
            {
                this.logBlockChange(null,toBlock,newToBlock,null);
            }
        }
        else
        {
            this.logWaterBreak(toBlock,newToBlock);
        }
    }

    private void logWaterBreak(BlockState toBlock, BlockState newToBlock)
    {
        WaterBreak waterBreak = this.manager.getActionType(WaterBreak.class);
        if (waterBreak.isActive(toBlock.getWorld()))
        {
            waterBreak.logBlockChange(null,toBlock,newToBlock,null);
        }
    }

    private void logBlockForm(BlockState toBlock, BlockState newToBlock, Material newType)
    {
        BlockForm blockForm = this.manager.getActionType(BlockForm.class);
        if (blockForm.isActive(toBlock.getWorld()))
        {
            newToBlock.setType(newType);
            newToBlock.setRawData((byte)0);
            blockForm.logBlockChange(null,toBlock,newToBlock,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {//TODO attach
        user.sendTranslated("%s&aWater flooded the block%s&a!",time,loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).WATER_FLOW_enable;
    }
}