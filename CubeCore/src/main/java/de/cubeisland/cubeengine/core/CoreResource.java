package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.filesystem.Resource;

/**
 * Holds all the resource of the core
 *
 * @author Phillip Schichtel
 */
public enum CoreResource implements Resource
{
    ENGLISH_META("resources/language/en_US.yml", "language/en_US.yml"),
    GERMAN_META("resources/language/de_DE.yml", "language/de_DE.yml"),
    GERMAN_MESSAGES("resources/language/messages/de_DE.json", "language/de_DE/core.json");
    private final String target;
    private final String source;

    private CoreResource(String source, String target)
    {
        this.source = source;
        this.target = target;
    }

    @Override
    public String getSource()
    {
        return this.source;
    }

    @Override
    public String getTarget()
    {
        return this.target;
    }
}