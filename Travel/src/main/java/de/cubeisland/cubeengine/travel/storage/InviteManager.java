package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TwoKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;

public class InviteManager extends TwoKeyStorage<Long, Long, TeleportInvite>
{
    private static final int REVISION = 2;
    private Collection<TeleportInvite> invites;

    public InviteManager(Database database)
    {
        super(database, TeleportInvite.class, REVISION);
        this.initialize();
        this.invites = this.getAll();
    }

    public void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = database.getQueryBuilder();
            this.database.storeStatement(this.modelClass, "getInvitedTo",
                    builder.select().cols("teleportpoint").from(this.tableName).where().field("userkey").is(EQUAL).value().end().end());
            this.database.storeStatement(this.modelClass, "getInvited",
                    builder.select().cols("userkey").from(this.tableName).where().field("teleportpoint").is(EQUAL).value().end().end());
        }
        catch (SQLException ex)
        {
            //TODO handle the exception
        }
    }

    public Set<User> getInvitedUsers(TeleportPoint tPP)
    {
        Set<User> invitedUsers = new HashSet<User>();
        for (String name : getInvited(tPP))
        {
            User user = CubeEngine.getUserManager().findOnlineUser(name);
            if (user != null)
            {
                invitedUsers.add(user);
            }
        }
        return invitedUsers;
    }

    public Set<String> getInvited(TeleportPoint tPP)
    {
        Set<String> invitedUsers = new HashSet<String>();
        for (TeleportInvite tpI : getInvites(tPP))
        {
            User user = CubeEngine.getUserManager().getUser(tpI.userKey);
            if (user != null)
            {
                invitedUsers.add(user.getName());
            }
        }
        return invitedUsers;
    }

    public Set<TeleportInvite> getInvites(TeleportPoint tPP)
    {
        Set<TeleportInvite> invites = new HashSet<TeleportInvite>();
        for (TeleportInvite invite : this.invites)
        {
            if (invite.teleportPoint == tPP.getKey())
            {
                invites.add(invite);
            }
        }
        return invites;
    }

    /**
     * Get what homes this user is invited to
     * @param user
     * @return A Set of the ids to the homes the user is invited to
     */
    public Set<Long> getInvitedTo(User user)
    {
        Set<Long> homes = new HashSet<Long>();
        try
        {
            ResultSet resultSet = database.preparedQuery(this.modelClass, "getInvitedTo", user.getKey());
            while (resultSet.next())
            {
                Long home = resultSet.getLong("teleportpoint");
                if (home != null)
                {
                    homes.add(home);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get the homes an user was invited to", ex);
        }
        return homes;
    }

    public void updateInvited(TeleportPoint tPP, Set<String> newInvited)
    {
        Set<TeleportInvite> invites = getInvites(tPP);
        Set<String> old = getInvited(tPP);
        Set<String> removed = old;
        removed.removeAll(newInvited);
        Set<String> added = newInvited;
        newInvited.removeAll(old);

        for (String user : added)
        {
            this.store(new TeleportInvite(tPP.getKey(), CubeEngine.getUserManager().getUser(user, false).getKey()));
        }
        for (String user : removed)
        {
            for (TeleportInvite invite : invites)
            {
                if (invite.semiEquals(new TeleportInvite(tPP.getKey(), CubeEngine.getUserManager().getUser(user, false).getKey())))
                {
                    this.delete(invite);
                }
            }
        }
    }
}