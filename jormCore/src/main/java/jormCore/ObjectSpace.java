package jormCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import jormCore.criteria.WhereClause;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.wrapping.AssociationWrapper;
import jormCore.wrapping.ClassWrapper;
import jormCore.wrapping.FieldWrapper;
import jormCore.wrapping.WrappingHandler;

/**
 * Main object to handle data manipulation from user
 */
@SuppressWarnings("unused")
public class ObjectSpace {

    private Map<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>> changedObjects;
    private Map<Class<? extends PersistentObject>, List<PersistentObject>> createdObjects;
    private ObjectCache objectCache;
    private boolean isLoadingObjects;
    private final DatabaseConnection connection;
    WrappingHandler wrappingHandler;
    FieldTypeParser fieldTypeParser;

    public ObjectSpace(DatabaseConnection connection, WrappingHandler handler, FieldTypeParser parser) {
        this.connection = connection;
        wrappingHandler = handler;
        fieldTypeParser = parser;
        initObjectSpace();
        refresh();
    }

    public ObjectSpace(DatabaseConnection connection, WrappingHandler handler, FieldTypeParser parser, boolean refresh) {
        this.connection = connection;
        wrappingHandler = handler;
        fieldTypeParser = parser;
        initObjectSpace();

        if (refresh)
            refresh();
    }

    /**
     * Initializes the object and loads it with data
     */
    private void initObjectSpace() {
        isLoadingObjects = false;
        changedObjects = new HashMap<>();
        createdObjects = new HashMap<>();
        objectCache = new ObjectCache(wrappingHandler.getRegisterdTypes());
    }

    /**
     * Marks that there is a object to be created in the database
     *
     * @param obj the newly created object which needs to be saved in the database
     */
    public void addCreatedObject(PersistentObject obj) {
        if (!createdObjects.containsKey(obj.getClass()))
            createdObjects.put(obj.getClass(), new ArrayList<>());

        if (!createdObjects.get(obj.getClass()).contains(obj))
            createdObjects.get(obj.getClass()).add(obj);
    }

    /**
     * Creates an object from the requested type
     *
     * @param <T>  the requested type
     * @param type the class of the requested type
     * @return an object from type T
     */
    public <T extends PersistentObject> T createObject(Class<T> type) {

        T newObject = null;

        try {
            Constructor<T> constructor = type.getConstructor(ObjectSpace.class);
            newObject = constructor.newInstance(this);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            // TODO
            e.printStackTrace();
        }

        return newObject;
    }

    /**
     * Returns a list of cached objects from the requested type.
     *
     * @param <T> the requested type
     * @param cls the class of the requested type
     * @return a list of objects from the requested type
     */
    public <T extends PersistentObject> List<T> getObjects(Class<T> cls) {
        return getObjects(cls, false);
    }

    /**
     * gets one cached object
     *
     * @param cls the class of the object to load
     * @param id  the uuid of the object to load
     * @param <T> the type of the object to load
     * @return a single cached object of type T with the given uuid
     */
    public <T extends PersistentObject> T getObject(Class<T> cls, UUID id) {
        return getObject(cls, id, false);
    }

