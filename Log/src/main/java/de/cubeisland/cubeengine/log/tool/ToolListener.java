
package de.cubeisland.cubeengine.log.tool;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.BlockData;
import java.sql.Timestamp;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ToolListener implements Listener
{
    private  final Log module;

    public ToolListener(Log module)
    {
        this.module = module;
    }
    
    @EventHandler
    public void onClick(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() != null && event.getPlayer().getItemInHand().getTypeId() == 7)
        {
            Location loc = event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                    ? event.getClickedBlock().getLocation()
                    : event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            this.module.getLogManager().getBlockLogs(
                    loc.getWorld(),loc, null,
                    new Integer[]{0,1,2}, 
                    new Long[]{}, false,
                    new BlockData[]{}, false,
                    new Timestamp(System.currentTimeMillis()-1000*60*60*24*7), 
                    new Timestamp(System.currentTimeMillis()));
            event.setCancelled(true);
        }
    }
}