package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ConditionalBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public abstract class MySQLConditionalBuilder<This extends MySQLConditionalBuilder> extends MySQLComponentBuilder<This, MySQLQueryBuilder> implements ConditionalBuilder<This, MySQLQueryBuilder>
{
    protected MySQLConditionalBuilder(Database database)
    {
        super(database);
    }

    public This orderBy(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");

        this.query.append(" ORDER BY ").append(this.database.prepareFieldName(cols[0]));
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        return (This)this;
    }

    public This limit(int n)
    {
        this.query.append(" LIMIT ").append(n);
        return (This)this;
    }

    public This offset(int n)
    {
        this.query.append(" OFFSET ").append(n);
        return (This)this;
    }

    public This where()
    {
        this.query.append(" WHERE ");
        return (This)this;
    }
}