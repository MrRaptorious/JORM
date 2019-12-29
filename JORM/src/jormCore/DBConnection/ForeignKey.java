package jormCore.DBConnection;

import java.lang.reflect.Field;

import jormCore.PersistentObject;

public class ForeignKey {

	private Class<? extends PersistentObject> referencingType;
	private Field referencingPrimaryKey;

	public ForeignKey(Class<? extends PersistentObject> type) {
		this.referencingType = type;
		referencingPrimaryKey = PersistentObject.getPrimaryKey(type);	
	}

	public String getReferencingTypeName() {
		return referencingType.getSimpleName();
	}

	public String getReferencingPrimaryKeyName() {
		return referencingPrimaryKey.getName();
	}
}
