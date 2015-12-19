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
package org.cubeengine.service.webapi.sender;

import java.util.Locale;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

public class ApiServerSender extends ApiCommandSender
{
    private I18n i18n;

    public ApiServerSender(I18n i18n, ObjectMapper mapper)
    {
        super(i18n, mapper);
        this.i18n = i18n;
    }

    @Override
    public String getName()
    {
        return "ApiCommandSender";
    }

    @Override
    public Text getDisplayName()
    {
        return Texts.of("ApiCommandSender");
    }

    @Override
    public Locale getLocale()
    {
        return i18n.getDefaultLanguage().getLocale();
    }

    @Override
    public boolean hasPermission(String name)
    {
        return true;
    }

    @Override
    public UUID getUniqueId()
    {
        return NON_PLAYER_UUID;
    }
}