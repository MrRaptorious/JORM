package jormCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract class to collect all database specific classes
 */
public abstract class DependencyConfiguration {

    private final HashMap<Class, Class> typeMapping;
    private final HashMap<Class, Object> objectMapping;

    public DependencyConfiguration() {
        typeMapping = new HashMap<>();
        objectMapping = new HashMap<>();

        configureTypes();
    }

    /**
     * Creates an instance of a type
     * @param cls class to create instance from
     * @param <T> type of class to create instance from
     * @return new instance of type T
     */
    public <T> T resolve(Class<T> cls) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        if (!typeMapping.containsKey(cls))
            throw new IllegalArgumentException("The type \"" + cls.getName() + "\" is not registered");

        if (objectMapping.containsKey(cls)) {
            if (objectMapping.get(cls) == null)
                objectMapping.put(cls, createInstance(typeMapping.get(cls)));

            return (T) objectMapping.get(cls);
        }

        return (T) createInstance(typeMapping.get(cls));
    }

    /**
     *  creates an instance of a given type
     * @param cls class of type T to create a instance from
     * @param <T> type of class to create an instance from
     */
    private <T> T createInstance(Class<T> cls) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, ExceptionInInitializerError {
        var constructors = cls.getConstructors();
        ArrayList<Constructor> validConstructors = new ArrayList<>();

        for (var constructor : constructors) {
            boolean skipConstructor = false;
            var params = constructor.getParameters();

            for (var param : params) {
                if (!typeMapping.containsKey(param.getType())) {
                    skipConstructor = true;
                    break;
                }
            }

            if (skipConstructor)
                continue;
            else
                validConstructors.add(constructor);
        }

        if (validConstructors.size() == 0 || validConstructors.size() > 1)
            throw new InstantiationException("There is no definitive Constructor for the type " + cls.getName());

        Constructor currentConstructor = validConstructors.get(0);
        Object[] argumentArray = new Object[currentConstructor.getParameters().length];

        for (int i = 0; i < currentConstructor.getParameters().length; i++) {
            try {
                argumentArray[i] = resolve(currentConstructor.getParameters()[i].getType());
            } catch (Exception e) {
                // TODO
                throw e;
            }
        }

        return (T) currentConstructor.newInstance(argumentArray);
    }

    /**
     * Adds a mapping to the type mapping list
     * @param src base class
     * @param target extending class
     */
    protected final void addMapping(Class<?> src, Class<?> target) {
        typeMapping.put(src, target);
    }

    /**
     * Adds a mapping to the type mapping list
     * @param src base class
     * @param target extending class
     * @param isSingle determines if there can be multiple instances of a class
     */
    protected final void addMapping(Class<?> src, Class<?> target, boolean isSingle) {
        typeMapping.put(src, target);

        if (isSingle) {
            objectMapping.put(src, null);
        }
    }

    protected abstract void configureTypes();
}
