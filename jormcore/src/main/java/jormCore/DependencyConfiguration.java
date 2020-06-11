package jormCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DependencyConfiguration {

    private HashMap<Class,Class> typeMapping;
    private HashMap<Class,Object> objectMapping;

    public DependencyConfiguration()
    {
        typeMapping = new HashMap<>();
        objectMapping = new HashMap<>();

        configureTypes();
    }

    public  <T> T resolve(Class<T> cls) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        if(!typeMapping.containsKey(cls))
            throw new IllegalArgumentException("The type \"" + cls.getName() + "\" is not registerd");

            if(objectMapping.containsKey(cls))
        {
            if(objectMapping.get(cls)==null)
                objectMapping.put(cls,createInstance(typeMapping.get(cls)));

            return (T)objectMapping.get(cls);
        }

        return (T)createInstance(typeMapping.get(cls));
    }

    private <T> T createInstance(Class<T> cls) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, ExceptionInInitializerError
    {
        var constructors = cls.getConstructors();
        ArrayList<Constructor> validCtors = new ArrayList<>();

        for (var ctor : constructors) {

            boolean skipCtor = false;

            var params = ctor.getParameters();

            for(var param : params) {
                if (!typeMapping.containsKey(param.getType())) {
                    skipCtor = true;
                    break;
                }
            }

            if(skipCtor)
                continue;
            else
                validCtors.add(ctor);
        }

        if(validCtors.size() == 0 || validCtors.size() > 1)
            throw new InstantiationException("There is no definitive Constructor for the type " + cls.getName());

        Constructor currentCtor = validCtors.get(0);
        Object[] argumentArray = new Object[currentCtor.getParameters().length];

        for (int i = 0 ; i<currentCtor.getParameters().length;i++)
        {
            try
            {
                argumentArray[i] = resolve(currentCtor.getParameters()[i].getType());
            }
            catch(Exception e)
            {
                throw e;
            }
        }

        return (T)currentCtor.newInstance(argumentArray);
    }

    protected final void addMapping(Class<?> src, Class<?> target)
    {
        typeMapping.put(src,target);
    }

    protected final void addMapping(Class<?> src, Class<?> target, boolean isSingle)
    {
        typeMapping.put(src,target);

        if(isSingle){
            objectMapping.put(src,null);
        }
    }


    protected abstract void configureTypes();

}
