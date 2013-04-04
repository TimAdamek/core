package de.cubeisland.cubeengine.core.ban;

import java.net.InetAddress;
import java.util.Set;

import de.cubeisland.cubeengine.core.user.User;

public interface BanManager
{
    void addBan(Ban ban);

    UserBan getBan(User user);

    IpBan getBan(InetAddress address);

    boolean removeBan(User user);

    boolean removeBan(InetAddress address);

    boolean isBanned(User user);

    boolean isBanned(InetAddress address);

    Set<UserBan> getUserBans();

    Set<IpBan> getIpBans();

    Set<Ban> getBans();
}