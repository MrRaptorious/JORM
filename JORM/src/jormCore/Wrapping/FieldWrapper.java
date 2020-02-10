package jormCore.Wrapping;

import java.lang.reflect.Field;
import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;
import jormCore.Annotaions.Autoincrement;
import jormCore.Annotaions.CanNotBeNull;
import jormCore.Annotaions.Persistent;
import jormCore.Annotaions.PrimaryKey;

public class FieldWrapper {
	private ClassWrapper declaringClassWrapper;
	private Field fieldToWrap;

	private AssociationWrapper association;
	private String name;
	private String type;
	private boolean isPrimaryKey;
	private boolean canNotBeNull;
	private boolean autoincrement;

	@SuppressWarnings("unchecked")
	public FieldWrapper(ClassWrapper cw, Field field) {
		fieldToWrap = field;
		name = calculateFieldName(field);
		type = JormApplication.getApplication().getCurrentFieldTypeParser().parseFieldType(field.getClass());
		isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
		canNotBeNull = field.isAnnotationPresent(CanNotBeNull.class);
		autoincrement = field.isAnnotationPresent(Autoincrement.class);
		declaringClassWrapper = cw;
		association = null;
	}

	public boolean isForeigenKey() {
		return association != null;
	}

	public AssociationWrapper getForeigenKey() {
		return association;
	}

	public String getName() {
		return name;
	}

	public String getDBType() {
		return type;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public boolean isCanNotBeNull() {
		return canNotBeNull;
	}

	public boolean isAutoincrement() {
		return autoincrement;
	}

	public ClassWrapper getClassWrapper() {
		return declaringClassWrapper;
	}

	public void updateAssociation() {
		if (PersistentObject.class.isAssignableFrom(fieldToWrap.getType())) {
			String name = null;

			if (fieldToWrap.getAnnotation(Association.class) != null)
				name = fieldToWrap.getAnnotation(Association.class).name();

			ClassWrapper foreigneClassWrapper = WrappingHandler.getWrappingHandler()
					.getClassWrapper((Class<? extends PersistentObject>) fieldToWrap.getType());

			this.association = new AssociationWrapper(foreigneClassWrapper, name);
		}
	}

	public static String calculateFieldName(Field field) {
		String name = "";
		Persistent persistentAnnotation = field.getAnnotation(Persistent.class);

		if (persistentAnnotation != null)
			name = persistentAnnotation.name();

		if (name == null || name.equals(""))
			name = field.getName();

		return name;
	}

	public Field getOriginalField() {
		return fieldToWrap;
	}
}
