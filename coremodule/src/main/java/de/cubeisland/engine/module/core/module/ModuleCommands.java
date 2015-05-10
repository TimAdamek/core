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
package de.cubeisland.engine.module.core.module;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.alias.Alias;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.Flag;
import de.cubeisland.engine.butler.parametric.Reader;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.vanillaplus.VanillaCommands;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.ContainerCommand;
import de.cubeisland.engine.module.core.command.CommandContext;
import de.cubeisland.engine.module.core.util.Version;
import de.cubeisland.engine.module.core.util.ChatFormat;
import org.spongepowered.api.plugin.PluginManager;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.*;


@Command(name = "module", desc = "Provides ingame module plugin management functionality")
public class ModuleCommands extends ContainerCommand
{
    private final SpongeCore core;
    private final Modularity modularity;
    private final PluginManager pm;

    public ModuleCommands(SpongeCore core, Modularity modularity, PluginManager pm)
    {
        super(core);
        this.core = core;
        this.modularity = modularity;
        this.pm = pm;
        core.getCommandManager().getProviderManager().register(core, new ModuleReader(modularity));
    }

    public static class ModuleReader implements ArgumentReader<de.cubeisland.engine.modularity.core.Module>
    {
        private Modularity mm;

        public ModuleReader(Modularity mm)
        {
            this.mm = mm;
        }

        @Override
        public de.cubeisland.engine.modularity.core.Module read(Class type, CommandInvocation invocation) throws ReaderException
        {
            String name = invocation.consume(1);
            for (de.cubeisland.engine.modularity.core.Module module : this.mm.getModules())
            {
                if (module.getInformation().getName().equals(name))
                {
                    return module;
                }
            }
            throw new ReaderException(CubeEngine.getI18n().translate(invocation.getLocale(), NEGATIVE, "The given module could not be found!"));        }
    }

