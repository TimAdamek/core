package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.TwoKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KitsGivenManager extends TwoKeyStorage<Integer, String, KitsGiven>
{
    private static final int REVISION = 1;

    public KitsGivenManager(Database database)
    {
        super(database, KitsGiven.class, REVISION);

        this.initialize();
    }

    @Override
    public void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();
            this.database.storeStatement(modelClass, "getLimitForUser",
                    builder.select().cols("amount").
                    from(this.tableName).
                    where().field(this.s_dbKey).isEqual().value()
                    .and().field(this.s_dbKey).isEqual().value().end().end());

            this.database.storeStatement(modelClass, "mergeLimitForUser",
                    builder.merge().into(this.tableName).
                    cols(this.allFields).
                    updateCols("amount").end().end());
        }
        catch (SQLException e)
        {
            throw new StorageException("Failed to initialize the mail-manager!", e);
        }
    }

    public boolean reachedUsageLimit(User user, String name, int limitUsagePerPlayer)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(modelClass, "getLimitForUser", user.key, name);
            if (resulsSet.next())
            {
                Integer amount = resulsSet.getInt("amount");
                if (amount >= limitUsagePerPlayer)
                {
                    return true;
                }
            }
            return false;
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Error while reading from Database", ex);
        }
    }
}