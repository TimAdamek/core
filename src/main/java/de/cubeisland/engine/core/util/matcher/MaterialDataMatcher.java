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
package de.cubeisland.engine.core.util.matcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.util.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class MaterialDataMatcher
{

    private HashMap<Material, Map<Short, Set<String>>> reverseItemData;
    private HashMap<Material, Map<Byte, Set<String>>> reverseBlockData;
    private HashMap<Material, Map<String, Short>> itemData;
    private HashMap<Material, Map<String, Byte>> blockData;

    MaterialDataMatcher()
    {
        this.readDataValues(false);
        this.readDataValues(true);
    }

    /**
     * Loads in the file with the saved item-datavalues
     */
    private void readDataValues(boolean update)
    {
        if (!update)
        {
            this.reverseItemData = new HashMap<>();
            this.reverseBlockData = new HashMap<>();
            this.itemData = new HashMap<>();
            this.blockData = new HashMap<>();
        }
        boolean updated = false;
        Path file = CubeEngine.getFileManager().getDataPath().resolve(CoreResource.DATAVALUES.getTarget());
        List<String> input;
        if (update)
        {
            try (InputStream is = CubeEngine.getFileManager().getSourceOf(file))
            {
                input = FileUtil.readStringList(is);
            }
            catch (IOException ex)
            {
                CubeEngine.getLog().warn(ex, "Could not update data values");
                return;
            }
        }
        else
        {
            try
            {
                input = FileUtil.readStringList(file);
            }
            catch (IOException ex)
            {
                CubeEngine.getLog().warn(ex, "Could not update data values");
                return;
            }
        }
        Map<Short, Set<String>> reverseCurrentItemData = null;
        Map<Byte, Set<String>> reverseCurrentBlockData = null;
        Map<String, Short> currentItemData = null;
        Map<String, Byte> currentBlockData = null;
        Set<Material> currentMaterials = new HashSet<>();
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.endsWith(":"))
            {
                if (this.saveData(currentMaterials, reverseCurrentItemData, reverseCurrentBlockData, currentItemData, currentBlockData))
                {
                    updated = true;
                }
                boolean first = true;
                for (String key : StringUtils.explode(",", line.substring(0, line.length() - 1)))
                {
                    try
                    {
                        Material material = Material.valueOf(key);
                        currentMaterials.add(material);
                        if (first)
                        {
                            reverseCurrentItemData = new HashMap<>();
                            reverseCurrentBlockData = new HashMap<>();
                            currentItemData = new HashMap<>();
                            currentBlockData = new HashMap<>();
                            first = false;
                        }
                    }
                    catch (IllegalArgumentException ex)
                    {
                        CubeEngine.getLog().warn("Unknown Material for Data: {}", key);
                        reverseCurrentItemData = new HashMap<>();
                        reverseCurrentBlockData = new HashMap<>();
                        currentItemData = new HashMap<>();
                        currentBlockData = new HashMap<>();
                    }
                }
            }
            else
            {
                if (line.contains(":"))
                {
                    String value = line.substring(line.indexOf(":") + 1).trim();
                    Set<String> names;
                    Byte blockDataVal = null;
                    Short itemDataVal = null;
                    try
                    {
                        if (value.contains("x"))
                        {
                            blockDataVal = Byte.decode(value);
                            names = reverseCurrentBlockData.get(blockDataVal);
                            if (names == null)
                            {
                                names = new HashSet<>();
                                reverseCurrentBlockData.put(blockDataVal, names);
                            }
                        }
                        else
                        {
                            itemDataVal = Short.parseShort(value);
                            names = reverseCurrentItemData.get(itemDataVal);
                            if (names == null)
                            {
                                names = new HashSet<>();
                                reverseCurrentItemData.put(itemDataVal, names);
                            }
                        }
                    }
                    catch (NumberFormatException ex)
                    {
                        CubeEngine.getLog().warn("Could not parse data for Material: {}", value);
                        continue;
                    }
                    for (String key : StringUtils.explode(",", line.substring(0, line.indexOf(":"))))
                    {
                        if (names.add(key))
                        {
                            if (itemDataVal == null)
                            {
                                currentBlockData.put(key, blockDataVal);
                            }
                            else
                            {
                                currentItemData.put(key, itemDataVal);
                            }
                        }
                    }
                }
            }
        }
        if (this.saveData(currentMaterials, reverseCurrentItemData, reverseCurrentBlockData, currentItemData, currentBlockData))
        {
            updated = true;
        }
        if (update && updated)
        {
            CubeEngine.getLog().info("Updated datavalues.txt");
            StringBuilder sb = new StringBuilder();
            Map<Map<Short, Set<String>>, String> itemMap = new HashMap<>();
            for (Entry<Material, Map<Short, Set<String>>> entry : this.reverseItemData.entrySet()) // make serializable...
            {
                Map<Short, Set<String>> map = entry.getValue();
                if (map.isEmpty())
                {
                    continue;
                }
                String mats = itemMap.get(map);
                if (mats == null)
                {
                    mats = entry.getKey().name();
                }
                else
                {
                    mats += "," + entry.getKey().name();
                }
                itemMap.put(map, mats);
            }
            for (Map.Entry<Map<Short, Set<String>>, String> entry : itemMap.entrySet()) // serialize...
            {
                if (entry.getKey().isEmpty())
                    continue;
                sb.append(entry.getValue()).append(":\n");
                Map<Short, Set<String>> map = entry.getKey();
                for (Entry<Short, Set<String>> e : map.entrySet())
                {
                    sb.append("    ").append(StringUtils.implode(",", e.getValue())).append(": ").append(e.getKey()).append("\n");
                }
            }
            HashMap<Map<Byte, Set<String>>, String> blockMap = new HashMap<>();
            for (Material material : this.reverseBlockData.keySet()) // make serializable...
            {
                Map<Byte, Set<String>> map = this.reverseBlockData.get(material);
                if (map.isEmpty())
                    continue;
                String mats = blockMap.put(map, material.name());
                if (mats != null)
                {
                    mats += "," + material.name();
                    blockMap.put(map, mats);
                }
            }
            for (Map.Entry<Map<Byte, Set<String>>, String> entry : blockMap.entrySet()) // serialize...
            {
                if (entry.getKey().isEmpty())
                    continue;
                sb.append(entry.getValue()).append(":\n");
                Map<Byte, Set<String>> map = entry.getKey();
                for (Entry<Byte, Set<String>> e : map.entrySet())
                {
                    String val;
                    if (e.getKey() < 0)
                    {
                        val = "-0x" + Integer.toString(-e.getKey(), 16);
                    }
                    else
                    {
                        val = "0x" + Integer.toString(e.getKey(), 16);
                    }
                    sb.append("    ").append(StringUtils.implode(",", e.getValue())).append(": ").append(val).append("\n");
                }
            }
            try
            {
                FileUtil.saveFile(sb.toString(), file);
            }
            catch (IOException e)
            {
                CubeEngine.getLog().warn("Could not save changed datavalues.txt");
            }
        }
    }

    private boolean saveData(Set<Material> currentMaterials, Map<Short, Set<String>> reverseCurrentItemData, Map<Byte, Set<String>> reverseCurrentBlockData, Map<String, Short> currentItemData, Map<String, Byte> currentBlockData)
    {
        boolean updated = false;
        for (Material material : currentMaterials)
        {
            if (!reverseCurrentItemData.isEmpty())
            {
                Map<Short, Set<String>> replaced = this.reverseItemData.put(material, reverseCurrentItemData);
                if (replaced != null)
                {
                    replaced.keySet().removeAll(this.reverseItemData.get(material).keySet());
                    if (replaced.isEmpty())
                    {
                        this.reverseItemData.get(material).putAll(replaced);
                        updated = true;
                    }
                }
            }
            if (!reverseCurrentBlockData.isEmpty())
            {
                Map<Byte, Set<String>> replaced = this.reverseBlockData.put(material, reverseCurrentBlockData);
                if (replaced != null)
                {
                    replaced.keySet().removeAll(this.reverseBlockData.get(material).keySet());
                    if (replaced.isEmpty())
                    {
                        this.reverseBlockData.get(material).putAll(replaced);
                        updated = true;
                    }
                }
            }
            if (!currentItemData.isEmpty())
            {
                Map<String, Short> replaced = this.itemData.put(material, currentItemData);
                if (replaced != null)
                {
                    replaced.keySet().removeAll(this.itemData.get(material).keySet());
                    if (replaced.isEmpty())
                    {
                        this.itemData.get(material).putAll(replaced);
                        updated = true;
                    }
                }
            }
            if (!currentBlockData.isEmpty())
            {
                Map<String, Byte> replaced = this.blockData.put(material, currentBlockData);
                if (replaced != null)
                {
                    replaced.keySet().removeAll(this.blockData.get(material).keySet());
                    if (replaced.isEmpty())
                    {
                        this.blockData.get(material).putAll(replaced);
                        updated = true;
                    }
                }
            }
        }
        currentMaterials.clear();
        return updated;
    }

    /**
     * Matches a DyeColor
     *
     * @param data the data
     * @return the dye color
     */
    public DyeColor colorData(String data)
    {
        try
        {
            byte byteData = Byte.parseByte(data);
            DyeColor color = DyeColor.getByWoolData(byteData);
            if (color != null)
            {
                return color;
            }
        }
        catch (NumberFormatException ignored)
        {}
        Map<String, Short> woolData = this.itemData.get(Material.WOOL);
        if (woolData == null)
        {
            CubeEngine.getLog().warn("No data found for Wool-color");
            return null;
        }
        String match = Match.string().matchString(data, woolData.keySet());
        if (match == null)
        {
            return null;
        }
        Short dataVal = woolData.get(match);
        return DyeColor.getByWoolData(dataVal.byteValue());
    }

    /**
     * Sets the data for an ItemStack
     *
     * @param item the item
     * @param rawData the data
     * @return the modified clone of the item
     */
    public ItemStack setData(ItemStack item, String rawData)
    {
        String data = rawData.toLowerCase(Locale.ENGLISH);
        if (item == null)
        {
            return null;
        }
        item = item.clone();
        try
        { // try dataValue as Number
            item.setDurability(Short.parseShort(data));
            return item;
        }
        catch (NumberFormatException e)
        { // check for special cases
            switch (item.getType())
            {
                case MONSTER_EGG:
                    EntityType foundEggData = Match.entity().spawnEggMob(data);
                    if (foundEggData != null)
                    {
                        item.setDurability(foundEggData.getTypeId());
                    }
                    return item;
                case SKULL_ITEM:
                    item.setDurability((short)3);
                    SkullMeta meta = ((SkullMeta)item.getItemMeta());
                    meta.setOwner(rawData);
                    item.setItemMeta(meta);
                    return item;
                default:
                    Map<String, Short> dataMap = this.itemData.get(item.getType());
                    if (dataMap != null)
                    {
                        String foundData = Match.string().matchString(data, dataMap.keySet());
                        if (foundData != null)
                        {
                            item.setDurability(dataMap.get(foundData));
                        }
                    }
                    return item;
            }
        }
    }

    public String getDataNameFor(Material mat, Byte blockData)
    {
        Map<Byte, Set<String>> map = this.reverseBlockData.get(mat);
        String dataNames = "";
        byte mask = blockData;
        if (map != null)
        {
            String zeroData = null;
            for (Entry<Byte, Set<String>> entry : map.entrySet())
            {
                Byte flag = entry.getKey();
                if (flag < 0)
                {
                    if (!((mask & -flag) == flag))
                    {
                        dataNames += ":" + entry.getValue().iterator().next();
                    }
                }
                else if (flag == 0)
                {
                    zeroData = entry.getValue().iterator().next();
                }
                else if ((mask & flag) == flag)
                {
                    dataNames += ":" + entry.getValue().iterator().next();
                    mask &= ~flag;
                }
            }

            if (dataNames.isEmpty() && zeroData != null)
            {
                dataNames += ":" + zeroData;
            }
        }
        Map<Short, Set<String>> itemMap = this.reverseItemData.get(mat);
        if (itemMap == null)
        {
            if (dataNames.isEmpty())
            {
                CubeEngine.getLog().warn("Unknown Block-Type: {}", mat);
                return null;
            }
        }
        else
        {
            Set<String> names = itemMap.get((short)mask);
            if (names == null)
            {
                if (dataNames.isEmpty())
                {
                    CubeEngine.getLog().warn("Unknown Block-Data: {} DATA: {}", mat, mask);
                    return null;
                }
            }
            else
                dataNames += ":" + names.iterator().next();
        }
        if (dataNames.isEmpty())
        {
            CubeEngine.getLog().warn("Unknown Block-Data: {} DATA: {}", mat, mask);
            return null;
        }
        return dataNames;
    }

    public String[] colorStrings()
    {
        Set<String> woolColors = this.itemData.get(Material.WOOL).keySet();
        return woolColors.toArray(new String[woolColors.size()]);
    }
}