    /**
     * gets one cached object
     *
     * @param cls        the class of the object to load
     * @param id         the uuid of the object to load
     * @param loadFromDB indicates whether  the object should be loaded from the db or not
     * @param <T>        the type of the object to load
     * @return a single cached object of type T with the given uuid
     */
    public <T extends PersistentObject> T getObject(Class<T> cls, UUID id, boolean loadFromDB) {

        T objToReturn = null;

        // check cache
        for (PersistentObject obj : objectCache.get(cls)) {
            if (obj.getID().equals(id)) {
                // objToReturn = (T) obj;
                objToReturn = castToT(obj);
                break;
            }
        }

        if (objToReturn == null && loadFromDB) {

            ClassWrapper clsWrapper = wrappingHandler.getClassWrapper(cls);

            ResultSet set = connection.getObject(clsWrapper, id);

            try {
                while (set.next()) {
                    objToReturn = castToT(loadObject(clsWrapper, set));
                }
            } catch (SQLException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return objToReturn;
    }

    /**
     * Returns a list of objects from the requested type (LOADS POSSIBLY FROM DB)
     *
     * @param <T>        the requested type
     * @param cls        the class of the requested type
     * @param loadFromDB determines if only cached or also non cached objects will
     *                   be returned
     * @return a list of objects from the requested type
     */
    public <T extends PersistentObject> List<T> getObjects(Class<T> cls, boolean loadFromDB) {

        if (loadFromDB) {
            refreshType(wrappingHandler.getClassWrapper(cls));
            objectCache.applyLoadedObjectsToCache();
        }

        List<T> castedList = new ArrayList<>();

        for (PersistentObject obj : objectCache.get(cls)) {
            castedList.add(castToT(obj));
        }

        return castedList.size() > 0 ? castedList : null;
    }

    /**
     * Loads always from db TODO create where for memory search
     *
     * @param <T>    type for cls argument
     * @param cls    class to load from db
     * @param clause where clause to restrict the search
     * @return list of t
     */
    public <T extends PersistentObject> List<T> getObjects(Class<T> cls, WhereClause clause) {
        ClassWrapper classWrapper = wrappingHandler.getClassWrapper(cls);
        List<T> objectsToReturn = new ArrayList<>();

        ResultSet set = connection.getTable(classWrapper, clause);

        try {
            while (set.next()) {
                objectsToReturn.add(castToT(loadObject(classWrapper, set)));
            }

            objectCache.applyLoadedObjectsToCache();
        } catch (SQLException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return objectsToReturn;
    }

    /**
     * Reloads all types from the database and updates the cache
     */
    public void refresh() {

        List<ClassWrapper> typeList = wrappingHandler.getWrapperList();

        for (ClassWrapper clsWr : typeList)
            refreshType(clsWr);

        objectCache.applyLoadedObjectsToCache();
    }

    /**
     * Marks a change to an object to update the database
     *
     * @param changedObject the PersistentObject which has changed
     * @param fieldName     the name of the changed member
     * @param newValue      the new value from the changed member
     */
    public void addChangedObject(PersistentObject changedObject, String fieldName, Object newValue) {
        if (!changedObjects.containsKey(changedObject.getClass())) {
            changedObjects.put(changedObject.getClass(), new HashMap<>());
        }

        if (!changedObjects.get(changedObject.getClass()).containsKey(changedObject)) {
            changedObjects.get(changedObject.getClass()).put(changedObject, new ChangedObject(changedObject));
        }

        // perhaps put old value in parameter for performance
        changedObjects.get(changedObject.getClass()).get(changedObject)
                .addChangedField(fieldName, newValue, changedObject.getMemberValue(fieldName));
    }

    /**
     * Updates database and commits all changes to all objects
     */
    public void commitChanges() {
        try {
            connection.beginTransaction();

            createObjects();
            updateObjects();

            connection.commitTransaction();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            connection.rollbackTransaction();
        }

        createdObjects.clear();
        changedObjects.clear();
    }


    /**
     * Rolls the current changes in the objectSpace back
     */
    public void rollbackChanges() {
        for (var type : changedObjects.entrySet()) {
            for (var cObject : type.getValue().entrySet()) {
                cObject.getValue().rollback();
            }
        }

        changedObjects.clear();
    }

    /**
     * Determines if the ObjectSpace is currently loading from the database
     */
    public boolean isLoadingObjects() {
        return isLoadingObjects;
    }

    /**
     * Returns the current wrapping handler of the objectspace
     */
    public WrappingHandler getWrappingHandler() {
        return wrappingHandler;
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentObject> T castToT(PersistentObject po) {
        return (T) po;
    }

    /**
     * Updates database and updates all objects
     */
    private void updateObjects() throws SQLException {
        for (Entry<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>> type : changedObjects
                .entrySet()) {
            for (Entry<PersistentObject, ChangedObject> typeObject : type.getValue().entrySet()) {
                connection.update(typeObject.getValue());
            }
        }
    }

    /**
     * Updates database and creates all created objects
     */
    private void createObjects() throws SQLException {
        for (Entry<Class<? extends PersistentObject>, List<PersistentObject>> type : createdObjects.entrySet()) {
            for (PersistentObject createdObject : type.getValue()) {

                // extract all relations in objects to create and add them to the changed
                // objects

                List<FieldWrapper> relationFields = wrappingHandler
                        .getClassWrapper(createdObject.getClass()).getRelationWrapper();

                for (FieldWrapper relation : relationFields) {

                    String relationMemberName = relation.getOriginalField().getName();

                    Object o = createdObject.getMemberValue(relationMemberName);

                    if (o != null) {
                        addChangedObject(createdObject, relationMemberName, o);
                        createdObject.setMemberValue(relationMemberName, null);
                    }
                }

                connection.create(createdObject);
            }
        }
    }

    /**
     * Reloads a type from the database and updates the cache
     *
     * @param classWrapper the requested type
     */
    private void refreshType(ClassWrapper classWrapper) {

        ResultSet set = connection.getTable(classWrapper);

        try {
            while (set.next()) {
                UUID elementUUID = UUID.fromString(set.getString(PersistentObject.KeyPropertyName));

                // only load if not already loaded
                if (objectCache.getTemp(classWrapper.getClassToWrap()).stream().noneMatch(x -> x.getID().equals(elementUUID))) {
                    loadObject(classWrapper, set);
                }
            }
        } catch (SQLException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private PersistentObject loadObject(ClassWrapper classWrapper, ResultSet set) throws SQLException {
        isLoadingObjects = true;

        PersistentObject pObject;
        pObject = createValueObject(classWrapper, set);

        objectCache.addTemp(classWrapper.getClassToWrap(), pObject);

        fillReferences(classWrapper, set, pObject);

        isLoadingObjects = false;

        return pObject;
    }

    /**
     * @param pObject object to fill references
     */
    private void fillReferences(ClassWrapper classWrapper, ResultSet set, PersistentObject pObject)
            throws SQLException {
        for (FieldWrapper fw : classWrapper.getRelationWrapper()) {
            if (fw.isList())
                continue;

            String oid = set.getString(fw.getName());

            if (oid != null && !oid.equals("")) {
                UUID uuidToCompare = UUID.fromString(oid);

                AssociationWrapper as = fw.getForeigenKey();
                ClassWrapper cw = as.getReferencingType();
                Class<? extends PersistentObject> cl = cw.getClassToWrap();

                // check if refObj is already loaded
                PersistentObject refObj = objectCache.getTemp(cl).stream().filter(x -> x.getID().equals(uuidToCompare)).findFirst().orElse(null);

                if (refObj == null)
                    refObj = getObject(cl, uuidToCompare, true);

                pObject.setRelation(fw.getOriginalField().getName(), refObj);
            }
        }
    }

    private PersistentObject createValueObject(ClassWrapper classWrapper, ResultSet set) throws SQLException {
        PersistentObject pObject = null;

        // create Object
        try {
            Constructor<? extends PersistentObject> constructor = classWrapper.getClassToWrap()
                    .getConstructor(ObjectSpace.class);

            pObject = constructor.newInstance(this);

            // set Object fields
            for (FieldWrapper fw : classWrapper.getWrappedValueMemeberWrapper()) {
                Object value = set.getString(fw.getName());

                pObject.setMemberValue(fw.getOriginalField().getName(),
                        fieldTypeParser.castValue(fw.getOriginalField().getType(), value));
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return pObject;
    }
}