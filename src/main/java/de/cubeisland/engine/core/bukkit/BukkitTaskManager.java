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
package de.cubeisland.engine.core.bukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleThreadFactory;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.task.thread.CoreThreadFactory;
import org.bukkit.scheduler.BukkitScheduler;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

public class BukkitTaskManager implements TaskManager
{
    private final BukkitCore corePlugin;
    private final BukkitScheduler bukkitScheduler;
    private final Map<Module, Set<Integer>> moduleTasks;
    private final CoreThreadFactory threadFactory;
    private final Map<String, ModuleThreadFactory> moduleThreadFactories;

    public BukkitTaskManager(Core core, BukkitScheduler bukkitScheduler)
    {
        this.corePlugin = (BukkitCore)core;
        this.threadFactory = new CoreThreadFactory(core);
        this.bukkitScheduler = bukkitScheduler;
        this.moduleTasks = new ConcurrentHashMap<>();
        this.moduleThreadFactories = new HashMap<>();
    }

    private Set<Integer> getModuleIDs(Module module)
    {
        return this.getModuleIDs(module, true);
    }

    private Set<Integer> getModuleIDs(Module module, boolean create)
    {
        Set<Integer> IDs = this.moduleTasks.get(module);
        if (create && IDs == null)
        {
            this.moduleTasks.put(module, IDs = new HashSet<>());
        }
        return IDs;
    }

    @Override
    public CoreThreadFactory getThreadFactory()
    {
        return this.threadFactory;
    }

    @Override
    public synchronized ModuleThreadFactory getThreadFactory(Module module)
    {
        ModuleThreadFactory threadFactory = this.moduleThreadFactories.get(module.getId());
        if (threadFactory == null)
        {
            this.moduleThreadFactories.put(module.getId(), threadFactory = new ModuleThreadFactory(module));
        }
        return threadFactory;
    }

    @Override
    public int runTask(Module module, Runnable runnable)
    {
        return this.runTaskDelayed(module, runnable, 0);
    }

    @Override
    public int runTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        final Set<Integer> tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.scheduleSyncDelayedTask(this.corePlugin, task, delay);
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    @Override
    public int runTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        final Set<Integer> tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.runTaskTimer(this.corePlugin, task, delay, interval).getTaskId();
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    @Override
    public int runAsynchronousTask(Module module, Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(module, runnable, 0);
    }

    @Override
    public int runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        final Set<Integer> tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.runTaskLaterAsynchronously(this.corePlugin, task, delay).getTaskId();
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    @Override
    public int runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        final Set<Integer> tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.runTaskTimerAsynchronously(this.corePlugin, task, delay, interval).getTaskId();
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    @Override
    public <T> Future<T> callSync(Callable<T> callable)
    {
        expectNotNull(callable, "The callable must not be null!");
        return this.bukkitScheduler.callSyncMethod(this.corePlugin, callable);
    }

    @Override
    public void cancelTask(Module module, int ID)
    {
        this.bukkitScheduler.cancelTask(ID);
        Set<Integer> IDs = this.getModuleIDs(module, false);
        if (IDs != null)
        {
            IDs.remove(ID);
        }
    }

    @Override
    public void cancelTasks(Module module)
    {
        Set<Integer> taskIDs = this.moduleTasks.remove(module);
        if (taskIDs != null)
        {
            for (Integer taskID : taskIDs)
            {
                this.bukkitScheduler.cancelTask(taskID);
            }
        }
    }

    @Override
    public boolean isCurrentlyRunning(int taskID)
    {
        return this.bukkitScheduler.isCurrentlyRunning(taskID);
    }

    @Override
    public boolean isQueued(int taskID)
    {
        return this.bukkitScheduler.isQueued(taskID);
    }

    @Override
    public synchronized void clean(Module module)
    {
        this.cancelTasks(module);
        final ModuleThreadFactory factory = this.moduleThreadFactories.remove(module.getId());
        if (factory != null)
        {
            factory.shutdown();
        }
    }

    @Override
    public synchronized void clean()
    {
        for (ModuleThreadFactory factory : this.moduleThreadFactories.values())
        {
            factory.shutdown();
        }
        this.moduleThreadFactories.clear();
    }

    private class Task implements Runnable
    {
        protected int taskID;
        private final Runnable task;
        private final Set<Integer> taskIDs;

        public Task(Runnable task, Set<Integer> taskIDs)
        {
            this.task = task;
            this.taskIDs = taskIDs;
        }

        @Override
        public void run()
        {
            this.task.run();
            this.taskIDs.remove(this.taskID);
        }
    }
}
