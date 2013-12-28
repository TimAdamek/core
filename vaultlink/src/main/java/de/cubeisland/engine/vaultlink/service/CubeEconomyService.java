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
package de.cubeisland.engine.vaultlink.service;

import java.util.List;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.vaultlink.Vaultlink;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import static net.milkbowl.vault.economy.EconomyResponse.ResponseType.FAILURE;
import static net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS;

public class CubeEconomyService implements Economy
{
    private final Vaultlink module;
    private de.cubeisland.engine.core.module.service.Economy backingService;

    public CubeEconomyService(Vaultlink module, de.cubeisland.engine.core.module.service.Economy backingService)
    {
        this.module = module;
        this.backingService = backingService;
    }

    @Override
    public boolean isEnabled()
    {
        return module.isEnabled();
    }

    @Override
    public String getName()
    {
        return "CubeEngine:" + backingService.getName();
    }

    @Override
    public boolean hasBankSupport()
    {
        return backingService.hasBankSupport();
    }

    @Override
    public int fractionalDigits()
    {
        return backingService.fractionalDigits();
    }

    @Override
    public String format(double v)
    {
        return backingService.format(v);
    }

    @Override
    public String currencyNamePlural()
    {
        return backingService.currencyNamePlural();
    }

    @Override
    public String currencyNameSingular()
    {
        return backingService.currencyName();
    }

    @Override
    public boolean hasAccount(String s)
    {
        return backingService.hasAccount(s);
    }

    @Override
    public double getBalance(String s)
    {
        return backingService.getBalance(s);
    }

    @Override
    public boolean has(String s, double v)
    {
        return backingService.has(s, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v)
    {
        boolean result = backingService.withdraw(s, v);
        String message = (result ? "Money successfully withdrawn!" : "You don't have enough money.");
        User user = module.getCore().getUserManager().getUser(s);
        if (user != null)
        {
            message = user.translate(message);
        }
        return new EconomyResponse(v, getBalance(s), result ? SUCCESS : FAILURE, message);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v)
    {
        boolean result = backingService.deposit(s, v);
        return new EconomyResponse(v, getBalance(s), result ? SUCCESS : FAILURE, result ? "Money successfully deposited!" : "Your account is full.");
    }

    @Override
    public EconomyResponse createBank(String name, String owner)
    {
        if (backingService.createBank(name, owner))
        {
            return new EconomyResponse(0, bankBalance(name).balance, SUCCESS, "");
        }

        return new EconomyResponse(0, 0, FAILURE, "Failed to create the bank!");
    }

    @Override
    public EconomyResponse deleteBank(String name)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        return new EconomyResponse(0, 0, backingService.deleteBank(name) ? SUCCESS : FAILURE, "");
    }

    @Override
    public EconomyResponse bankBalance(String name)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        return new EconomyResponse(0, backingService.getBankBalance(name), SUCCESS, "");
    }

    @Override
    public EconomyResponse bankHas(String name, double v)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        if (backingService.bankHas(name, v))
        {
            return new EconomyResponse(0, getBalance(name), ResponseType.FAILURE, "The bank does not have enough money!");
        }
        else
        {
            return new EconomyResponse(0, getBalance(name), ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double v)
    {
        EconomyResponse response = bankHas(name, v);
        if (!response.transactionSuccess())
        {
            return response;
        }
        backingService.bankWithdraw(name, v);
        return new EconomyResponse(v, getBalance(name), ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double v)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        backingService.bankDeposit(name, v);
        return new EconomyResponse(v, getBalance(name), ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String player)
    {
        if (!getBanks().contains(name))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        else if (backingService.isBankOwner(name, player))
        {
            return new EconomyResponse(0, bankBalance(name).balance, ResponseType.SUCCESS, "");
        }
        else
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That player is not a bank owner!");
        }
    }

    @Override
    public EconomyResponse isBankMember(String bank, String player)
    {
        if (!getBanks().contains(bank))
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That bank does not exist!");
        }
        else if (backingService.isBankMember(bank, player))
        {
            return new EconomyResponse(0, bankBalance(bank).balance, ResponseType.SUCCESS, "");
        }
        else
        {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "That player is not a bank member!");
        }
    }

    @Override
    public List<String> getBanks()
    {
        return backingService.getBanks();
    }

    @Override
    public boolean createPlayerAccount(String name)
    {
        return backingService.createPlayerAccount(name);
    }
}
