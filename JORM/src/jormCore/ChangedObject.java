package jormCore;

import java.util.HashMap;
import java.util.Map;

import jormCore.Wrapping.WrappingHandler;

public class ChangedObject {

	private PersistentObject runtimeObject;
	private Map<String, Object> changedFields;
	
	public ChangedObject(PersistentObject runtimeObject) {
		this.runtimeObject = runtimeObject;
		changedFields = new HashMap<>();
	}

	public void addChangedField(String fieldName, Object value) {

		changedFields.put(fieldName, value);
	}

	public Map<String, Object> getChanedFields() {
		return changedFields;
	}

	public PersistentObject getRuntimeObject() {
		return runtimeObject;
	}
}
