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
package de.cubeisland.engine.core.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public abstract class AbstractWorldManager implements WorldManager
{
    protected final Map<String, WorldEntity> worlds;
    protected final Map<UInteger, World> worldIds;
    protected final Set<UUID> worldUUIDs;
    private final Map<String, Map<String, ChunkGenerator>> generatorMap;

    protected final Database database;

    public AbstractWorldManager(Core core)
    {
        this.database = core.getDB();
        this.worlds = new HashMap<>();
        this.worldIds = new HashMap<>();
        this.worldUUIDs = new HashSet<>();
        this.generatorMap = new HashMap<>();
    }

    @Override
    public synchronized UInteger getWorldId(World world)
    {
        if (world == null)
        {
            throw new IllegalArgumentException("the world given is null!");
        }
        return this.getWorldEntity(world).getValue(TABLE_WORLD.KEY);
    }

    @Override
    public WorldEntity getWorldEntity(World world)
    {
        DSLContext dsl = this.database.getDSL();
        WorldEntity worldEntity = this.worlds.get(world.getName());
        if (worldEntity == null)
        {
            UUID uid = world.getUID();
            worldEntity = dsl.selectFrom(TABLE_WORLD).where(TABLE_WORLD.LEAST.eq(uid.getLeastSignificantBits()),
                                                            TABLE_WORLD.MOST.eq(uid.getMostSignificantBits())).fetchOne();
            if (worldEntity == null)
            {
                worldEntity = dsl.newRecord(TABLE_WORLD).newWorld(world);
                worldEntity.insertAsync();
            }
            this.worlds.put(world.getName(), worldEntity);
            this.worldIds.put(worldEntity.getValue(TABLE_WORLD.KEY), world);
            this.worldUUIDs.add(world.getUID());
        }
        return worldEntity;
    }

    @Override
    public synchronized UInteger getWorldId(String name)
    {
        WorldEntity entity = this.worlds.get(name);
        if (entity == null)
        {
            World world = this.getWorld(name);
            if (world == null) return null;
            return this.getWorldId(world);
        }
        return entity.getValue(TABLE_WORLD.KEY);
    }

    @Override
    public synchronized Set<UInteger> getAllWorldIds()
    {
        return this.worldIds.keySet();
    }

    @Override
    public Set<UUID> getAllWorldUUIDs()
    {
        return Collections.unmodifiableSet(this.worldUUIDs);
    }

    @Override
    public synchronized World getWorld(UInteger id)
    {
        return this.worldIds.get(id);
    }

    @Override
    public synchronized void registerGenerator(Module module, String id, ChunkGenerator generator)
    {
        expectNotNull(id, "The ID must nto be null!");
        expectNotNull(generator, "The generator must not be null!");

        Map<String, ChunkGenerator> moduleGenerators = this.generatorMap.get(module.getId());
        if (moduleGenerators == null)
        {
            this.generatorMap.put(module.getId(), moduleGenerators = new HashMap<>(1));
        }
        moduleGenerators.put(id.toLowerCase(Locale.ENGLISH), generator);
    }

    @Override
    public synchronized ChunkGenerator getGenerator(Module module, String id)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(id, "The ID must nto be null!");

        Map<String, ChunkGenerator> moduleGenerators = this.generatorMap.get(module.getId());
        if (moduleGenerators != null)
        {
            return moduleGenerators.get(id.toLowerCase(Locale.ENGLISH));
        }
        return null;
    }

    @Override
    public synchronized void removeGenerator(Module module, String id)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(id, "The ID must nto be null!");

        Map<String, ChunkGenerator> moduleGenerators = this.generatorMap.get(module.getId());
        if (moduleGenerators != null)
        {
            moduleGenerators.remove(id.toLowerCase(Locale.ENGLISH));
        }
    }

    @Override
    public synchronized void removeGenerators(Module module)
    {
        this.generatorMap.remove(module.getId());
    }

    @Override
    public boolean unloadWorld(String worldName, boolean save)
    {
        return this.unloadWorld(this.getWorld(worldName), save);
    }

    @Override
    public boolean deleteWorld(String worldName) throws IOException
    {
        return this.deleteWorld(this.getWorld(worldName));
    }


    @Override
    public synchronized void clean()
    {
        this.worlds.clear();
        this.worldIds.clear();
        this.worldUUIDs.clear();
        this.generatorMap.clear();
    }

    @Override
    public List<String> getWorldNames()
    {
        List<String> worlds = new ArrayList<>();
        for (World world : this.getWorlds())
        {
            worlds.add(world.getName());
        }
        return worlds;
    }
}
