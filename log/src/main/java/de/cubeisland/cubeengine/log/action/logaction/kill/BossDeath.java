package de.cubeisland.cubeengine.log.action.logaction.kill;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * boss-death
 * <p>Events: {@link KillActionType}</p>
 */
public class BossDeath extends SimpleLogActionType
{
    public BossDeath(Log module)
    {
        super(module, 0x79, "boss-death");
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        KillActionType.showSubActionLogEntry(user, logEntry,time,loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return KillActionType.isSimilarSubAction(logEntry,other);
    }
}
