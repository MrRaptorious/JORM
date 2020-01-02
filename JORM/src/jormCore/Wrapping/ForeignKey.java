package jormCore.Wrapping;

import java.lang.reflect.Field;

import jormCore.PersistentObject;

public class ForeignKey {

	private ClassWrapper referencingType;
	private FieldWrapper referencingPrimaryKey;

	public ForeignKey(ClassWrapper type) {
		this.referencingType = type;
		referencingPrimaryKey = type.getPrimaryKey();
	}

	public String getReferencingTypeName() {
		return referencingType.getName();
	}

	public String getReferencingPrimaryKeyName() {
		return referencingPrimaryKey.getName();
	}
}
