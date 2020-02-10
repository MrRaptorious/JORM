package jormCore;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a changed PersistentObject
 */
public class ChangedObject {
	private PersistentObject runtimeObject;
	private Map<String, Object> changedFields;
	
	public ChangedObject(PersistentObject runtimeObject) {
		this.runtimeObject = runtimeObject;
		changedFields = new HashMap<>();
	}

	/**
	 * Adds the the "fieldName" to the list of changed fields
	 * @param fieldName The actual name of the changed field
	 * @param value The new value in the changed field
	 */
	public void addChangedField(String fieldName, Object value) {

		changedFields.put(fieldName, value);
	}

	/**
	 * Returns all the changed fields of the handled PersistentObject
	 */
	public Map<String, Object> getChanedFields() {
		return changedFields;
	}

	/**
	 * Returns handled PersistentObject
	 */
	public PersistentObject getRuntimeObject() {
		return runtimeObject;
	}
}
