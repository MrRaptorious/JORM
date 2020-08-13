package jormCore;

import jormCore.annotaions.Persistent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectCache {
    private Map<Class<? extends PersistentObject>, List<PersistentObject>> permanentCache;
    private Map<Class<? extends PersistentObject>, List<PersistentObject>> tempCache;
    
    public ObjectCache(List<Class<? extends PersistentObject>> types) {
        permanentCache = new HashMap<>();
        tempCache = new HashMap<>();

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


    public List<PersistentObject> get(Class<? extends PersistentObject> cls) {
        return permanentCache.get(cls);
    }

    public List<PersistentObject> getTemp(Class<? extends PersistentObject> cls) {
        return tempCache.get(cls);
    }

    public void add(Class<? extends PersistentObject> type, PersistentObject obj){
        permanentCache.get(type).add(obj);
    }

    public void addTemp(Class<? extends PersistentObject> type, PersistentObject obj){
        tempCache.get(type).add(obj);
    }
}