    @Alias(value = "modules")
    @Command(alias = "show", desc = "Lists all the loaded modules")
    public void list(CommandContext context)
    {
        Set<de.cubeisland.engine.modularity.core.Module> modules = this.modularity.getModules();
        if (modules.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "There are no modules loaded!");
            return;
        }
        context.sendTranslated(NEUTRAL, "These are the loaded modules.");
        context.sendTranslated(NEUTRAL, "{text:Green (+):color=BRIGHT_GREEN} stands for enabled, {text:red (-):color=RED} for disabled.");
        for (de.cubeisland.engine.modularity.core.Module module : modules)
        {
            if (module.isEnabled())
            {
                context.sendMessage(" + " + ChatFormat.BRIGHT_GREEN + module.getInformation().getName());
            }
            else
            {
                context.sendMessage(" - " + ChatFormat.RED + module.getInformation().getName());
            }
        }
    }

    @Command(desc = "Enables a module")
    public void enable(CommandContext context, @Reader(ModuleReader.class) Module module)
    {
        if (this.modularity.enableModule(module))
        {
            context.sendTranslated(POSITIVE, "The given module was successfully enabled!");
            return;
        }
        context.sendTranslated(CRITICAL, "An error occurred while enabling the module!");
    }

    @Command(desc = "Disables a module")
    public void disable(CommandContext context, @Reader(ModuleReader.class) Module module)
    {
        this.modularity.disableModule(module);
        context.sendTranslated(POSITIVE, "The module {name#module} was successfully disabled!", module.getInformation().getName());
    }

    @Command(desc = "Unloaded a module and all the modules that depend on it")
    public void unload(CommandContext context, @Reader(ModuleReader.class) Module module)
    {
        this.modularity.unloadModule(module);
        context.sendTranslated(POSITIVE, "The module {name#module} was successfully unloaded!", module.getInformation().getName());
    }

    @Command(desc = "Reloads a module")
    public void reload(CommandContext context, @Reader(ModuleReader.class) Module module, @Flag boolean file)
    {
        try
        {
            this.modularity.reloadModule(module, file);
            if (file)
            {
                context.sendTranslated(POSITIVE, "The module {name#module} was successfully reloaded from file!", module.getInformation().getName());
            }
            else
            {
                context.sendTranslated(POSITIVE, "The module {name#module} was successfully reloaded!", module.getInformation().getName());
            }
        }
        catch (ModuleException ex)
        {
            context.sendTranslated(NEGATIVE, "Failed to reload the module!");
            context.sendTranslated(NEUTRAL, "Check the server log for info.");
            core.getLog().error(ex, "Failed to reload the module {}!", module.getInformation().getName());
        }
    }

    @Command(desc = "Loads a module from the modules directory.")
    public void load(CommandSender context, String filename)
    {
        if (filename.contains(".") || filename.contains("/") || filename.contains("\\"))
        {
            context.sendTranslated(NEGATIVE, "The given file name is invalid!");
            return;
        }
        Path modulesPath = core.getFileManager().getModulesPath();
        Path modulePath = modulesPath.resolve(filename + ".jar");
        if (!Files.exists(modulePath))
        {
            context.sendTranslated(NEGATIVE, "The given module file was not found! The name might be case sensitive.");
            return;
        }
        if (!Files.isReadable(modulePath))
        {
            context.sendTranslated(NEGATIVE, "The module exists, but cannot be read! Check the file permissions.");
            return;
        }
        try
        {
            Module module = modularity.loadModule(modulePath);
            if (modularity.enableModule(module))
            {
                context.sendTranslated(POSITIVE, "The module {name#module} has been successfully loaded and enabled!", module.getInformation().getName());
            }
        }
        catch (ModuleAlreadyLoadedException e)
        {
            context.sendTranslated(NEUTRAL, "This module is already loaded, try reloading it.");
        }
        catch (ModuleException ex)
        {
            context.sendTranslated(NEGATIVE, "The module failed to load! Check the server log for info.");
            core.getLog().error(ex, "Failed to load a module from file {}!", modulePath);
        }
    }

    @Command(desc = "Get info about a module")
    public void info(CommandContext context, @Reader(ModuleReader.class) Module module, @Flag boolean source)
    {
        ModuleMetadata moduleInfo = module.getInformation();
        context.sendTranslated(POSITIVE, "Name: {input}", moduleInfo.getName());
        context.sendTranslated(POSITIVE, "Description: {input}", moduleInfo.getDescription());
        context.sendTranslated(POSITIVE, "Version: {input}", moduleInfo.getVersion());
        if (source && moduleInfo.getSourceVersion() != null)
        {
            VanillaCommands.showSourceVersion(context.getSource(), moduleInfo.getSourceVersion());
        }

        Map<String, Version> dependencies = moduleInfo.getDependencies();
        Map<String, Version> softDependencies = moduleInfo.getSoftDependencies();
        Set<String> pluginDependencies = moduleInfo.getPluginDependencies();
        Set<String> services = moduleInfo.getServices();
        Set<String> softServices = moduleInfo.getSoftServices();
        Set<String> providedServices = moduleInfo.getProvidedServices();

        String green = "   " + ChatFormat.BRIGHT_GREEN + "- ";
        String red = "   " + ChatFormat.RED + "- ";
        if (!providedServices.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Provided services:");
            for (String service : providedServices)
            {
                context.sendMessage(green + service);
            }
        }
        if (!dependencies.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Module dependencies:");
            for (String dependency : dependencies.keySet())
            {
                Module dep = this.modularity.getModule(dependency);
                if (dep != null && dep.isEnabled())
                {
                    context.sendMessage(green + dependency);
                }
                else
                {
                    context.sendMessage(red + dependency);
                }
            }
        }
        if (!softDependencies.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Module soft-dependencies:");
            for (String dependency : softDependencies.keySet())
            {
                Module dep = this.modularity.getModule(dependency);
                if (dep != null && dep.isEnabled())
                {
                    context.sendMessage(green + dependency);
                }
                else
                {
                    context.sendMessage(red + dependency);
                }
            }
        }
        if (!pluginDependencies.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Plugin dependencies:");
            for (String dependency : pluginDependencies)
            {
                if (pm.isLoaded(dependency))
                {
                    context.sendMessage(green + dependency);
                }
                else
                {
                    context.sendMessage(red + dependency);
                }
            }
        }
        if (!services.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Service dependencies:");
            for (String service : services)
            {
                context.sendMessage(green + service); // TODO colors to show if service is found OR NOT
            }
        }
        if (!softServices.isEmpty())
        {
            context.sendTranslated(POSITIVE, "Service soft dependencies");
            for (String service : softServices)
            {
                context.sendMessage(green + service); // TODO colors to show if service is found OR NOT
            }
        }
    }
}