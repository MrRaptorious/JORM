package jormCore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.*;
import jormCore.DBConnection.DatabaseConnection;
import jormCore.Wrapping.ClassWrapper;
import jormCore.Wrapping.FieldWrapper;
import jormCore.Wrapping.WrappingHandler;

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

	private void initObjectSpace() {
		isLoadingObjects = false;
		objectCache = new HashMap<Class<? extends PersistentObject>, List<PersistentObject>>();
		changedObjects = new HashMap<Class<? extends PersistentObject>, Map<PersistentObject, ChangedObject>>();
		createdObjects = new HashMap<Class<? extends PersistentObject>, List<PersistentObject>>();
		refresh();
	}

	public void addCreatedObject(PersistentObject obj) {
		if (!createdObjects.containsKey(obj.getClass()))
			createdObjects.put(obj.getClass(), new ArrayList<>());

		if (!createdObjects.get(obj.getClass()).contains(obj))
			createdObjects.get(obj.getClass()).add(obj);
	}

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

	public <T extends PersistentObject> List<T> getObjects(Class<T> cls) {
		return getObjects(cls, false);
	}

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

	private void refreshType(ClassWrapper classWrapper) {
		this.isLoadingObjects = true;

		ResultSet set = connection.getTable(classWrapper.getName());

		objectCache.put(classWrapper.getClassToWrap(), new ArrayList<>());

		try {
			while (set.next()) {

				// create Object
				try {
					Constructor<? extends PersistentObject> ctor = classWrapper.getClassToWrap()
							.getConstructor(ObjectSpace.class);

					PersistentObject pobject;
					pobject = ctor.newInstance(this);

					// set Object fields
					for (FieldWrapper fw : classWrapper.getWrappedFields()) {

						Object value = set.getString(fw.getName());

						pobject.setMemberValue(fw.getOriginalField().getName(),
								connection.castValue(fw.getOriginalField().getType(), value));

					}

					objectCache.get(classWrapper.getClassToWrap()).add(pobject);

				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.isLoadingObjects = false;
		}
	}

	public void refresh() {
		List<ClassWrapper> typeList = WrappingHandler.getWrappingHandler().getWrapperList();

		for (ClassWrapper clsWr : typeList)
			refreshType(clsWr);
	}

	public void addChangedObject(PersistentObject changedObject, String fieldName, Object newValue) {
		if (!changedObjects.containsKey(changedObject.getClass())) {
			changedObjects.put(changedObject.getClass(), new HashMap<PersistentObject, ChangedObject>());
		}

		if (!changedObjects.get(changedObject.getClass()).containsKey(changedObject)) {
			changedObjects.get(changedObject.getClass()).put(changedObject, new ChangedObject(changedObject));
		}

		changedObjects.get(changedObject.getClass()).get(changedObject).addChangedField(fieldName, newValue);
	}

	public void commitChanges() {
		try {
			connection.beginTransaction();

			// create objects
			for (Entry<Class<? extends PersistentObject>, List<PersistentObject>> type : createdObjects.entrySet()) {
				for (PersistentObject createdObject : type.getValue()) {

					// extract all relations in objects to create and add them to the changed objects
					
					List<FieldWrapper> relationFields = WrappingHandler.getWrappingHandler()
							.getClassWrapper(createdObject.getClass()).getSingleRelationWrapper();

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

	public boolean isLoadingObjects() {
		return isLoadingObjects;
	}
}