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
package de.cubeisland.engine.core.command.readers;

import java.text.NumberFormat;
import java.text.ParseException;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class FloatReader implements ArgumentReader<Float>
{
    @Override
    public Float read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String consumed = invocation.consume(1);
        try
        {
            return NumberFormat.getInstance(invocation.getLocale()).parse(consumed).floatValue();
        }
        catch (ParseException e)
        {
            try
            {
                return NumberFormat.getInstance().parse(consumed).floatValue(); // Try default locale
            }
            catch (ParseException e1)
            {
                throw new ReaderException(CubeEngine.getI18n().translate(invocation.getLocale(), MessageType.NEGATIVE, "Could not parse {input} to float!", consumed)); // standardized exception message
            }
        }
    }
}
