package de.cubeisland.cubeengine.core.module.exception;

//TODO DOCU
public class IncompatibleDependencyException extends ModuleException
{
    public IncompatibleDependencyException(String module, String dep, int reqiredRev, int foundRev)
    {
        super("Module \"" + module + "\" requested revision " + reqiredRev + " of the module \"" + dep + "\" but found revision " + foundRev + "!");
    }
}
