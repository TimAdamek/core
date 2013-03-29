package de.cubeisland.cubeengine.log.storage;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.DoubleChest;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.math.BlockVector3;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class Lookup
{
    private final Module module;

    // Lookup Types:
    // Full lookup / all action types / can set time & location
    // Player lookup / all player related / time / location / users
    // Block lookup / all block related / time / location / block MAT:id  | < WORLDEDIT 0x4B || 0x61 || 0x63 (hangings)


    // The actions to look for
    private Set<Integer> actions = new THashSet<Integer>();
    // When (since/before/from-to)
    private Long from_since;
    private Long to_before;
    // Where (in world / at location1 / in between location1 and location2)
    private BlockVector3 location1;
    private BlockVector3 location2;
    private Long worldID;
    // users
    private Set<Long> users;
    private boolean includeUsers = true;
    //Block-Logs:
    private Set<BlockData> blocks;
    private boolean includeBlocks = true;

    public Lookup(Module module)
    {
        this.module = module;
    }

    public Lookup setUsers(Set<Long> userIds, boolean include)
    {
        this.users.clear();
        this.users.addAll(userIds);
        this.includeUsers = include;
        return this;
    }

    public Lookup includeUser(Long userId)
    {
        if (this.includeUsers)
        {
            this.users.add(userId);
        }
        else
        {
            this.users.remove(userId);
        }
        return this;
    }

    public Lookup excludeUser(Long userId)
    {
        if (this.includeUsers)
        {
            this.users.remove(userId);
        }
        else
        {
            this.users.add(userId);
        }
        return this;
    }

    public Lookup includeAction(int action)
    {
        this.actions.add(action);
        return this;
    }

    public Lookup excludeAction(int action)
    {
        this.actions.remove(action);
        return this;
    }

    public Lookup includeActions(Set<Integer> actions)
    {
        this.actions.addAll(actions);
        return this;
    }

    public Lookup excludeActions(Set<Integer> actions)
    {
        this.actions.removeAll(actions);
        return this;
    }

    public Lookup clearActions()
    {
        actions.clear();
        return this;
    }

    public Lookup since(long date)
    {
        this.from_since = date;
        this.to_before = null;
        return this;
    }

    public Lookup before(long date)
    {
        this.from_since = null;
        this.to_before = date;
        return this;
    }

    public Lookup range(long from, long to)
    {
        this.from_since = from;
        this.to_before = to;
        return this;
    }

    public Lookup setWorld(World world)
    {
        this.worldID = this.module.getCore().getWorldManager().getWorldId(world);
        return this;
    }

    public Lookup setLocation(Location location)
    {
        this.location1 = new BlockVector3(location.getBlockX(),location.getBlockY(),location.getBlockZ());
        this.location2 = null;
        return this.setWorld(location.getWorld());
    }

    public Lookup setSelection(Location location1, Location location2)
    {
        if (location1.getWorld() != location2.getWorld())
        {
            throw new IllegalArgumentException("Both locations must be in the same world!");
        }
        this.location1 = new BlockVector3(location1.getBlockX(),location1.getBlockY(),location1.getBlockZ());
        this.location2 = new BlockVector3(location2.getBlockX(),location2.getBlockY(),location2.getBlockZ());
        return this.setWorld(location1.getWorld());
    }

    public void clear()
    {
// clear all logs
    }

    /**
     player [name1] <name2> <name3> ...
     area <radius>
     selection, sel
     block [type1] <type2> <type3> ..., type [type1] <type2> <type3> ...
     created, destroyed
     chestaccess
     kills
     since [timespec], time [timespec]
     before [timespec]
     limit [count]
     sum [none|blocks|players]
     world [worldname]
     asc, desc
     coords
     silent
     last
     chat
     search, match
     loc, location (v1.51+)
     */
}
