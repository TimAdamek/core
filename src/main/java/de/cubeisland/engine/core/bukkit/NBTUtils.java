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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import de.cubeisland.engine.converter.node.ByteNode;
import de.cubeisland.engine.converter.node.DoubleNode;
import de.cubeisland.engine.converter.node.FloatNode;
import de.cubeisland.engine.converter.node.IntNode;
import de.cubeisland.engine.converter.node.ListNode;
import de.cubeisland.engine.converter.node.LongNode;
import de.cubeisland.engine.converter.node.MapNode;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.NullNode;
import de.cubeisland.engine.converter.node.ShortNode;
import de.cubeisland.engine.converter.node.StringNode;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagByteArray;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagEnd;
import net.minecraft.server.v1_8_R3.NBTTagFloat;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagIntArray;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagLong;
import net.minecraft.server.v1_8_R3.NBTTagShort;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.TileEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class NBTUtils
{
    public static NBTTagCompound getTileEntityNBTAt(Location location)
    {
        NBTTagCompound result = new NBTTagCompound();
        TileEntity tileEntity = ((CraftWorld)location.getWorld()).getHandle()
             .getTileEntity(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        if (tileEntity == null) return null;
        tileEntity.b(result);
        return result;
    }

    public static void setTileEntityNBTAt(Location location, NBTTagCompound nbtData)
    {
        TileEntity tileEntity =  ((CraftWorld)location.getWorld()).getHandle()
                      .getTileEntity(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        tileEntity.a(nbtData);
    }

    @SuppressWarnings("unchecked")
    public static Node convertNBTToNode(NBTBase nbtBase)
    {
        if (nbtBase == null) return null;
        if (nbtBase instanceof NBTTagEnd)
        {
            return NullNode.emptyNode();
        }
        if (nbtBase instanceof NBTTagByte)
        {
            return new ByteNode(((NBTTagByte)nbtBase).f());
        }
        if (nbtBase instanceof NBTTagShort)
        {
            return new ShortNode(((NBTTagShort)nbtBase).e());
        }
        if (nbtBase instanceof NBTTagInt)
        {
            return  new IntNode(((NBTTagInt)nbtBase).d());
        }
        if (nbtBase instanceof NBTTagLong)
        {
            return new LongNode(((NBTTagLong)nbtBase).c());
        }
        if (nbtBase instanceof NBTTagFloat)
        {
            return new FloatNode(((NBTTagFloat)nbtBase).h());
        }
        if (nbtBase instanceof NBTTagDouble)
        {
            return new DoubleNode(((NBTTagDouble)nbtBase).g());
        }
        if (nbtBase instanceof NBTTagByteArray)
        {
            ListNode list = ListNode.emptyList();
            for (byte b : ((NBTTagByteArray)nbtBase).c())
            {
                list.addNode(new ByteNode(b));
            }
            return list;
        }
        if (nbtBase instanceof NBTTagString)
        {
            return StringNode.of(((NBTTagString)nbtBase).a_());
        }
        if (nbtBase instanceof NBTTagList)
        {
            ListNode list = ListNode.emptyList();
            for (int i = 0; i < ((NBTTagList)nbtBase).size(); i++)
            {
                switch (((NBTTagList)nbtBase).f())
                {
                case 0:
                    //return new net.minecraft.server.NBTTagEnd();
                case 1:
                    //return new net.minecraft.server.NBTTagByte();
                case 2:
                    //return new net.minecraft.server.NBTTagShort();
                case 3:
                    //return new net.minecraft.server.NBTTagInt();
                case 4:
                    //return new net.minecraft.server.NBTTagLong();
                    throw new UnsupportedOperationException();
                case 5:
                    list.addNode(new FloatNode(((NBTTagList)nbtBase).e(i)));
                    break;
                case 6:
                    list.addNode(new DoubleNode(((NBTTagList)nbtBase).d(i)));
                    break;
                case 7:
                    // return new net.minecraft.server.NBTTagByteArray();
                    throw new UnsupportedOperationException();
                case 11:
                    ListNode listNode = ListNode.emptyList();
                    for (int anInt : ((NBTTagList)nbtBase).c(i))
                    {
                        listNode.addNode(new IntNode(anInt));
                    }
                    list.addNode(listNode);
                    break;
                case 8:
                    list.addNode(StringNode.of(((NBTTagList)nbtBase).getString(i)));
                    break;
                case 9:
                    //return new net.minecraft.server.NBTTagList();
                    throw new UnsupportedOperationException();
                case 10:
                    list.addNode(convertNBTToNode(((NBTTagList)nbtBase).get(i)));
                    break;
                }
            }
            return list;
        }
        if (nbtBase instanceof NBTTagCompound)
        {
            MapNode map = MapNode.emptyMap();
            NBTTagCompound compound = (NBTTagCompound)nbtBase;
            for (String key : compound.c())
            {
                map.set(key, convertNBTToNode(compound.get(key)));
            }
            return map;
        }
        if (nbtBase instanceof NBTTagIntArray)
        {
            ListNode list = ListNode.emptyList();
            for (int i : ((NBTTagIntArray)nbtBase).c())
            {
                list.addNode(new IntNode(i));
            }
            return list;
        }
        throw new IllegalStateException("Unknown NBT tag type! "+ nbtBase.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public static NBTBase convertNodeToNBT(Node node)
    {
        if (node instanceof NullNode)
        {
            return HelperNBTBase.createEnd();
        }
        if (node instanceof ByteNode)
        {
            return new NBTTagByte((Byte)node.getValue());
        }
        if (node instanceof ShortNode)
        {
            return new NBTTagShort((Short)node.getValue());
        }
        if (node instanceof IntNode)
        {
            return new NBTTagInt((Integer)node.getValue());
        }
        if (node instanceof LongNode)
        {
            return new NBTTagLong((Long)node.getValue());
        }
        if (node instanceof FloatNode)
        {
            return new NBTTagFloat((Float)node.getValue());
        }
        if (node instanceof DoubleNode)
        {
            return new NBTTagDouble((Double)node.getValue());
        }
        if (node instanceof ListNode)
        {
            boolean onlyByte = true;
            boolean onlyInt = true;
            List<Node> listedNodes = ((ListNode)node).getValue();
            for (Node listedNode : listedNodes)
            {
                if (!(listedNode instanceof IntNode))
                {
                    onlyInt = false;
                }
                if (!(listedNode instanceof ByteNode))
                {
                    onlyByte = false;
                }
            }
            if (onlyByte)
            {
                byte[] byteArray = new byte[listedNodes.size()];
                for (int i = 0 ; i < byteArray.length ; i++)
                {
                    byteArray[i] = (Byte)listedNodes.get(i).getValue();
                }
                return new NBTTagByteArray(byteArray);
            }
            if (onlyInt)
            {
                int[] intarray = new int[listedNodes.size()];
                for (int i = 0 ; i < intarray.length ; i++)
                {
                    intarray[i] = (Integer)listedNodes.get(i).getValue();
                }
                return new NBTTagIntArray(intarray);
            }
            NBTTagList list = new NBTTagList();
            for (Node listedNode : listedNodes)
            {
                list.add(convertNodeToNBT(listedNode));
            }
            return list;

        }
        if (node instanceof StringNode)
        {
            return new NBTTagString((String)node.getValue());
        }
        if (node instanceof MapNode)
        {
            NBTTagCompound compound = new NBTTagCompound();
            for (Entry<String, Node> entry : ((MapNode)node).getValue().entrySet())
            {
                compound.set(((MapNode)node).getOriginalKey(entry.getKey()),convertNodeToNBT(entry.getValue()));
            }
            return compound;
        }
        throw new IllegalStateException("Cannot convert nodes to NBT tags! "+ node.getClass().getName());
    }

    private abstract static class HelperNBTBase extends NBTBase
    {
        public static NBTTagEnd createEnd()
        {
            return (NBTTagEnd)NBTBase.createTag((byte)0);
        }
    }
}
