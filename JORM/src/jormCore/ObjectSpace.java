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
import jormCore.DBConnection.DatabaseConnection;
import jormCore.Wrapping.AssociationWrapper;
import jormCore.Wrapping.ClassWrapper;
import jormCore.Wrapping.FieldWrapper;
import jormCore.Wrapping.WrappingHandler;

/**
 * Main object to handle datamanipulation from user
 */
public class ObjectSpace {

	private Map<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>> changedObjects;
	private Map<Class<? extends PersistentObject>, List<PersistentObject>> createdObjects;
	private Map<Class<? extends PersistentObject>, List<PersistentObject>> objectCache;
	private boolean isLoadingObjects;
	private DatabaseConnection connection;

	public ObjectSpace(DatabaseConnection connection) {
		this.connection = connection;
		initObjectSpace();
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
		refresh();
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

	@SuppressWarnings("unchecked")
	public <T extends PersistentObject> T getObject(Class<T> cls, UUID id, boolean loadFromDB) {
		// check cache
		T objToReturn = null;

		if (objectCache != null && objectCache.containsKey(cls)) {

			for (PersistentObject obj : objectCache.get(cls)) {
				if (obj.getID().equals(id)) {
					objToReturn = (T) obj;
					break;
				}
			}
		}

		if (objToReturn == null && loadFromDB) {

			this.isLoadingObjects = true;

			ClassWrapper clsWrapper = WrappingHandler.getWrappingHandler().getClassWrapper(cls);

			ResultSet set = connection.getObject(clsWrapper, id);

			objectCache.put(clsWrapper.getClassToWrap(), new ArrayList<>());

			try {
				while (set.next()) {
					objToReturn = (T) loadObject(clsWrapper, set);
				}
			} catch (SQLException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				this.isLoadingObjects = false;
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
	@SuppressWarnings("unchecked")
	public <T extends PersistentObject> List<T> getObjects(Class<T> cls, boolean loadFromDB) {

		if (loadFromDB) {
			refreshType(WrappingHandler.getWrappingHandler().getClassWrapper(cls));
		}

		if (objectCache != null && objectCache.containsKey(cls)) {
			List<T> castedList = new ArrayList<T>();

			for (PersistentObject obj : objectCache.get(cls)) {
				castedList.add((T) obj);
			}

			return castedList;
		}

		return null;
	}

	/**
	 * Reloads a type from the database and updates the cache
	 * 
	 * @param classWrapper the requested type
	 */
	private void refreshType(ClassWrapper classWrapper) {
		this.isLoadingObjects = true;

		ResultSet set = connection.getTable(classWrapper);

		objectCache.put(classWrapper.getClassToWrap(), new ArrayList<>());

		try {
			while (set.next()) {
				loadObject(classWrapper, set);
			}
		} catch (SQLException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.isLoadingObjects = false;
		}
	}

	private PersistentObject loadObject(ClassWrapper classWrapper, ResultSet set) throws SQLException {
		PersistentObject pobject = null;

		pobject = createValueObject(classWrapper, set);

		// add to cache to not load endless from DB
		objectCache.get(classWrapper.getClassToWrap()).add(pobject);

		pobject = fillReferences(classWrapper, set, pobject);

		return pobject;
	}

	private PersistentObject fillReferences(ClassWrapper classWrapper, ResultSet set, PersistentObject pobject)
			throws SQLException {
		for (FieldWrapper fw : classWrapper.getRelationWrapper()) {

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
						connection.castValue(fw.getOriginalField().getType(), value));
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
		List<ClassWrapper> typeList = WrappingHandler.getWrappingHandler().getWrapperList();

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

		changedObjects.get(changedObject.getClass()).get(changedObject).addChangedField(fieldName, newValue);
	}

	/**
	 * Updates database and commits all changes to all objects
	 */
	public void commitChanges() {
		try {
			connection.beginTransaction();

			// create objects
			for (Entry<Class<? extends PersistentObject>, List<PersistentObject>> type : createdObjects.entrySet()) {
				for (PersistentObject createdObject : type.getValue()) {

					// extract all relations in objects to create and add them to the changed
					// objects

					List<FieldWrapper> relationFields = WrappingHandler.getWrappingHandler()
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

			// update objects
			for (Entry<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>> type : changedObjects
					.entrySet()) {
				for (Entry<PersistentObject, ChangedObject> typeObject : type.getValue().entrySet()) {
					connection.update(typeObject.getValue());
				}
			}

			connection.commitTransaction();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			connection.rollbackTransaction();
		}
	}

	/**
	 * Determines if the ObjectSpace is currently loading from the database
	 * 
	 * @return
	 */
	public boolean isLoadingObjects() {
		return isLoadingObjects;
	}
}