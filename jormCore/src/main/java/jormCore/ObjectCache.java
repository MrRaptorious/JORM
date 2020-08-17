package jormCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Can cache objects extending PersistedObject
 */
@SuppressWarnings("unused")
public class ObjectCache {
    private final Map<Class<? extends PersistentObject>, List<PersistentObject>> permanentCache;
    private final Map<Class<? extends PersistentObject>, List<PersistentObject>> tempCache;
    private final List<Class<? extends PersistentObject>> types;

    public ObjectCache(List<Class<? extends PersistentObject>> types) {
        permanentCache = new HashMap<>();
        tempCache = new HashMap<>();
        this.types = types;

        // "deep" init caches
        for (var type : types) {
            permanentCache.put(type, new ArrayList<>());
            tempCache.put(type, new ArrayList<>());
        }
    }

    /**
     * Put the newly loaded objects in the permanent cache
     */
    public void applyLoadedObjectsToCache() {
        for (var elem : tempCache.entrySet()) {
            permanentCache.put(elem.getKey(), elem.getValue());
        }

        tempCache.replaceAll((k, v) -> new ArrayList<>());
    }

    /**
     * Empties the tempCache WITHOUT transmitting data to the permanentCache
     */
    public void emptyTempCache() {
        for (var type : types) {
            tempCache.put(type, new ArrayList<>());
        }
    }

    /**
     * Gets objects from a specific type from the cache
     *
     * @param cls look for objects from this type/class
     * @return list of cached objects from given type
     */
    public List<PersistentObject> get(Class<? extends PersistentObject> cls) {
        return permanentCache.get(cls);
    }

    /**
     * Gets objects from a specific type from the temp cache
     *
     * @param cls look for objects from this type/class
     * @return list of cached objects from given type
     */
    public List<PersistentObject> getTemp(Class<? extends PersistentObject> cls) {
        return tempCache.get(cls);
    }

    /**
     * Adds an object to the cache
     *
     * @param obj the object to add
     */
    public boolean add(PersistentObject obj) {
        Class<? extends PersistentObject> key = obj.getClass();
        if (permanentCache.containsKey(key)) {
            permanentCache.get(key).add(obj);
            return true;
        }

        return false;
    }

    /**
     * Adds an object to the temp cache
     *
     * @param obj the object to add
     */
    public boolean addTemp(PersistentObject obj) {
        Class<? extends PersistentObject> key = obj.getClass();
        if (tempCache.containsKey(key)) {
            tempCache.get(key).add(obj);
            return true;
        }

        return false;
    }
}
