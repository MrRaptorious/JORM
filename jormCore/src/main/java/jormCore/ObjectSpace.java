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
 * Main object to handle datamanipulation from user
 */
public class ObjectSpace {

	private Map<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>> changedObjects;
	private Map<Class<? extends PersistentObject>, List<PersistentObject>> createdObjects;
	private Map<Class<? extends PersistentObject>, List<PersistentObject>> objectCache;
	private boolean isLoadingObjects;
	private DatabaseConnection connection;
	WrappingHandler wrappingHandler;
	FieldTypeParser fieldTypeParser;

	public ObjectSpace(DatabaseConnection connection,WrappingHandler handler, FieldTypeParser parser) {
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
		objectCache = new HashMap<Class<? extends PersistentObject>, List<PersistentObject>>();
		changedObjects = new HashMap<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>>();
		createdObjects = new HashMap<Class<? extends PersistentObject>, List<PersistentObject>>();
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
			Constructor<T> ctor = type.getConstructor(ObjectSpace.class);
			newObject = ctor.newInstance(this);
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

	public <T extends PersistentObject> T getObject(Class<T> cls, UUID id) {
		return getObject(cls, id, false);
	}

	public <T extends PersistentObject> T getObject(Class<T> cls, UUID id, boolean loadFromDB) {
		// check cache
		T objToReturn = null;

		if (objectCache != null && objectCache.containsKey(cls)) {

			for (PersistentObject obj : objectCache.get(cls)) {
				if (obj.getID().equals(id)) {
					// objToReturn = (T) obj;
					objToReturn = castToT(obj);
					break;
				}
			}
		}

		if (objToReturn == null && loadFromDB) {

			ClassWrapper clsWrapper = wrappingHandler.getClassWrapper(cls);

			ResultSet set = connection.getObject(clsWrapper, id);

			objectCache.put(clsWrapper.getClassToWrap(), new ArrayList<>());

			try {
				while (set.next()) {
					// objToReturn = (T) loadObject(clsWrapper, set);
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
		}

		if (objectCache != null && objectCache.containsKey(cls)) {
			List<T> castedList = new ArrayList<T>();

			for (PersistentObject obj : objectCache.get(cls)) {
				// castedList.add((T)obj);
				castedList.add(castToT(obj));
			}

			return castedList;
		}

		return null;
	}

	/**
	 * Loads always from db TODO create where for memory search
	 * 
	 * @param <T>
	 * @param cls
	 * @param clause
	 * @return
	 */
	public <T extends PersistentObject> List<T> getObjects(Class<T> cls, WhereClause clause) {
		ClassWrapper classWrapper = wrappingHandler.getClassWrapper(cls);
		List<T> objectsToReturn = new ArrayList<>();

		ResultSet set = connection.getTable(classWrapper, clause);

		objectCache.put(classWrapper.getClassToWrap(), new ArrayList<>());

		try {
			while (set.next()) {
				objectsToReturn.add(castToT(loadObject(classWrapper, set)));
			}
		} catch (SQLException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}

		return objectsToReturn;
	}

	/**
	 * Reloads a type from the database and updates the cache
	 * 
	 * @param classWrapper the requested type
	 */
	private void refreshType(ClassWrapper classWrapper) {

		ResultSet set = connection.getTable(classWrapper);

		objectCache.put(classWrapper.getClassToWrap(), new ArrayList<>());

		try {
			while (set.next()) {
				loadObject(classWrapper, set);
			}
		} catch (SQLException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private PersistentObject loadObject(ClassWrapper classWrapper, ResultSet set) throws SQLException {

		isLoadingObjects = true;

		PersistentObject pobject = null;

		pobject = createValueObject(classWrapper, set);

		// add to cache to not load endless from DB
		objectCache.get(classWrapper.getClassToWrap()).add(pobject);

		pobject = fillReferences(classWrapper, set, pobject);

		isLoadingObjects = false;

		return pobject;
	}

	private PersistentObject fillReferences(ClassWrapper classWrapper, ResultSet set, PersistentObject pobject)
			throws SQLException {
		for (FieldWrapper fw : classWrapper.getRelationWrapper()) {
			if(fw.isList())
				continue;

			String oid = set.getString(fw.getName());

			if (oid != null && !oid.equals("")) {
				AssociationWrapper as = fw.getForeigenKey();
				ClassWrapper cw = as.getReferencingType();
				Class cl = cw.getClassToWrap();

				PersistentObject refObj = getObject(cl, UUID.fromString(oid), true);
				pobject.setRelation(fw.getOriginalField().getName(), refObj);
			}
		}

		return pobject;
	}

	private PersistentObject createValueObject(ClassWrapper classWrapper, ResultSet set) throws SQLException {
		PersistentObject pobject = null;

		// create Object
		try {
			Constructor<? extends PersistentObject> ctor = classWrapper.getClassToWrap()
					.getConstructor(ObjectSpace.class);

			pobject = ctor.newInstance(this);

			// set Object fields
			for (FieldWrapper fw : classWrapper.getWrappedValueMemeberWrapper()) {
				Object value = set.getString(fw.getName());

				pobject.setMemberValue(fw.getOriginalField().getName(),
						fieldTypeParser.castValue(fw.getOriginalField().getType(), value));
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
		return pobject;
	}

	/**
	 * Reloads all types from the database and updates the cache
	 */
	public void refresh() {

			List<ClassWrapper> typeList = wrappingHandler.getWrapperList();

			for (ClassWrapper clsWr : typeList)
				refreshType(clsWr);

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
			changedObjects.put(changedObject.getClass(), new HashMap<PersistentObject, ChangedObject>());
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
	 * Updates database and updates all objects
	 */
	private void updateObjects() throws SQLException{
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
	 * Rolls the current chaneges in the objectspace back
	 */
	public void rollbackChanges() {
		for ( var type : changedObjects.entrySet()) {
			for (var cObject : type.getValue().entrySet()) {
				cObject.getValue().rollback();
			}
		}

		changedObjects.clear();
	}

	/**
	 * Determines if the ObjectSpace is currently loading from the database
	 * 
	 * @return
	 */
	public boolean isLoadingObjects() {
		return isLoadingObjects;
	}

	@SuppressWarnings("unchecked")
	private <T extends PersistentObject> T castToT(PersistentObject po) {
		return (T) po;
	}

	public WrappingHandler getWrappingHandler() {
		return wrappingHandler;
	}
}