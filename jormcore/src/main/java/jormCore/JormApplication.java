package jormCore;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The main object representing the entire program
 */
public class JormApplication {
    // has List<ApplicationSupManager>
    private HashMap<String, ApplicationSubManager> subManagers;
    private ApplicationSubManager defaultSubManager;

    private static JormApplication application;

    private JormApplication() {
        subManagers = new HashMap<String, ApplicationSubManager>();
    }

    public static JormApplication getApplication() {
        if (application == null)
            application = new JormApplication();

        return application;
    }

    public void registerApplicationSubManager(String name, ApplicationSubManager subManager) {
        subManagers.put(name, subManager);
    }

    public void registerApplicationSubManager(String name, ApplicationSubManager subManager, boolean isDefault) {
        subManagers.put(name, subManager);

        if (isDefault)
            defaultSubManager = subManager;
    }

    public ApplicationSubManager getDefaultSubManager() {
        return defaultSubManager;
    }

    public ApplicationSubManager getApplicationSubManagerByName(String name) {
        return subManagers.get(name);
    }

    public void start()
    {
        for(var managerSet : subManagers.entrySet())
        {
            managerSet.getValue().start();
        }
    }
}
