package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;
import static org.bukkit.Material.AIR;

/**
 * Leaves decaying
 * <p>Events: {@link LeavesDecayEvent}
 */
public class LeafDecay extends BlockActionType
{

    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT);
    }

    @Override
    public String getName()
    {
        return "leaf-decay";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(event.getBlock().getLocation(),null,
                                BlockData.of(event.getBlock().getState()),
                                AIR,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s &adecayed%s!",
                            time,logEntry.getOldBlock(),loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LEAF_DECAY_enable;
    }

}
