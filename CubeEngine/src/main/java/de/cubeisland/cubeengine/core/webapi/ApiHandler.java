package de.cubeisland.cubeengine.core.webapi;

import de.cubeisland.cubeengine.core.module.Module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class is a wrapper for the action requestMethods to extract the information
 * from the annotation and to link the method with its holder.
 *
 * This class is usually not needed by holder developers
 */
public final class ApiHandler
{
    private final ApiHolder holder;
    private final String route;
    private final Method method;
    private final boolean authNeeded;
    private final String[] parameters;
    private final Set<RequestMethod> requestMethods;

    /**
     * Initializes the request action.
     *
     * @param holder     the parent
     * @param route      the route of the action
     * @param method     the method to invoke
     * @param authNeeded whether authentication is needed
     */
    ApiHandler(ApiHolder holder, String route, Method method, boolean authNeeded, String[] parameters, RequestMethod[] requestMethods)
    {
        this.holder = holder;
        this.route = route;
        this.method = method;
        this.authNeeded = authNeeded;
        this.parameters = parameters;
        this.requestMethods = EnumSet.copyOf(Arrays.asList(requestMethods));

        this.method.setAccessible(true);
    }

    public Module getModule()
    {
        return this.holder.getModule();
    }

    public ApiHolder getHolder()
    {
        return this.holder;
    }

    /**
     * Returns the route of action
     *
     * @return the route
     */
    public String getRoute()
    {
        return this.route;
    }

    /**
     * Returns whether this action requires authentication.
     *
     * @return whether authentication is needed
     */
    public Boolean isAuthNeeded()
    {
        return this.authNeeded;
    }

    /**
     * Returns an array of the required parameters
     *
     * @return the required parameters
     */
    public String[] getParameters()
    {
        return this.parameters;
    }

    public boolean isMethodAccepted(RequestMethod method)
    {
        return this.requestMethods.contains(method);
    }

    /**
     * This method handles the request.
     */
    public void execute(final ApiRequest request, final ApiResponse response) throws Throwable
    {
        try
        {
            this.method.invoke(this.holder, request, response);
        }
        catch (InvocationTargetException e)
        {
            throw e.getCause();
        }
    }

    @Override
    public String toString()
    {
        return this.getRoute();
    }
}
