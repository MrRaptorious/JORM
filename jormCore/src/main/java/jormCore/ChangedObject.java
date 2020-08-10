package jormCore;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a changed PersistentObject
 */
public class ChangedObject {
	private PersistentObject runtimeObject;
	private Map<String, Object[]> changedFields;

	public ChangedObject(PersistentObject runtimeObject) {
		this.runtimeObject = runtimeObject;
		changedFields = new HashMap<>();
	}

	/**
	 * Adds the the "fieldName" to the list of changed fields
	 * 
	 * @param fieldName The actual name of the changed field
	 * @param newValue     The new value in the changed field
	 * @param oldValue     The old value in the changed field
	 */
	public void addChangedField(String fieldName, Object newValue, Object oldValue) {
		changedFields.put(fieldName, new Object[] { newValue, oldValue });
	}

	/**
	 * Returns all the changed fields of the handled PersistentObject
	 */
	public Map<String, Object> getChanedFields() {

		HashMap<String, Object> tmpMap = new HashMap<String, Object>();

		for (var elem : changedFields.entrySet()) {
			tmpMap.put(elem.getKey(), elem.getValue()[0]);
		}

		return tmpMap;
	}

	/**
	 * Returns handled PersistentObject
	 */
	public PersistentObject getRuntimeObject() {
		return runtimeObject;
	}

	/**
	 * Rolls the changes on the handled PersistentObject back
	 */
	public void rollback() {
		for (var elem : changedFields.entrySet()) {
			runtimeObject.setMemberValue(elem.getKey(), elem.getValue()[1]);
		}
	}
}
